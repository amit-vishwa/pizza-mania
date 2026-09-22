package com.pizzamania;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import com.pizzamania.security.repository.RoleRepository;
import com.pizzamania.security.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class PizzaManiaApplicationTests {

	@MockBean
	private JwtDecoder jwtDecoder;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
