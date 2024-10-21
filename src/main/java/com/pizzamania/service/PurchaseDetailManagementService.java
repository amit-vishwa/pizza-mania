package com.pizzamania.service;

import java.util.List;

import com.pizzamania.dto.PurchaseDetailDto;
import com.pizzamania.exception.CRUDFailureException;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.exception.ResourceNotFoundException;
import com.pizzamania.model.PurchaseDetail;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;

public interface PurchaseDetailManagementService {

	Page<PurchaseDetailDto> searchPurchaseDetails(SearchCriteria<PurchaseDetailDto> searchCriteria);

	List<PurchaseDetailDto> addPurchaseDetails(List<PurchaseDetailDto> requestList)
			throws CRUDFailureException, EntityMissingException, ResourceNotFoundException;

	List<PurchaseDetailDto> saveAllPurchaseDetails(List<PurchaseDetail> purchaseList);

}
