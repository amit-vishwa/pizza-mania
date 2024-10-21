package com.pizzamania.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzamania.dto.ProductDto;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.service.ProductManagementService;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.Resource;
import com.pizzamania.utility.Utility;

@RestController
@RequestMapping("/api/auth/product")
public class ProductManagementController {

	private static Logger log = LoggerFactory.getLogger(ProductManagementController.class);

	@Autowired
	ProductManagementService productManagementService;

	@Autowired
	MessageConfiguration messageConfig;

	@PostMapping("/search")
	@PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_USER')")
	public Resource<Page<ProductDto>> searchProducts(@RequestBody Resource<ProductDto> request) {
		try {
			log.info("Searching products");
			if (!Utility.hasCriteria(request)) {
				return new Resource<Page<ProductDto>>(null, null, messageConfig.getMessage("missingSearchCriteria"),
						HttpStatus.BAD_REQUEST);
			}
			Page<ProductDto> response = productManagementService.searchProducts(request.getSearchCriteria());
			if (!Utility.hasEntries(response)) {
				return new Resource<Page<ProductDto>>(null, null, messageConfig.getMessage("notFoundException"),
						HttpStatus.NOT_FOUND);
			}
			return new Resource<Page<ProductDto>>(null, response, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while searching products!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	public Resource<List<ProductDto>> addProducts(@RequestBody List<ProductDto> requestList) {
		try {
			log.info("Adding provided products");
			if (!Utility.listHasValues(requestList)) {
				throw new EntityMissingException("Request cannot be null!");
			}
			List<ProductDto> responseList = productManagementService.addProducts(requestList);
			if (responseList == null) {
				return new Resource<List<ProductDto>>(null, null, messageConfig.getMessage("badInput"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<List<ProductDto>>(null, responseList, messageConfig.getMessage("success"),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while creating products!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@PutMapping
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	public Resource<List<ProductDto>> editProducts(@RequestBody List<ProductDto> requestList) {
		try {
			log.info("Updating provided products");
			if (!Utility.listHasValues(requestList)) {
				throw new EntityMissingException("Request cannot be null!");
			}
			List<ProductDto> responseList = productManagementService.editProducts(requestList);
			if (responseList == null) {
				return new Resource<List<ProductDto>>(null, null, messageConfig.getMessage("badInput"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<List<ProductDto>>(null, responseList, messageConfig.getMessage("success"),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while updating products!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@DeleteMapping
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	public Resource<List<ProductDto>> deleteProducts(@RequestBody List<ProductDto> requestList) {
		try {
			log.info("Deleting provided products");
			if (!Utility.listHasValues(requestList)) {
				throw new EntityMissingException("Request cannot be null!");
			}
			List<ProductDto> responseList = productManagementService.deleteProducts(requestList);
			if (responseList == null) {
				return new Resource<List<ProductDto>>(null, null, messageConfig.getMessage("badInput"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<List<ProductDto>>(null, responseList, messageConfig.getMessage("success"),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while deleting products!", e);
			return Utility.getExceptionMessage(e);
		}
	}

}
