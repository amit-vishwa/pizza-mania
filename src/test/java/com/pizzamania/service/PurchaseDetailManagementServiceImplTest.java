package com.pizzamania.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pizzamania.model.PurchaseDetail;

class PurchaseDetailManagementServiceImplTest {

	@Test
	void totalsMoneyWithoutFloatingPointRounding() {
		PurchaseDetail firstItem = new PurchaseDetail();
		firstItem.setTotalProductCost(new BigDecimal("0.10"));
		PurchaseDetail secondItem = new PurchaseDetail();
		secondItem.setTotalProductCost(new BigDecimal("0.20"));

		BigDecimal total = PurchaseDetailManagementServiceImpl
				.totalPurchaseAmount(List.of(firstItem, secondItem));

		assertEquals(new BigDecimal("0.30"), total);
	}
}
