package com.pizzamania.security.dto;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.nimbusds.jwt.JWT;

public class AuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;
	private JWT idToken;
	private UserDto principal;

	public AuthenticationToken(Collection<? extends GrantedAuthority> authorities, JWT idToken, UserDto userDetails) {
		super(authorities);
		this.setIdToken(idToken);
		this.principal = userDetails;
		super.setAuthenticated(true);
	}

	public AuthenticationToken(JWT idToken) {
		super(Arrays.asList());
		this.setIdToken(idToken);
		this.principal = null;
	}

	public AuthenticationToken() {
		super(null);
	}

	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	public UserDto setPrincipal(UserDto principal) {
		return this.principal = principal;
	}

	public JWT getIdToken() {
		return this.idToken;
	}

	public void setIdToken(JWT idToken) {
		this.idToken = idToken;
	}

}
