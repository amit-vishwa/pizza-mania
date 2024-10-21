package com.pizzamania.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.pizzamania.model.PurchaseDetail;
import com.pizzamania.utility.GenericDao;
import com.pizzamania.utility.SearchCriteria;
import com.pizzamania.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class PurchaseDetailManagementSearchRepository extends GenericDao<PurchaseDetail, Integer> {

	public PurchaseDetailManagementSearchRepository(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		setEntityManager(entityManager);
	}

	@Override
	protected List<Predicate> applyCriteria(SearchCriteria<PurchaseDetail> searchCriteria, CriteriaBuilder cb,
			Root<PurchaseDetail> root, List<Predicate> predicates) {
		PurchaseDetail domain = searchCriteria.getModel();
		if (domain.getPurchaseDetailId() != null) {
			predicates.add(cb.equal(root.get("purchaseDetailId"), domain.getPurchaseDetailId()));
		}
		if (domain.getPurchaseId() != null) {
			predicates.add(cb.equal(root.get("purchaseId"), domain.getPurchaseId()));
		}
		if (domain.getProductId() != null) {
			predicates.add(cb.equal(root.get("productId"), domain.getProductId()));
		}
		if (domain.getProductQuantity() != null) {
			predicates.add(cb.equal(root.get("productQuantity"), domain.getProductQuantity()));
		}
		if (domain.getProductCost() != null) {
			predicates.add(cb.equal(root.get("productCost"), domain.getProductCost()));
		}
		if (domain.getTotalProductCost() != null) {
			predicates.add(cb.equal(root.get("totalProductCost"), domain.getTotalProductCost()));
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
	protected List<Order> applySort(SearchCriteria<PurchaseDetail> searchCriteria, CriteriaBuilder cb,
			Root<PurchaseDetail> root, List<Order> orders) {
		orders = Utility.populateOrderList(searchCriteria, cb, root);
		// when no sorting criteria is provided, then by default below will be added
		if (!Utility.hasEntries(orders)) {
			orders.add(cb.asc(root.get("purchaseDetailId")));
		}
		return orders;
	}

}
