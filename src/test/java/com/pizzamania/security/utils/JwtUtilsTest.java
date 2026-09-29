package com.pizzamania.security.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.nimbusds.jwt.JWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtUtilsTest {

	private static final String APPLICATION_SECRET =
			"0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
	private static final String ATTACKER_SECRET =
			"abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789";

	private JwtUtils jwtUtils;

	@BeforeEach
	void setUp() {
		jwtUtils = new JwtUtils(mock(JwtDecoder.class));
		ReflectionTestUtils.setField(jwtUtils, "jwtSecret", APPLICATION_SECRET);
		ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3_600_000);
	}

	@Test
	void acceptsLocallyIssuedTokenWithValidSignature() throws Exception {
		String token = signedToken(APPLICATION_SECRET, "customer@example.com");

		JWT parsed = jwtUtils.getJwtFromHeader("Bearer " + token);

		assertEquals("customer@example.com", jwtUtils.getUserNameFromJwtToken(parsed));
	}

	@Test
	void rejectsTokenSignedWithAttackerControlledKey() {
		String forgedToken = signedToken(ATTACKER_SECRET, "admin@example.com");

		assertThrows(BadCredentialsException.class,
				() -> jwtUtils.getJwtFromHeader("Bearer " + forgedToken));
	}

	@Test
	void rejectsUnsignedToken() {
		String unsignedToken = "eyJhbGciOiJub25lIn0.eyJ0b2tlbl91c2UiOiJpZCIsInVzZXJuYW1lIjoiYWRtaW5AZXhhbXBsZS5jb20iLCJleHAiOjQxMDI0NDQ4MDB9.";

		assertThrows(BadCredentialsException.class,
				() -> jwtUtils.getJwtFromHeader("Bearer " + unsignedToken));
	}

	private String signedToken(String secret, String username) {
		SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		return Jwts.builder().subject(username).claim("token_use", "id").claim("username", username)
				.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 3_600_000)).signWith(key)
				.compact();
	}
}
