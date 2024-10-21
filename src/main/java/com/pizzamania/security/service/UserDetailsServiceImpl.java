package com.pizzamania.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.security.model.User;
import com.pizzamania.security.repository.UserRepository;
import com.pizzamania.utility.Utility;

import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUserNameAndRecordStatus(username, GlobalConstants.ACTIVE_RECORD_STATUS)
				.orElse(null);
		return Utility.hasValue(user) ? UserDetailsImpl.build(user) : null;
	}
}
