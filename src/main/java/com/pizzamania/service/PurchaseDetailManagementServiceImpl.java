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
import com.pizzamania.exception.CRUDFailureException;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.exception.ResourceNotFoundException;
import com.pizzamania.mapper.ProductMapper;
import com.pizzamania.mapper.PurchaseDetailMapper;
import com.pizzamania.model.Product;
import com.pizzamania.model.PurchaseDetail;
import com.pizzamania.repository.PurchaseDetailManagementRepository;
import com.pizzamania.repository.PurchaseDetailManagementSearchRepository;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.SecurityContextUtil;
import com.pizzamania.utility.Utility;

import jakarta.transaction.Transactional;

@Service
public class PurchaseDetailManagementServiceImpl implements PurchaseDetailManagementService {

	private static Logger log = LoggerFactory.getLogger(PurchaseDetailManagementServiceImpl.class);

	@Autowired
	PurchaseDetailManagementSearchRepository purchaseDetailManagementSearchRepository;

	@Autowired
	PurchaseDetailManagementRepository purchaseDetailManagementRepository;

	@Lazy
	@Autowired
	PurchaseManagementService purchaseManagementService;

	@Autowired
	ProductManagementService productManagementService;

	@Lazy
	@Autowired
	CartManagementService cartManagementService;

	@Autowired
	UserManagementService userManagementService;

	@Autowired
	PurchaseDetailMapper purchaseDetailMapper;

	@Autowired
	SecurityContextUtil securityContextUtil;

	@Autowired
	ProductMapper productMapper;

	@Override
	public Page<PurchaseDetailDto> searchPurchaseDetails(SearchCriteria<PurchaseDetailDto> searchCriteria) {
		log.info("Searching for purchase detail records with provided search criteria");
		PurchaseDetail purchaseDetail = purchaseDetailMapper.purchaseDetailDtoToEntity(searchCriteria.getModel());
		SearchCriteria<PurchaseDetail> newSearchCriteria = new SearchCriteria<PurchaseDetail>(purchaseDetail,
				searchCriteria.getPaging(), searchCriteria.getSortFields(), searchCriteria.getDataFilterList());
		Page<PurchaseDetail> responsePage = purchaseDetailManagementSearchRepository.search(newSearchCriteria);
		if (Utility.hasEntries(responsePage)) {
			List<PurchaseDetailDto> purchaseDetails = new ArrayList<PurchaseDetailDto>();
			responsePage.getPageEntries().forEach(response -> {
				purchaseDetails.add(purchaseDetailMapper.purchaseDetailEntityToDto(response));
			});
			return new Page<PurchaseDetailDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
					responsePage.getTotalRecords(), purchaseDetails);
		}
		return new Page<PurchaseDetailDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
				responsePage.getTotalRecords(), new ArrayList<PurchaseDetailDto>());
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<PurchaseDetailDto> addPurchaseDetails(List<PurchaseDetailDto> requestList)
			throws CRUDFailureException, EntityMissingException, ResourceNotFoundException {
		log.info("Inserting " + requestList.size() + " purchase detail records in the database");
		List<PurchaseDetail> purchaseDetails = new ArrayList<PurchaseDetail>();
		String process = ProcessEnum.ADD_PURCHASES.getProcessCode();
		for (PurchaseDetailDto request : requestList) {
			if (isValidItem(request, process)) {
				request.setCreatedByProcess(process);
				request.setCreatedByUser(getLoggedInUserName());
				request.setStatus(GlobalConstants.PENDING_STATUS);
				request.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
				request.setStatusTimestamp(new Timestamp(System.currentTimeMillis()));
				request.setCreatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				purchaseDetails.add(purchaseDetailMapper.purchaseDetailDtoToEntity(request));
			}
		}
		if (purchaseDetails.size() == requestList.size()) {
			Integer purchaseId = generatePurchaseId(purchaseDetails);
			if (Utility.hasValue(purchaseId)) {
				saveCart(purchaseDetails.get(0), purchaseId);
				purchaseDetails.forEach(item -> item.setPurchaseId(purchaseId));
				return saveAllPurchaseDetails(purchaseDetails);
			}
			throw new CRUDFailureException("Failed to generate purchase id and save cart");
		}
		throw new CRUDFailureException(
				"Please correct " + (requestList.size() - purchaseDetails.size()) + " records with incorrect data");
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<PurchaseDetailDto> saveAllPurchaseDetails(List<PurchaseDetail> purchaseList) {
		log.info("Saving records in database!");
		return purchaseDetailManagementRepository.saveAll(purchaseList).stream()
				.map(purchase -> purchaseDetailMapper.purchaseDetailEntityToDto(purchase)).collect(Collectors.toList());
	}

	private boolean isValidItem(PurchaseDetailDto request, String process)
			throws CRUDFailureException, EntityMissingException, ResourceNotFoundException {
		if (Utility.hasValue(request) && Utility.hasValue(request.getProductId())
				&& Utility.hasValue(request.getProductQuantity()) && request.getProductQuantity() > 0) {
			Page<ProductDto> productResponse = fetchProductById(request.getProductId());
			if (Utility.hasEntries(productResponse)) {
				request.setProductCost(productResponse.getPageEntries().get(0).getProductCost());
				request.setTotalProductCost(request.getProductCost() * request.getProductQuantity());
				updateProductQuantity(productResponse.getPageEntries(), request, process);
				return true;
			}
			throw new CRUDFailureException("Product is unavailable for product id " + request.getProductId());
		}
		throw new CRUDFailureException("Not a valid purchase item request!");
	}

	private Page<ProductDto> fetchProductById(Integer productId) {
		ProductDto product = new ProductDto();
		product.setProductId(productId);
		product.setProductAvailable(true);
		product.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
		return productManagementService.searchProducts(new SearchCriteria<ProductDto>(product));
	}

	private void updateProductQuantity(List<ProductDto> productDtoList, PurchaseDetailDto request, String process)
			throws CRUDFailureException, EntityMissingException, ResourceNotFoundException {
		List<Product> productList = productDtoList.stream().map(dto -> productMapper.productDtoToEntity(dto))
				.collect(Collectors.toList());
		Integer availableProductQuantity = productList.get(0).getProductQuantity();
		Integer requiredProductQuantity = request.getProductQuantity();
		if (availableProductQuantity >= requiredProductQuantity) {
			productList.get(0).setProductQuantity(availableProductQuantity - requiredProductQuantity);
			productList.get(0).setProductAvailable(productList.get(0).getProductQuantity() > 0);
			productList.get(0).setLastUpdatedByProcess(process);
			productList.get(0).setLastUpdatedByUser(getLoggedInUserName());
			productManagementService.saveAllProducts(productList);
		} else {
			throw new CRUDFailureException(
					"Requested product quantity is more than the available quantity for product id "
							+ productList.get(0).getProductId());
		}
	}

	private Integer generatePurchaseId(List<PurchaseDetail> purchaseDetails) throws CRUDFailureException {
		Double purchaseAmount = purchaseDetails.stream().mapToDouble(item -> item.getTotalProductCost()).sum();
		PurchaseDto purchaseOrder = new PurchaseDto();
		purchaseOrder.setPurchaseItems(purchaseDetails.size());
		purchaseOrder.setPurchaseAmount(purchaseAmount);
		purchaseOrder.setCreatedByUser(purchaseDetails.get(0).getCreatedByUser());
		purchaseOrder.setCreatedByProcess(purchaseDetails.get(0).getCreatedByProcess());
		List<PurchaseDto> purchaseResponse = purchaseManagementService
				.addPurchases(new ArrayList<PurchaseDto>(Arrays.asList(purchaseOrder)));
		if (Utility.listHasValues(purchaseResponse) && Utility.hasValue(purchaseResponse.get(0).getPurchaseId())) {
			return purchaseResponse.get(0).getPurchaseId();
		}
		throw new CRUDFailureException("Failed to generate purchase id");
	}

	private void saveCart(PurchaseDetail purchaseDetails, Integer purchaseId) throws CRUDFailureException {
		CartDto cart = new CartDto();
		cart.setUserId(userManagementService.getLoggedInUserId());
		cart.setPurchaseId(purchaseId);
		cart.setStatus(GlobalConstants.PENDING_STATUS);
		cart.setStatusTimestamp(new Timestamp(System.currentTimeMillis()));
		cart.setCreatedByUser(purchaseDetails.getCreatedByUser());
		cart.setCreatedByProcess(purchaseDetails.getCreatedByProcess());
		if (!Utility.listHasValues(cartManagementService.addItems(new ArrayList<CartDto>(Arrays.asList(cart))))) {
			throw new CRUDFailureException("Failed to create cart for purchase id " + purchaseId);
		}
	}

	private String getLoggedInUserName() {
		return securityContextUtil.getUserName();
	}

}
