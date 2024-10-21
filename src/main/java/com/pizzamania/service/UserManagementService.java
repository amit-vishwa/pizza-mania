package com.pizzamania.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.pizzamania.exception.CRUDFailureException;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.exception.ResourceAlreadyExistsException;
import com.pizzamania.exception.ResourceNotFoundException;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.security.response.LoginResponse;
import com.pizzamania.utility.Page;
import com.pizzamania.utility.SearchCriteria;

public interface UserManagementService {

	Page<UserDto> searchUsers(SearchCriteria<UserDto> searchCriteria);

	List<UserDto> addUsers(List<UserDto> requestList) throws CRUDFailureException, ResourceNotFoundException,
			EntityMissingException, ResourceAlreadyExistsException;

	List<UserDto> editUsers(List<UserDto> requestList)
			throws ResourceNotFoundException, EntityMissingException, CRUDFailureException;

	List<UserDto> deleteUsers(List<UserDto> requestList)
			throws ResourceNotFoundException, EntityMissingException, CRUDFailureException;

	UserDetails getUserDetails();

	String deleteUserDetails(String password) throws CRUDFailureException;

	Integer getLoggedInUserId();

	LoginResponse authenticateUser(UserDto request);

}
