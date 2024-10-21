package com.pizzamania.security.filter;

import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.gson.Gson;
import com.nimbusds.jwt.JWT;
import com.pizzamania.security.service.UserDetailsServiceImpl;
import com.pizzamania.security.utils.JwtUtils;
import com.pizzamania.utility.MessageConfiguration;
import com.pizzamania.utility.Resource;
import com.pizzamania.utility.Utility;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private MessageConfiguration messageConfig;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		logger.info("AuthTokenFilter called for URI: {}", request.getRequestURI());

		try {

			JWT authToken = jwtUtils.getJwtFromHeader(request.getHeader("Authorization"));
			String username = jwtUtils.getUserNameFromJwtToken(authToken);
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			if (!Utility.hasValue(userDetails)) {
				logger.error("Active user does not exist with username " + username);
				createUnauthorizedResponse(response);
				return;
			}

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
					null, userDetails.getAuthorities());
			logger.info("Roles from JWT: {}", userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);

		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
			createUnauthorizedResponse(response);
			return;
		}

	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		boolean shouldNotFilter = request.getRequestURI().toLowerCase().contains("/api/auth/") ? false : true;
		return shouldNotFilter;
	}

	private void createUnauthorizedResponse(HttpServletResponse response) throws IOException {
		Gson gson = new Gson();
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("WWW-Authenticate", "Bearer");
		response.setStatus(401);
		Resource<String> responseBody = new Resource<String>(null, null,
				messageConfig.getMessage("unauthorizedException"));
		String responseString = gson.toJson(responseBody.getBody());
		out.print(responseString);
		out.flush();
	}

}
