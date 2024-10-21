package com.pizzamania.service;

import java.util.List;

import com.pizzamania.dto.CartDto;
import com.pizzamania.model.Cart;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;

public interface CartManagementService {

	Page<CartDto> searchItems(SearchCriteria<CartDto> searchCriteria);

	List<CartDto> addItems(List<CartDto> requestList);

	List<CartDto> saveAllCarts(List<Cart> cartList);

}
