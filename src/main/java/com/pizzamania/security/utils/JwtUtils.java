package com.pizzamania.security.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.utility.Utility;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${spring.app.jwtSecret}")
	private String jwtSecret;

	@Value("${spring.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	private final JwtDecoder jwtDecoder;

	public JwtUtils(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	public JWT getJwtFromHeader(String bearerToken) {
		return getJWTToken(bearerToken);
	}

	private JWT getJWTToken(String headerValue) {
		if (!isValidFormat(headerValue)) {
			throw new BadCredentialsException("Not a valid token");
		}
		String[] splitHeaderValue = headerValue.split(" ");
		String idTokenString = splitHeaderValue[1];
		try {
			JWT idToken = JWTParser.parse(idTokenString);
			verifySignature(idToken, idTokenString);
			if (isTokenExpired(idToken, idTokenString)) {
				throw new BadCredentialsException("Token expired");
			}
			return idToken;
		} catch (ParseException | io.jsonwebtoken.JwtException
				| org.springframework.security.oauth2.jwt.JwtException | IllegalArgumentException e) {
			throw new BadCredentialsException("Not a valid token", e);
		}
	}

	private void verifySignature(JWT token, String tokenValue) {
		String algorithm = token.getHeader().getAlgorithm().getName();
		if (algorithm != null && algorithm.matches("HS(256|384|512)")) {
			Jwts.parser().verifyWith(key()).build().parseSignedClaims(tokenValue);
			return;
		}
		if (!"RS256".equals(algorithm) || jwtDecoder.decode(tokenValue) == null) {
			throw new BadCredentialsException("Unsupported or unverifiable token");
		}
	}

	private boolean isValidFormat(String headerValue) {
		if (headerValue == null) {
			return false;
		}
		String[] splitHeaderValue = headerValue.trim().split(" ");
		if (splitHeaderValue.length != 2 || !splitHeaderValue[0].toLowerCase().equals("bearer")) {
			return false;
		}
		return true;
	}

	private boolean isTokenExpired(JWT idToken, String idTokenString) throws ParseException {
		if (Utility.hasValue(idTokenString)) {
			JWTClaimsSet claims = idToken.getJWTClaimsSet();
			Long currentTimeInMs = System.currentTimeMillis();
			Date expirationTime = claims.getExpirationTime();
			if (expirationTime != null && expirationTime.getTime() > currentTimeInMs) {
				return false;
			}
		}
		return true;
	}

	public String generateTokenFromUsername(UserDto userDetails) {
		return Jwts.builder().subject(userDetails.getUserName()).claim("token_use", "id")
				.claim("email", userDetails.getUserName()).claim("username", userDetails.getUserName())
				.issuedAt(new Date()).expiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(key())
				.compact();
	}

	private SecretKey key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}

	public String getUserNameFromJwtToken(JWT authToken) {
		String identifier = null;
		try {
			JWTClaimsSet claims = authToken.getJWTClaimsSet();
			String tokenType = claims.getStringClaim("token_use");
			if (Objects.equals(tokenType, "id")) {
				identifier = claims.getStringClaim("username");
			} else if (Objects.equals(tokenType, "access")) {
				String scope = claims.getStringClaim("scope");
				if (Utility.hasValue(scope) && scope.contains("/")) {
					String[] info = scope.split("/");
					identifier = info[1] + "@" + info[0];
				} else {
					identifier = scope;
				}
			}
		} catch (ParseException e) {
			logger.error("JWT token string parse failed: {}", e.getMessage());
		}
		logger.info("Identifier is " + identifier);
		return identifier;
	}

}
