package com.pizzamania.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.dto.ProductDto;
import com.pizzamania.enumeration.ProcessEnum;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.exception.ResourceAlreadyExistsException;
import com.pizzamania.exception.ResourceNotFoundException;
import com.pizzamania.mapper.ProductMapper;
import com.pizzamania.model.Product;
import com.pizzamania.repository.ProductManagementRepository;
import com.pizzamania.repository.ProductManagementSearchRepository;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.SecurityContextUtil;
import com.pizzamania.utility.Utility;

import jakarta.transaction.Transactional;

@Service
public class ProductManagementServiceImpl implements ProductManagementService {

	private static Logger log = LoggerFactory.getLogger(ProductManagementServiceImpl.class);

	@Autowired
	ProductManagementSearchRepository productManagementSearchRepository;

	@Autowired
	ProductManagementRepository productManagementRepository;

	@Autowired
	SecurityContextUtil securityContextUtil;

	@Autowired
	ProductMapper productMapper;

	@Override
	public Page<ProductDto> searchProducts(SearchCriteria<ProductDto> searchCriteria) {
		log.info("Searching for product records with provided search criteria");
		Product product = productMapper.productDtoToEntity(searchCriteria.getModel());
		SearchCriteria<Product> newSearchCriteria = new SearchCriteria<Product>(product, searchCriteria.getPaging(),
				searchCriteria.getSortFields(), searchCriteria.getDataFilterList());
		Page<Product> responsePage = productManagementSearchRepository.search(newSearchCriteria);
		if (Utility.hasEntries(responsePage)) {
			List<ProductDto> products = new ArrayList<ProductDto>();
			responsePage.getPageEntries().forEach(response -> {
				products.add(productMapper.productEntityToDto(response));
			});
			return new Page<ProductDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
					responsePage.getTotalRecords(), products);
		}
		return new Page<ProductDto>(responsePage.getRecordsInPage(), responsePage.getCurrentPage(),
				responsePage.getTotalRecords(), new ArrayList<ProductDto>());
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<ProductDto> addProducts(List<ProductDto> requestList)
			throws EntityMissingException, ResourceAlreadyExistsException {
		log.info("Inserting " + requestList.size() + " product records in the database");
		List<Product> products = new ArrayList<Product>();
		for (ProductDto request : requestList) {
			if (isValidProduct(request)) {
				request.setProductAvailable(true);
				request.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
				request.setCreatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
				request.setCreatedByUser(getLoggedInUserName());
				request.setCreatedByProcess(ProcessEnum.ADD_PRODUCTS.getProcessCode());
				products.add(productMapper.productDtoToEntity(request));
			}
		}
		if (products.size() == requestList.size()) {
			return saveAllProducts(products);
		}
		return null;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<ProductDto> editProducts(List<ProductDto> requestList)
			throws EntityMissingException, ResourceNotFoundException {
		log.info("Updating " + requestList.size() + " product records in the database");
		return updateProductRecords(requestList, false);
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<ProductDto> deleteProducts(List<ProductDto> requestList)
			throws EntityMissingException, ResourceNotFoundException {
		log.info("Deleting " + requestList.size() + " product records");
		return updateProductRecords(requestList, true);
	}

	private boolean isValidProduct(ProductDto request) throws EntityMissingException, ResourceAlreadyExistsException {
		if (Utility.hasValue(request) && Utility.hasValue(request.getProductName())
				&& Utility.hasValue(request.getProductDescription()) && Utility.hasValue(request.getProductCost())
				&& Utility.hasValue(request.getProductQuantity()) && request.getProductCost() > 0
				&& request.getProductQuantity() > 0) {
			ProductDto product = new ProductDto();
			product.setProductName(request.getProductName());
			product.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
			SearchCriteria<ProductDto> searchCriteria = new SearchCriteria<ProductDto>(product);
			Page<ProductDto> response = searchProducts(searchCriteria);
			if (!Utility.hasEntries(response)) {
				return true;
			}
			throw new ResourceAlreadyExistsException("Product already exists with name " + request.getProductName());
		}
		throw new EntityMissingException("Provided data product name, description, cost and quantity are not valid!");
	}

	private List<ProductDto> updateProductRecords(List<ProductDto> requestList, boolean isDeleteRequest)
			throws EntityMissingException, ResourceNotFoundException {
		List<Product> products = new ArrayList<Product>();
		for (ProductDto request : requestList) {
			if (Utility.hasValue(request) && Utility.hasValue(request.getProductId())) {
				ProductDto product = new ProductDto();
				product.setProductId(request.getProductId());
				product.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
				SearchCriteria<ProductDto> searchCriteria = new SearchCriteria<ProductDto>(product);
				Page<ProductDto> response = searchProducts(searchCriteria);
				if (Utility.hasEntries(response)) {
					saveUpdatedProducts(request, isDeleteRequest, response.getPageEntries().get(0), products);
				} else {
					throw new ResourceNotFoundException(
							"Product does not exist with product id " + request.getProductId());
				}
			} else {
				throw new EntityMissingException("Product id cannot be null!");
			}
		}
		if (products.size() == requestList.size()) {
			return saveAllProducts(products);
		}
		return null;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public List<ProductDto> saveAllProducts(List<Product> productList) {
		log.info("Saving records in database!");
		return productManagementRepository.saveAll(productList).stream()
				.map(product -> productMapper.productEntityToDto(product)).collect(Collectors.toList());
	}

	private void saveUpdatedProducts(ProductDto request, boolean isDeleteRequest, ProductDto response,
			List<Product> products) {
		Product productCopy = productMapper.productDtoToEntity(response);
		boolean isRecordUpdated = false;
		String process = ProcessEnum.EDIT_PRODUCTS.getProcessCode();
		if (isDeleteRequest) {
			productCopy.setRecordStatus(GlobalConstants.DEACTIVE_RECORD_STATUS);
			process = ProcessEnum.DELETE_PRODUCTS.getProcessCode();
			isRecordUpdated = true;
		} else {
			if (Utility.hasValue(request.getProductName())) {
				productCopy.setProductName(request.getProductName());
				isRecordUpdated = true;
			}
			if (Utility.hasValue(request.getProductDescription())) {
				productCopy.setProductDescription(request.getProductDescription());
				isRecordUpdated = true;
			}
			if (Utility.hasValue(request.getProductCost()) && request.getProductCost() >= 0) {
				productCopy.setProductCost(request.getProductCost());
				isRecordUpdated = true;
			}
			if (Utility.hasValue(request.getProductQuantity()) && request.getProductQuantity() >= 0) {
				productCopy.setProductQuantity(request.getProductQuantity());
				productCopy.setProductAvailable(productCopy.getProductQuantity() > 0);
				isRecordUpdated = true;
			}
		}
		if (isRecordUpdated) {
			productCopy.setLastUpdatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
			productCopy.setLastUpdatedByProcess(
					Utility.hasValue(request.getLastUpdatedByProcess()) ? request.getLastUpdatedByProcess() : process);
			productCopy.setLastUpdatedByUser(getLoggedInUserName());
			products.add(productCopy);
		}
	}

	private String getLoggedInUserName() {
		return securityContextUtil.getUserName();
	}

}
