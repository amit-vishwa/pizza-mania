package com.pizzamania.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.enumeration.ProcessEnum;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.security.response.LoginResponse;
import com.pizzamania.service.UserManagementService;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Resource;
import com.pizzamania.utility.Utility;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/public")
public class PublicController {

	private static Logger log = LoggerFactory.getLogger(PublicController.class);

	@Autowired
	private MessageConfiguration messageConfig;

	@Autowired
	private UserManagementService userManagementService;

	@GetMapping("/csrf-token")
	public Resource<CsrfToken> getCsrfToken(HttpServletRequest request) {
		log.info("Generating csrf token...");
		return new Resource<CsrfToken>(null, (CsrfToken) request.getAttribute(CsrfToken.class.getName()),
				messageConfig.getMessage("success"), HttpStatus.OK);
	}

	@PostMapping("/sign-in")
	public Resource<LoginResponse> authenticateUser(@RequestBody UserDto request) {
		try {
			log.info("Signing in user...");
			if (!(Utility.hasValue(request) && Utility.hasValue(request.getUserName())
					&& Utility.hasValue(request.getUserPass()))) {
				throw new EntityMissingException("Username and password cannot be null!");
			}
			LoginResponse response = userManagementService.authenticateUser(request);
			if (response == null) {
				return new Resource<LoginResponse>(null, null, messageConfig.getMessage("unauthorizedException"),
						HttpStatus.UNAUTHORIZED);
			}
			return new Resource<LoginResponse>(null, response, messageConfig.getMessage("success"), HttpStatus.OK);
		} catch (Exception exception) {
			log.error("Exception occurred while authenticating credentials {}", exception.getMessage());
			return Utility.getExceptionMessage(exception);
		}
	}

	@PostMapping("/sign-up")
	public Resource<String> registerUser(@RequestBody UserDto request) {
		try {
			log.info("Signing up user...");
			if (!(Utility.hasValue(request) && Utility.hasValue(request.getUserName())
					&& Utility.hasValue(request.getUserPass()) && request.getUserPass().length() >= 8)) {
				return new Resource<String>(null,
						"Username and password with at least 8 characters should be provided!",
						messageConfig.getMessage("badRequest"), HttpStatus.BAD_REQUEST);
			}
			List<UserDto> requestList = new ArrayList<UserDto>();
			request.setUserType(GlobalConstants.EXTERNAL_USER);
			request.setCreatedByUser(request.getUserName());
			request.setCreatedByProcess(ProcessEnum.REGISTER_USERS.getProcessCode());
			requestList.add(request);
			List<UserDto> responseList = userManagementService.addUsers(requestList);
			if (responseList == null) {
				return new Resource<String>(null, null, messageConfig.getMessage("failedToCreateData"),
						HttpStatus.BAD_REQUEST);
			}
			return new Resource<String>(null, "User registered successfully!", messageConfig.getMessage("success"),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while registering users!", e);
			return Utility.getExceptionMessage(e);
		}
	}

}
