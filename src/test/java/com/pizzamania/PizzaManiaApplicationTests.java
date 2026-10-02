package com.pizzamania;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.pizzamania.security.repository.RoleRepository;
import com.pizzamania.security.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class PizzaManiaApplicationTests {

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private RoleRepository roleRepository;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
