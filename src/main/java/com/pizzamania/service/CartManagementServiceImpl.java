package com.pizzamania.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.dto.CartDto;
import com.pizzamania.dto.PurchaseDto;
import com.pizzamania.enumeration.ProcessEnum;
import com.pizzamania.mapper.CartMapper;
import com.pizzamania.model.Cart;
import com.pizzamania.repository.CartManagementRepository;
import com.pizzamania.repository.CartManagementSearchRepository;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.SecurityContextUtil;
import com.pizzamania.utility.Utility;

import jakarta.transaction.Transactional;

@Service
public class CartManagementServiceImpl implements CartManagementService {

	private static Logger log = LoggerFactory.getLogger(CartManagementServiceImpl.class);

	@Autowired
	CartManagementSearchRepository cartManagementSearchRepository;

	@Lazy
	@Autowired
	PurchaseManagementService purchaseManagementService;

	@Autowired
	CartManagementRepository cartManagementRepository;

	@Autowired
	SecurityContextUtil securityContextUtil;

	@Autowired
	CartMapper cartMapper;

	@Override
	public Page<CartDto> searchItems(SearchCriteria<CartDto> searchCriteria) {
		log.info("Searching for cart items with provided search criteria");
		Cart cart = cartMapper.cartDtoToEntity(searchCriteria.getModel());
		SearchCriteria<Cart> newSearchCriteria = new SearchCriteria<Cart>(cart, searchCriteria.getPaging(),
				searchCriteria.getSortFields(), searchCriteria.getDataFilterList());
		Page<Cart> responsePage = cartManagementSearchRepository.search(newSearchCriteria);
		if (Utility.hasEntries(responsePage)) {
			List<CartDto> carts = new ArrayList<CartDto>();
			responsePage.getPageEntries().forEach(response -> {
				CartDto cartDto = cartMapper.cartEntityToDto(response);
				cartDto.setPurchases(fetchPurchases(cartDto));
				carts.add(cartDto);
			});
			return new Page<CartDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
					responsePage.getTotalRecords(), carts);
		}
		return new Page<CartDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
				responsePage.getTotalRecords(), new ArrayList<CartDto>());
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<CartDto> addItems(List<CartDto> requestList) {
		log.info("Inserting " + requestList.size() + " cart records in the database");
		List<Cart> carts = new ArrayList<Cart>();
		for (CartDto request : requestList) {
			// Not keeping validation as cart is added or updated with no direct user
			// interference
			request.setRecordStatus(Utility.hasValue(request.getRecordStatus()) ? request.getRecordStatus()
					: GlobalConstants.ACTIVE_RECORD_STATUS);
			request.setCreatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
			request.setCreatedByUser(
					Utility.hasValue(request.getCreatedByUser()) ? request.getCreatedByUser() : getLoggedInUserName());
			request.setCreatedByProcess(Utility.hasValue(request.getCreatedByProcess()) ? request.getCreatedByProcess()
					: ProcessEnum.ADD_CARTS.getProcessCode());
			carts.add(cartMapper.cartDtoToEntity(request));
		}
		if (carts.size() == requestList.size()) {
			return saveAllCarts(carts);
		}
		return null;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<CartDto> saveAllCarts(List<Cart> cartList) {
		log.info("Saving records in database!");
		return cartManagementRepository.saveAll(cartList).stream().map(cart -> cartMapper.cartEntityToDto(cart))
				.collect(Collectors.toList());
	}

	private List<PurchaseDto> fetchPurchases(CartDto response) {
		PurchaseDto purchases = new PurchaseDto();
		purchases.setPurchaseId(response.getPurchaseId());
		SearchCriteria<PurchaseDto> searchCriteria = new SearchCriteria<PurchaseDto>(purchases);
		return purchaseManagementService.searchPurchases(searchCriteria).getPageEntries();
	}

	private String getLoggedInUserName() {
		return securityContextUtil.getUserName();
	}

}
