package com.pizzamania.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.security.enums.AppRole;
import com.pizzamania.security.filter.AuthTokenFilter;
import com.pizzamania.security.model.Role;
import com.pizzamania.security.model.User;
import com.pizzamania.security.repository.RoleRepository;
import com.pizzamania.security.repository.UserRepository;
import com.pizzamania.security.utils.AuthEntryPointJwt;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

	@Autowired
	AuthEntryPointJwt unauthorizedHandler;

	@Autowired
	AuthTokenFilter authTokenFilter;

	@Bean
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		// Csrf configuration
		http.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers("/api/test/**", "/api/public/**"));
		http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));
		// Auth filter added
		http.addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class);
		http.httpBasic(withDefaults());
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			createRequiredUsers(roleRepository, userRepository, passwordEncoder, GlobalConstants.REQUIRED_USER_USER,
					AppRole.ROLE_USER);
			createRequiredUsers(roleRepository, userRepository, passwordEncoder, GlobalConstants.REQUIRED_USER_MANAGER,
					AppRole.ROLE_MANAGER);
			createRequiredUsers(roleRepository, userRepository, passwordEncoder, GlobalConstants.REQUIRED_USER_ADMIN,
					AppRole.ROLE_ADMIN);
		};
	}

	private Role getRole(RoleRepository roleRepository, AppRole roleName) {
		return roleRepository.findByRoleName(roleName)
				.orElseGet(() -> roleRepository.save(new Role(roleName, GlobalConstants.ACTIVE_RECORD_STATUS,
						GlobalConstants.COMMAND_LINE_RUNNER_PROCESS, GlobalConstants.COMMAND_LINE_RUNNER_PROCESS,
						new Timestamp(System.currentTimeMillis()))));
	}

	private void createRequiredUsers(RoleRepository roleRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder, String user, AppRole roleName) {
		String userNameExt = "@example.com";
		String userPassExt = "password";
		if (!userRepository.existsByUserName(user.concat(userNameExt))) {
			User admin = new User();
			admin.setUserName(user.concat(userNameExt));
			admin.setUserPass(passwordEncoder.encode(user.concat(userPassExt)));
			admin.setUserType(GlobalConstants.INTERNAL_USER);
			admin.setRole(getRole(roleRepository, roleName));
			admin.setRecordStatus(GlobalConstants.ACTIVE_RECORD_STATUS);
			admin.setCreatedByUser(GlobalConstants.COMMAND_LINE_RUNNER_PROCESS);
			admin.setCreatedByProcess(GlobalConstants.COMMAND_LINE_RUNNER_PROCESS);
			admin.setCreatedOnTimestamp(new Timestamp(System.currentTimeMillis()));
			userRepository.save(admin);
		}
	}

}
