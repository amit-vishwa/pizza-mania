package com.pizzamania.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.dto.CartDto;
import com.pizzamania.service.CartManagementService;
import com.pizzamania.service.UserManagementService;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.Resource;
import com.pizzamania.utility.Utility;

@RestController
@RequestMapping("/api/auth/cart")
public class CartManagementController {

	private static Logger log = LoggerFactory.getLogger(CartManagementController.class);

	@Autowired
	CartManagementService cartManagementService;

	@Autowired
	UserManagementService userManagementService;

	@Autowired
	MessageConfiguration messageConfig;

	@PostMapping("/search")
	@PreAuthorize("hasRole('ROLE_USER')")
	public Resource<Page<CartDto>> searchItems(@RequestBody Resource<CartDto> request) {
		try {
			log.info("Searching cart items");
			if (!Utility.hasCriteria(request)) {
				return new Resource<Page<CartDto>>(null, null, messageConfig.getMessage("missingSearchCriteria"),
						HttpStatus.BAD_REQUEST);
			}
			request.getSearchCriteria().getModel().setUserId(userManagementService.getLoggedInUserId());
			request.getSearchCriteria().getModel().setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
			Page<CartDto> response = cartManagementService.searchItems(request.getSearchCriteria());
			if (!Utility.hasEntries(response)) {
				return new Resource<Page<CartDto>>(null, null, messageConfig.getMessage("notFoundException"),
						HttpStatus.NOT_FOUND);
			}
			return new Resource<Page<CartDto>>(null, response, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while searching cart items!", e);
			return Utility.getExceptionMessage(e);
		}
	}

}
