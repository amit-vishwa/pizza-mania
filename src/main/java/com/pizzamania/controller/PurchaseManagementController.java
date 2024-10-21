package com.pizzamania.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.service.PurchaseManagementService;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Resource;
import com.pizzamania.utility.Utility;

@RestController
@RequestMapping("/api/auth/purchase")
public class PurchaseManagementController {

	private static Logger log = LoggerFactory.getLogger(PurchaseManagementController.class);

	@Autowired
	PurchaseManagementService purchaseManagementService;

	@Autowired
	MessageConfiguration messageConfig;

	@GetMapping("/{userId}/cancel/{purchaseId}")
	@PreAuthorize("hasRole('ROLE_USER') and #userId == principal.id")
	public Resource<String> cancelPurchase(@PathVariable Integer userId, @PathVariable Integer purchaseId) {
		try {
			log.info("Cancelling purchase for id " + purchaseId);
			if (!Utility.hasValue(userId) || !Utility.hasValue(purchaseId)) {
				throw new EntityMissingException("User id and purchase id cannot be null");
			}
			return new Resource<String>(null, purchaseManagementService.cancelPurchase(userId, purchaseId),
					messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while cancelling purchases!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@GetMapping("/complete")
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	public Resource<String> completePurchase() {
		try {
			log.info("Marking purchase orders with more than 30mins as complete");
			return new Resource<String>(null, purchaseManagementService.completePurchase(),
					messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while cancelling purchases!", e);
			return Utility.getExceptionMessage(e);
		}
	}

}
