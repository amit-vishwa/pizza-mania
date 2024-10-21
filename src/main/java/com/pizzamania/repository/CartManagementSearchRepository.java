package com.pizzamania.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.pizzamania.model.Cart;
import com.pizzamania.utility.GenericDao;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class CartManagementSearchRepository extends GenericDao<Cart, Integer> {

	public CartManagementSearchRepository(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		setEntityManager(entityManager);
	}

	@Override
	protected List<Predicate> applyCriteria(SearchCriteria<Cart> searchCriteria, CriteriaBuilder cb, Root<Cart> root,
			List<Predicate> predicates) {
		Cart domain = searchCriteria.getModel();
		if (domain.getCartId() != null) {
			predicates.add(cb.equal(root.get("cartId"), domain.getCartId()));
		}
		if (domain.getUserId() != null) {
			predicates.add(cb.equal(root.get("userId"), domain.getUserId()));
		}
		if (domain.getPurchaseId() != null) {
			predicates.add(cb.equal(root.get("purchaseId"), domain.getPurchaseId()));
		}
		if (domain.getStatus() != null) {
			predicates.add(cb.equal(root.get("status"), domain.getStatus()));
		}
		if (domain.getStatusTimestamp() != null) {
			predicates.add(cb.equal(root.get("statusTimestamp"), domain.getStatusTimestamp()));
		}
		if (domain.getRecordStatus() != null) {
			predicates.add(cb.equal(root.get("recordStatus"), domain.getRecordStatus()));
		}
		if (domain.getCreatedOnTimestamp() != null) {
			predicates.add(cb.equal(root.get("createdOnTimestamp"), domain.getCreatedOnTimestamp()));
		}
		if (domain.getCreatedByUser() != null) {
			predicates.add(cb.equal(root.get("createdByUser"), domain.getCreatedByUser()));
		}
		if (domain.getCreatedByProcess() != null) {
			predicates.add(cb.equal(root.get("createdByProcess"), domain.getCreatedByProcess()));
		}
		if (domain.getLastUpdatedOnTimestamp() != null) {
			predicates.add(cb.equal(root.get("lastUpdatedOnTimestamp"), domain.getLastUpdatedOnTimestamp()));
		}
		if (domain.getLastUpdatedByUser() != null) {
			predicates.add(cb.equal(root.get("lastUpdatedByUser"), domain.getLastUpdatedByUser()));
		}
		if (domain.getLastUpdatedByProcess() != null) {
			predicates.add(cb.equal(root.get("lastUpdatedByProcess"), domain.getLastUpdatedByProcess()));
		}
		return predicates;
	}

	@Override
	protected List<Order> applySort(SearchCriteria<Cart> searchCriteria, CriteriaBuilder cb, Root<Cart> root,
			List<Order> orders) {
		orders = Utility.populateOrderList(searchCriteria, cb, root);
		// when no sorting criteria is provided, then by default below will be added
		if (!Utility.hasEntries(orders)) {
			orders.add(cb.asc(root.get("cartId")));
		}
		return orders;
	}

}
