package com.pizzamania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzamania.model.PurchaseDetail;

@Repository
public interface PurchaseDetailManagementRepository extends JpaRepository<PurchaseDetail, Integer> {

}
