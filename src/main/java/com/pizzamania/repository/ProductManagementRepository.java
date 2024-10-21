package com.pizzamania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzamania.model.Product;

@Repository
public interface ProductManagementRepository extends JpaRepository<Product, Integer> {

}
