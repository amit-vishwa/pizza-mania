package com.pizzamania.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzamania.security.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByUserName(String user);

	Optional<User> findByUserNameAndRecordStatus(String username, String string);

	Optional<User> findUserIdByUserName(String username);

}
