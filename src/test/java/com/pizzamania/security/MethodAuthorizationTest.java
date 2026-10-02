package com.pizzamania.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.PostMapping;

import com.pizzamania.controller.CartManagementController;
import com.pizzamania.controller.PurchaseManagementController;
import com.pizzamania.controller.UserManagementController;
import com.pizzamania.security.repository.RoleRepository;
import com.pizzamania.security.repository.UserRepository;
import com.pizzamania.security.service.UserDetailsImpl;
import com.pizzamania.service.PurchaseManagementService;

import jakarta.validation.constraints.Positive;

@SpringBootTest
@ActiveProfiles("test")
class MethodAuthorizationTest {

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private RoleRepository roleRepository;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private PurchaseManagementService purchaseManagementService;

	@Autowired
	private UserManagementController userManagementController;

	@Autowired
	private PurchaseManagementController purchaseManagementController;

	@Autowired
	private CartManagementController cartManagementController;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

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

	@Test
	void userCanCancelOwnPurchase() {
		authenticateUser(30);

		assertDoesNotThrow(() -> purchaseManagementController.cancelPurchase(30, 11));
	}

	@Test
	void userCannotCancelAnotherUsersPurchase() {
		authenticateUser(30);

		assertThrows(AccessDeniedException.class,
				() -> purchaseManagementController.cancelPurchase(31, 11));
	}

	@Test
	void purchaseMutationsArePostOnlyAndIdentifiersMustBePositive() throws Exception {
		Method cancel = PurchaseManagementController.class.getMethod("cancelPurchase", Integer.class, Integer.class);
		Method complete = PurchaseManagementController.class.getMethod("completePurchase");

		assertTrue(cancel.isAnnotationPresent(PostMapping.class));
		assertTrue(complete.isAnnotationPresent(PostMapping.class));
		assertEquals(2, cancel.getParameterCount());
		assertTrue(cancel.getParameters()[0].isAnnotationPresent(Positive.class));
		assertTrue(cancel.getParameters()[1].isAnnotationPresent(Positive.class));
	}

	private void authenticateUser(Integer userId) {
		UserDetailsImpl principal = new UserDetailsImpl(userId, "customer@example.com", "customer@example.com",
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
	}
}
