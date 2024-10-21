package com.pizzamania.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.pizzamania.security.model.User;
import com.pizzamania.utility.GenericDao;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class UserManagementSearchRepository extends GenericDao<User, Integer> {

	public UserManagementSearchRepository(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		setEntityManager(entityManager);
	}

	@Override
	protected List<Predicate> applyCriteria(SearchCriteria<User> searchCriteria, CriteriaBuilder cb, Root<User> root,
			List<Predicate> predicates) {
		User domain = searchCriteria.getModel();
		if (domain.getUserId() != null) {
			predicates.add(cb.equal(root.get("userId"), domain.getUserId()));
		}
		if (domain.getUserName() != null) {
			predicates.add(cb.equal(root.get("userName"), domain.getUserName()));
		}
		if (domain.getUserType() != null) {
			predicates.add(cb.equal(root.get("userType"), domain.getUserType()));
		}
		if (domain.getRole() != null) {
			predicates.add(cb.equal(root.get("role"), domain.getRole()));
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
	protected List<Order> applySort(SearchCriteria<User> searchCriteria, CriteriaBuilder cb, Root<User> root,
			List<Order> orders) {
		orders = Utility.populateOrderList(searchCriteria, cb, root);
		// when no sorting criteria is provided, then by default below will be added
		if (!Utility.hasEntries(orders)) {
			orders.add(cb.asc(root.get("userId")));
		}
		return orders;
	}

}
