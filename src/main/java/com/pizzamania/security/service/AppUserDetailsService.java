package com.pizzamania.security.service;

import java.text.ParseException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.service.UserManagementService;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.Utility;

@Service
public class AppUserDetailsService {

	private static Logger log = LoggerFactory.getLogger(AppUserDetailsService.class);

	@Autowired
	UserManagementService userManagementService;

	public UserDto getUserDetailsFromToken(JWT authToken) {
		try {
			JWTClaimsSet claims = authToken.getJWTClaimsSet();
			String tokenType = claims.getStringClaim("token_use");
			String identifier = null;
			if (Objects.equals(tokenType, "id")) {
				identifier = claims.getStringClaim("email");

			} else if (Objects.equals(tokenType, "access")) {
				String scope = claims.getStringClaim("scope");
				if (scope != null && scope.contains("/")) {
					String[] info = scope.split("/");
					identifier = info[1] + "@" + info[0];
				}
			}
			if (identifier != null) {
				UserDto user = new UserDto();
				user.setUserName(identifier);
				Page<UserDto> page = userManagementService.searchUsers(new SearchCriteria<UserDto>(user));
				if (Utility.hasEntries(page)) {
					log.info("User found with email " + identifier);
					return page.getPageEntries().get(0);
				}
				log.info("User not found in the database");
				throw new AccessDeniedException("User not found in the database");
			}
		} catch (ParseException e) {
			log.info("Exception occurred while parsing token");
			throw new BadCredentialsException("Token not parsed", e);
		}
		return null;
	}
}
