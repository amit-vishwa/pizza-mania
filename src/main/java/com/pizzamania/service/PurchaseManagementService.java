package com.pizzamania.service;

import java.util.List;

import com.pizzamania.dto.PurchaseDto;
import com.pizzamania.model.Purchase;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;

public interface PurchaseManagementService {

	Page<PurchaseDto> searchPurchases(SearchCriteria<PurchaseDto> searchCriteria);

	List<PurchaseDto> addPurchases(List<PurchaseDto> requestList);

	String cancelPurchase(Integer userId, Integer purchaseId);

	String completePurchase();

	List<PurchaseDto> saveAllPurchases(List<Purchase> purchaseList);

}
