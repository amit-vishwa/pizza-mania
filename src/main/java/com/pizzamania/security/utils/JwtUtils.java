package com.pizzamania.security.utils;

import java.security.Key;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.utility.Utility;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${spring.app.jwtSecret}")
	private String jwtSecret;

	@Value("${spring.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	public JWT getJwtFromHeader(String bearerToken) {
		logger.info("Authorization Header: {}", bearerToken);
		return getJWTToken(bearerToken);
	}

	private JWT getJWTToken(String headerValue) {
		if (!isValidFormat(headerValue)) {
			throw new BadCredentialsException("Not a valid token");
		}
		String[] splitHeaderValue = headerValue.split(" ");
		String idTokenString = splitHeaderValue[1];
		JWT idToken = null;
		try {
			idToken = JWTParser.parse(idTokenString);
			if (isTokenExpired(idToken, idTokenString)) {
				throw new BadCredentialsException("Token expired");
			}
		} catch (ParseException e) {
			throw new BadCredentialsException("Not a valid token", e);
		}
		return idToken;
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
			Long expirationTimeInMs = claims.getExpirationTime().getTime();
			if (Utility.hasValue(expirationTimeInMs) && expirationTimeInMs > currentTimeInMs) {
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

	private Key key() {
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

	public JWT validateJwtToken(String authToken) {
		try {
			JWT token = JWTParser.parse(authToken);
			if (Utility.hasValue(authToken)) {
				return token;
			}
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		} catch (ParseException e) {
			logger.error("JWT token string parse failed: {}", e.getMessage());
		}
		return null;
	}

}
