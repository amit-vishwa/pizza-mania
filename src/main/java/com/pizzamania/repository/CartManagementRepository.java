package com.pizzamania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzamania.model.Cart;

@Repository
public interface CartManagementRepository extends JpaRepository<Cart, Integer> {

}
