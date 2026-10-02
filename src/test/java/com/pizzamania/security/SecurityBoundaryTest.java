package com.pizzamania.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pizzamania.security.repository.RoleRepository;
import com.pizzamania.security.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityBoundaryTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private RoleRepository roleRepository;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void publicHealthEndpointRemainsAccessible() throws Exception {
		mockMvc.perform(get("/api/test"))
				.andExpect(status().isOk())
				.andExpect(cookie().doesNotExist("JSESSIONID"));
	}

	@Test
	void authenticatedEndpointRejectsMissingBearerToken() throws Exception {
		mockMvc.perform(get("/api/auth/user/details"))
				.andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist("JSESSIONID"));
	}

	@Test
	void basicAuthenticationCannotBypassBearerAuthentication() throws Exception {
		mockMvc.perform(get("/api/auth/user/details").with(httpBasic("admin@example.com", "password")))
				.andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist("JSESSIONID"));
	}

	@Test
	void unclassifiedRoutesAreDeniedByDefault() throws Exception {
		mockMvc.perform(get("/not-an-api"))
				.andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist("JSESSIONID"));
	}
}
