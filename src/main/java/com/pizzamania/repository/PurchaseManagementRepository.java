package com.pizzamania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzamania.model.Purchase;

@Repository
public interface PurchaseManagementRepository extends JpaRepository<Purchase, Integer> {

}
