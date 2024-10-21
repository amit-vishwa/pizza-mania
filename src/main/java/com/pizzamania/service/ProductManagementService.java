package com.pizzamania.service;

import java.util.List;

import com.pizzamania.dto.ProductDto;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.exception.ResourceAlreadyExistsException;
import com.pizzamania.exception.ResourceNotFoundException;
import com.pizzamania.model.Product;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;

public interface ProductManagementService {

	Page<ProductDto> searchProducts(SearchCriteria<ProductDto> searchCriteria);

	List<ProductDto> addProducts(List<ProductDto> requestList)
			throws EntityMissingException, ResourceAlreadyExistsException;

	List<ProductDto> editProducts(List<ProductDto> requestList)
			throws EntityMissingException, ResourceNotFoundException;

	List<ProductDto> deleteProducts(List<ProductDto> requestList)
			throws EntityMissingException, ResourceNotFoundException;

	List<ProductDto> saveAllProducts(List<Product> productList);

}
