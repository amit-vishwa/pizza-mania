package com.pizzamania.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.security.dto.RoleDto;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.security.enums.AppRole;
import com.pizzamania.service.UserManagementService;
import com.pizzamania.utility.MessageConfiguration;

@ExtendWith(MockitoExtension.class)
class PublicControllerTest {

	@Mock
	private MessageConfiguration messageConfig;

	@Mock
	private UserManagementService userManagementService;

	@InjectMocks
	private PublicController controller;

	@Test
	void publicRegistrationCannotChoosePrivilegedRole() throws Exception {
		RoleDto requestedRole = new RoleDto();
		requestedRole.setRoleName(AppRole.ROLE_ADMIN);
		UserDto request = new UserDto();
		request.setUserName("customer@example.com");
		request.setUserPass("valid-password");
		request.setRole(requestedRole);
		when(userManagementService.addUsers(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		controller.registerUser(request);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<UserDto>> usersCaptor = ArgumentCaptor.forClass(List.class);
		verify(userManagementService).addUsers(usersCaptor.capture());
		UserDto registeredUser = usersCaptor.getValue().get(0);
		assertEquals(AppRole.ROLE_USER, registeredUser.getRole().getRoleName());
		assertEquals(GlobalConstants.EXTERNAL_USER, registeredUser.getUserType());
	}
}
