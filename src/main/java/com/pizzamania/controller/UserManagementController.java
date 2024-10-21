package com.pizzamania.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.service.UserManagementService;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.Resource;
import com.pizzamania.utility.Utility;

@RestController
@RequestMapping("/api/auth/user")
public class UserManagementController {

	private static Logger log = LoggerFactory.getLogger(UserManagementController.class);

	@Autowired
	UserManagementService userManagementService;

	@Autowired
	MessageConfiguration messageConfig;

	@GetMapping("/details")
	public Resource<UserDetails> getUserDetails() {
		try {
			log.info("Searching user details");
			return new Resource<UserDetails>(null, userManagementService.getUserDetails(),
					messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while deleting users!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@DeleteMapping("/delete")
	public Resource<String> deleteUserDetails(@RequestBody UserDto request) {
		try {
			log.info("Deleting user details");
			if (!Utility.hasValue(request) || !Utility.hasValue(request.getUserPass())) {
				throw new EntityMissingException("User password is missing from the request!");
			}
			String response = userManagementService.deleteUserDetails(request.getUserPass());
			if (response == null) {
				return new Resource<String>(null, response, messageConfig.getMessage("failedToDeleteData"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<String>(null, response, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while deleting users!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@PostMapping("/search")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Resource<Page<UserDto>> searchUsers(@RequestBody Resource<UserDto> request) {
		try {
			log.info("Searching users");
			if (!Utility.hasCriteria(request)) {
				return new Resource<Page<UserDto>>(null, null, messageConfig.getMessage("missingSearchCriteria"),
						HttpStatus.BAD_REQUEST);
			}
			Page<UserDto> response = userManagementService.searchUsers(request.getSearchCriteria());
			if (!Utility.hasEntries(response)) {
				return new Resource<Page<UserDto>>(null, null, messageConfig.getMessage("notFoundException"),
						HttpStatus.NOT_FOUND);
			}
			return new Resource<Page<UserDto>>(null, response, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while searching users!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Resource<List<UserDto>> addUsers(@RequestBody List<UserDto> requestList) {
		try {
			log.info("Adding provided users");
			if (!Utility.listHasValues(requestList)) {
				throw new EntityMissingException("Request cannot be null!");
			}
			List<UserDto> responseList = userManagementService.addUsers(requestList);
			if (responseList == null) {
				return new Resource<List<UserDto>>(null, null, messageConfig.getMessage("badInput"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<List<UserDto>>(null, responseList, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while creating users!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@PutMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Resource<List<UserDto>> editUsers(@RequestBody List<UserDto> requestList) {
		try {
			log.info("Updating provided users");
			if (!Utility.listHasValues(requestList)) {
				throw new EntityMissingException("Request cannot be null!");
			}
			List<UserDto> responseList = userManagementService.editUsers(requestList);
			if (responseList == null) {
				return new Resource<List<UserDto>>(null, null, messageConfig.getMessage("badInput"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<List<UserDto>>(null, responseList, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while updating users!", e);
			return Utility.getExceptionMessage(e);
		}
	}

	@DeleteMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Resource<List<UserDto>> deleteUsers(@RequestBody List<UserDto> requestList) {
		try {
			log.info("Deleting provided users");
			if (!Utility.listHasValues(requestList)) {
				throw new EntityMissingException("Request cannot be null!");
			}
			List<UserDto> responseList = userManagementService.deleteUsers(requestList);
			if (responseList == null) {
				return new Resource<List<UserDto>>(null, null, messageConfig.getMessage("badInput"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<List<UserDto>>(null, responseList, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while deleting users!", e);
			return Utility.getExceptionMessage(e);
		}
	}

}
