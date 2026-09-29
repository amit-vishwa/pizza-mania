package com.pizzamania.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import com.pizzamania.controller.CartManagementController;
import com.pizzamania.controller.PurchaseManagementController;
import com.pizzamania.controller.UserManagementController;
import com.pizzamania.security.repository.RoleRepository;
import com.pizzamania.security.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class MethodAuthorizationTest {

	@MockBean
	private JwtDecoder jwtDecoder;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private UserRepository userRepository;

	@Autowired
	private UserManagementController userManagementController;

	@Autowired
	private PurchaseManagementController purchaseManagementController;

	@Autowired
	private CartManagementController cartManagementController;

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanReachAdminEndpoint() {
		assertDoesNotThrow(() -> userManagementController.searchUsers(null));
	}

	@Test
	@WithMockUser(roles = "MANAGER")
	void managerCanReachManagerEndpoint() {
		assertDoesNotThrow(() -> purchaseManagementController.completePurchase());
	}

	@Test
	@WithMockUser(roles = "USER")
	void userCanReachUserEndpoint() {
		assertDoesNotThrow(() -> cartManagementController.searchItems(null));
	}

	@Test
	@WithMockUser(roles = "USER")
	void userCannotReachAdminEndpoint() {
		assertThrows(AccessDeniedException.class, () -> userManagementController.searchUsers(null));
	}
}
