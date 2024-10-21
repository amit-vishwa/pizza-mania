package com.pizzamania.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.dto.CartDto;
import com.pizzamania.dto.ProductDto;
import com.pizzamania.dto.PurchaseDetailDto;
import com.pizzamania.dto.PurchaseDto;
import com.pizzamania.enumeration.ProcessEnum;
import com.pizzamania.mapper.CartMapper;
import com.pizzamania.mapper.ProductMapper;
import com.pizzamania.mapper.PurchaseDetailMapper;
import com.pizzamania.mapper.PurchaseMapper;
import com.pizzamania.model.Cart;
import com.pizzamania.model.Purchase;
import com.pizzamania.model.PurchaseDetail;
import com.pizzamania.repository.PurchaseManagementRepository;
import com.pizzamania.repository.PurchaseManagementSearchRepository;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.SecurityContextUtil;
import com.pizzamania.utility.Utility;

import jakarta.transaction.Transactional;

@Service
public class PurchaseManagementServiceImpl implements PurchaseManagementService {

	private static Logger log = LoggerFactory.getLogger(PurchaseManagementServiceImpl.class);

	@Autowired
	PurchaseManagementSearchRepository purchaseManagementSearchRepository;

	@Lazy
	@Autowired
	PurchaseDetailManagementService purchaseDetailManagementService;

	@Autowired
	PurchaseManagementRepository purchaseManagementRepository;

	@Autowired
	ProductManagementService productManagementService;

	@Lazy
	@Autowired
	CartManagementService cartManagementService;

	@Autowired
	PurchaseDetailMapper purchaseDetailMapper;

	@Autowired
	SecurityContextUtil securityContextUtil;

	@Autowired
	PurchaseMapper purchaseMapper;

	@Autowired
	ProductMapper productMapper;

	@Autowired
	CartMapper cartMapper;

	@Override
	public Page<PurchaseDto> searchPurchases(SearchCriteria<PurchaseDto> searchCriteria) {
		log.info("Searching for purchase records with provided search criteria");
		Purchase purchase = purchaseMapper.purchaseDtoToEntity(searchCriteria.getModel());
		SearchCriteria<Purchase> newSearchCriteria = new SearchCriteria<Purchase>(purchase, searchCriteria.getPaging(),
				searchCriteria.getSortFields(), searchCriteria.getDataFilterList());
		Page<Purchase> responsePage = purchaseManagementSearchRepository.search(newSearchCriteria);
		if (Utility.hasEntries(responsePage)) {
			List<PurchaseDto> purchases = new ArrayList<PurchaseDto>();
			responsePage.getPageEntries().forEach(response -> {
				PurchaseDto purchaseDto = purchaseMapper.purchaseEntityToDto(response);
				purchaseDto.setPurchaseDetailsDto(fetchPurchaseDetails(purchaseDto));
				purchases.add(purchaseDto);
			});
			return new Page<PurchaseDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
					responsePage.getTotalRecords(), purchases);
		}
		return new Page<PurchaseDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
				responsePage.getTotalRecords(), new ArrayList<PurchaseDto>());
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<PurchaseDto> addPurchases(List<PurchaseDto> requestList) {
		log.info("Inserting " + requestList.size() + " purchase records in the database");
		List<Purchase> purchases = new ArrayList<Purchase>();
		requestList.forEach(request -> {
			if (validPurchase(request)) {
				request.setStatus(GlobalConstants.PENDING_STATUS);
				request.setStatusTimestamp(new Timestamp(System.currentTimeMillis()));
				request.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
				request.setCreatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				request.setCreatedByUser(Utility.hasValue(request.getCreatedByUser()) ? request.getCreatedByUser()
						: getLoggedInUserName());
				request.setCreatedByProcess(
						Utility.hasValue(request.getCreatedByProcess()) ? request.getCreatedByProcess()
								: ProcessEnum.ADD_PURCHASES.getProcessCode());
				purchases.add(purchaseMapper.purchaseDtoToEntity(request));
			}
		});
		if (purchases.size() == requestList.size()) {
			return saveAllPurchases(purchases);
		}
		return null;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<PurchaseDto> saveAllPurchases(List<Purchase> purchaseList) {
		log.info("Saving records in database!");
		return purchaseManagementRepository.saveAll(purchaseList).stream()
				.map(purchase -> purchaseMapper.purchaseEntityToDto(purchase)).collect(Collectors.toList());
	}

	private boolean validPurchase(PurchaseDto request) {
		if (Utility.hasValue(request) && Utility.hasValue(request.getPurchaseItems())
				&& Utility.hasValue(request.getPurchaseAmount()) && request.getPurchaseItems() > 0
				&& request.getPurchaseAmount() > 0) {
			return true;
		}
		log.info("Not a valid purchase request!");
		return false;
	}

	private List<PurchaseDetailDto> fetchPurchaseDetails(PurchaseDto response) {
		PurchaseDetailDto purchaseDetails = new PurchaseDetailDto();
		purchaseDetails.setPurchaseId(response.getPurchaseId());
		SearchCriteria<PurchaseDetailDto> searchCriteria = new SearchCriteria<PurchaseDetailDto>(purchaseDetails);
		return purchaseDetailManagementService.searchPurchaseDetails(searchCriteria).getPageEntries();
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public String cancelPurchase(Integer userId, Integer purchaseId) {
		List<CartDto> pendingCartList = getPendingUserCart(userId, purchaseId);
		if (Utility.listHasValues(pendingCartList)) {
			// Purchase orders can only be cancelled within 30mins of creating PENDING
			// orders
			Timestamp requiredTimeStamp = new Timestamp(System.currentTimeMillis() - 30 * 60 * 1000);
			if (pendingCartList.get(0).getStatusTimestamp().after(requiredTimeStamp)) {
				String process = ProcessEnum.CANCEL_PURCHASES.getProcessCode();
				String user = getLoggedInUserName();
				String status = GlobalConstants.CANCELED_STATUS;
				updateCartStatus(pendingCartList.get(0), process, user, status);
				updatePurchaseStatus(pendingCartList.get(0), process, user, status);
				updatePurchaseDetailsStatus(pendingCartList.get(0), process, user, status);
				return "Purchase order cancelled successfully!";
			}
			return "Purchase order older than 30mins, cannot be CANCELED!";
		}
		return "No active PENDING cart exists to CANCEL!";
	}

	private List<CartDto> getPendingUserCart(Integer userId, Integer purchaseId) {
		CartDto cart = new CartDto();
		cart.setUserId(userId);
		cart.setPurchaseId(purchaseId);
		cart.setStatus(GlobalConstants.PENDING_STATUS);
		cart.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
		SearchCriteria<CartDto> searchCriteria = new SearchCriteria<CartDto>(cart);
		List<CartDto> response = cartManagementService.searchItems(searchCriteria).getPageEntries();
		return Utility.hasEntries(response) ? response : null;
	}

	private void updateCartStatus(CartDto pendingCart, String process, String user, String status) {
		pendingCart.setLastUpdatedByUser(user);
		pendingCart.setLastUpdatedByProcess(process);
		pendingCart.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
		pendingCart.setStatus(status);
		pendingCart.setStatusTimestamp(new Timestamp(System.currentTimeMillis()));
		cartManagementService.saveAllCarts(new ArrayList<Cart>(Arrays.asList(cartMapper.cartDtoToEntity(pendingCart))));
	}

	private void updatePurchaseStatus(CartDto pendingCart, String process, String user, String status) {
		PurchaseDto purchaseDto = pendingCart.getPurchases().get(0);
		purchaseDto.setLastUpdatedByUser(user);
		purchaseDto.setLastUpdatedByProcess(process);
		purchaseDto.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
		purchaseDto.setStatus(status);
		purchaseDto.setStatusTimestamp(new Timestamp(System.currentTimeMillis()));
		saveAllPurchases(new ArrayList<Purchase>(Arrays.asList(purchaseMapper.purchaseDtoToEntity(purchaseDto))));
	}

	private void updatePurchaseDetailsStatus(CartDto pendingCart, String process, String user, String status) {
		List<PurchaseDetailDto> purchaseDetailsList = pendingCart.getPurchases().get(0).getPurchaseDetailsDto();
		if (Utility.listHasValues(purchaseDetailsList)) {
			List<PurchaseDetail> purchaseDetails = new ArrayList<PurchaseDetail>();
			purchaseDetailsList.forEach(purchaseDetail -> {
				purchaseDetail.setLastUpdatedByUser(user);
				purchaseDetail.setLastUpdatedByProcess(process);
				purchaseDetail.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				purchaseDetail.setStatus(status);
				purchaseDetail.setStatusTimestamp(new Timestamp(System.currentTimeMillis()));
				if (GlobalConstants.CANCELED_STATUS.equals(status)) {
					updateProducts(purchaseDetail);
				}
				purchaseDetails.add(purchaseDetailMapper.purchaseDetailDtoToEntity(purchaseDetail));
			});
			purchaseDetailManagementService.saveAllPurchaseDetails(purchaseDetails);
		}
	}

	private void updateProducts(PurchaseDetailDto purchaseDetail) {
		ProductDto model = new ProductDto();
		model.setProductId(purchaseDetail.getProductId());
		model.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
		SearchCriteria<ProductDto> searchCriteria = new SearchCriteria<ProductDto>(model);
		List<ProductDto> productList = productManagementService.searchProducts(searchCriteria).getPageEntries();
		if (Utility.listHasValues(productList)) {
			productList.forEach(product -> {
				product.setProductQuantity(product.getProductQuantity() + purchaseDetail.getProductQuantity());
				product.setProductAvailable(product.getProductQuantity() > 0);
				product.setLastUpdatedByUser(purchaseDetail.getLastUpdatedByUser());
				product.setLastUpdatedByProcess(purchaseDetail.getLastUpdatedByProcess());
				product.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
			});
			productManagementService.saveAllProducts(productList.stream()
					.map(dto -> productMapper.productDtoToEntity(dto)).collect(Collectors.toList()));
		}
	}

	private String getLoggedInUserName() {
		return securityContextUtil.getUserName();
	}

	@Override
	public String completePurchase() {
		List<CartDto> pendingCartList = getPendingUserCart(null, null);
		if (Utility.listHasValues(pendingCartList)) {
			// Purchase orders can only be completed after 30mins of creating PENDING
			// orders
			Timestamp requiredTimeStamp = new Timestamp(System.currentTimeMillis() - 30 * 60 * 1000);
			pendingCartList = pendingCartList.stream()
					.filter(pendingCart -> pendingCart.getStatusTimestamp().before(requiredTimeStamp))
					.collect(Collectors.toList());
			if (Utility.listHasValues(pendingCartList)) {
				pendingCartList.forEach(pendingCart -> {
					String process = ProcessEnum.COMPLETE_PURCHASES.getProcessCode();
					String user = getLoggedInUserName();
					String status = GlobalConstants.COMPLETED_STATUS;
					updateCartStatus(pendingCart, process, user, status);
					updatePurchaseStatus(pendingCart, process, user, status);
					updatePurchaseDetailsStatus(pendingCart, process, user, status);
				});
				return "Marked " + pendingCartList.size() + " purchase orders as complete";
			}
		}
		return null;
	}

}
