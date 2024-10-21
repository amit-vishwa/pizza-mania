package com.pizzamania.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzamania.dto.PurchaseDetailDto;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.service.PurchaseDetailManagementService;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Resource;
import com.pizzamania.utility.Utility;

@RestController
@RequestMapping("/api/auth/purchase-detail")
public class PurchaseDetailManagementController {

	private static Logger log = LoggerFactory.getLogger(PurchaseDetailManagementController.class);

	@Autowired
	PurchaseDetailManagementService purchaseDetailManagementService;

	@Autowired
	MessageConfiguration messageConfig;

	@PostMapping("/add")
	@PreAuthorize("hasRole('ROLE_USER')")
	public Resource<List<PurchaseDetailDto>> addPurchaseDetails(@RequestBody List<PurchaseDetailDto> requestList) {
		try {
			log.info("Adding provided purchaseDetails");
			if (!Utility.listHasValues(requestList)) {
				throw new EntityMissingException("Request cannot be null!");
			}
			List<PurchaseDetailDto> responseList = purchaseDetailManagementService.addPurchaseDetails(requestList);
			if (responseList == null) {
				return new Resource<List<PurchaseDetailDto>>(null, null, messageConfig.getMessage("badInput"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<List<PurchaseDetailDto>>(null, responseList, messageConfig.getMessage("success"),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while creating purchaseDetails!", e);
			return Utility.getExceptionMessage(e);
		}
	}

}
