package com.pizzamania.utility;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.enumeration.DataFilterLogicEnum;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public abstract class GenericDao<T, PK extends Serializable> {

	private EntityManager entityManager;

	private Class<? extends T> clazz;

	public GenericDao(final Class<T> clazz) {
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	public GenericDao() {
		this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@SuppressWarnings({ "unchecked" })
	protected List<T> doSearch(SearchCriteria<T> searchCriteria, long rowCount) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cr = (CriteriaQuery<T>) cb.createQuery(getClazz());
		Root<T> root = (Root<T>) cr.from(getClazz());

		List<Predicate> modelRestrictions = applyCriteria(searchCriteria, cb, root, new ArrayList<Predicate>());
		List<Predicate> filterRestrictions = applyFilterCriteria(searchCriteria, cb, root, new ArrayList<Predicate>());
		List<Predicate> restrictions = new ArrayList<>();
		Stream.of(modelRestrictions, filterRestrictions).forEach(restrictions::addAll);

		if (restrictions != null && restrictions.size() > 0) {
			cr.where(restrictions.toArray(new Predicate[restrictions.size()]));
		}
		List<Order> orders = applySort(searchCriteria, cb, root, new ArrayList<Order>());
		if (Utility.listHasValues(orders)) {
			cr.orderBy(orders);
		}
		TypedQuery<T> q = entityManager.createQuery(cr);
		q.setFirstResult(searchCriteria.calcStartIndex(rowCount));
		final int recordsInPage = searchCriteria.getRecordsInPage();
		if (recordsInPage < Integer.MAX_VALUE) {
			q.setMaxResults(recordsInPage);
		}
		List<T> all = q.getResultList();
		return all;
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends T> getClazz() {
		Class<?> cl = getClass();
		if (Object.class.getSimpleName().equals(cl.getSuperclass().getSimpleName())) {
			throw new IllegalArgumentException("Default constructor does not support direct instantiation");
		}
		while (!GenericDao.class.getSimpleName().equals(cl.getSuperclass().getSimpleName())) {
			if (cl.getGenericSuperclass() instanceof ParameterizedType) {
				break;
			}
			cl = cl.getSuperclass();
		}
		if (cl.getGenericSuperclass() instanceof ParameterizedType) {
			this.clazz = (Class<T>) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[0];
		}
		return clazz;
	}

	public Page<T> search(SearchCriteria<T> searchCriteria) {
		final long rowCount = rowCount(searchCriteria);
		if (rowCount == 0) {
			return new Page<>(searchCriteria.getRecordsInPage(), 0, rowCount, new ArrayList<T>());
		}
		return new Page<>(searchCriteria.getRecordsInPage(), searchCriteria.calcStartIndex(rowCount), rowCount,
				doSearch(searchCriteria, rowCount));
	}

	@SuppressWarnings("unchecked")
	private long rowCount(SearchCriteria<T> searchCriteria) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<T> root = (Root<T>) criteria.from(getClazz());
		criteria.select(builder.count(root));

		List<Predicate> modelRestrictions = applyCriteria(searchCriteria, builder, root, new ArrayList<Predicate>());
		List<Predicate> filterRestrictions = applyFilterCriteria(searchCriteria, builder, root,
				new ArrayList<Predicate>());
		List<Predicate> restrictions = new ArrayList<>();
		Stream.of(modelRestrictions, filterRestrictions).forEach(restrictions::addAll);

		if (restrictions != null && restrictions.size() > 0) {
			criteria.where(restrictions.toArray(new Predicate[restrictions.size()]));
		}

		try {
			return entityManager.createQuery(criteria).getSingleResult();
		} catch (Exception e) {
			return 0;
		}
	}

	protected List<Predicate> applyFilterCriteria(SearchCriteria<T> searchCriteria, CriteriaBuilder cb, Root<T> root,
			List<Predicate> predicates) {

		List<DataFilter> dataFilterList = searchCriteria.getDataFilterList();
		if (dataFilterList != null && dataFilterList.size() > 0) {
			List<Predicate> orFieldPredicates = new ArrayList<Predicate>();
			for (DataFilter dataFilter : dataFilterList) {
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.OR.getValue())) {
					List<String> list = Stream.of(dataFilter.getValue().split(",")).collect(Collectors.toList());
					// boolean flag, If we have to add isNull condition in OR conditions
					Boolean isNullCheckAdded = list.stream()
							.anyMatch(item -> item.equalsIgnoreCase(GlobalConstants.NULL));
					List<Predicate> orPredicates = new ArrayList<Predicate>();
					if (isNullCheckAdded) {
						list = list.stream().filter(item -> !item.equalsIgnoreCase(GlobalConstants.NULL))
								.collect(Collectors.toList());
						orPredicates.add(cb.isNull(root.get(dataFilter.getName())));
					}
					for (String val : list) {
						orPredicates.add(cb.equal(root.get(dataFilter.getName()), val));
					}
					predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.ORFIELD.getValue())) {
					orFieldPredicates.add(cb.equal(root.get(dataFilter.getName()), dataFilter.getValue()));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.IN.getValue())) {
					List<String> list = Stream.of(dataFilter.getValue().split(",")).collect(Collectors.toList());
					predicates.add(root.get(dataFilter.getName()).in(list));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.EQ.getValue())) {
					predicates.add(cb.equal(root.get(dataFilter.getName()), dataFilter.getValue()));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.NE.getValue())) {
					predicates.add(cb.notEqual(root.get(dataFilter.getName()), dataFilter.getValue()));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.LT.getValue())) {
					predicates.add(cb.lessThan(root.get(dataFilter.getName()), dataFilter.getValue()));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.LE.getValue())) {
					predicates.add(cb.lessThanOrEqualTo(root.get(dataFilter.getName()), dataFilter.getValue()));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.GT.getValue())) {
					predicates.add(cb.greaterThan(root.get(dataFilter.getName()), dataFilter.getValue()));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.GE.getValue())) {
					predicates.add(cb.greaterThanOrEqualTo(root.get(dataFilter.getName()), dataFilter.getValue()));
				}
				if (dataFilter.getLogic().equalsIgnoreCase(DataFilterLogicEnum.LIKE.getValue())) {
					predicates.add(cb.like(root.get(dataFilter.getName()), "%" + dataFilter.getValue() + "%"));
				}
			}
			if (Utility.listHasValues(orFieldPredicates)) {
				predicates.add(cb.or(orFieldPredicates.toArray(new Predicate[orFieldPredicates.size()])));
			}
		}

		return predicates;
	};

	protected abstract List<Predicate> applyCriteria(SearchCriteria<T> searchCriteria, CriteriaBuilder cb, Root<T> root,
			List<Predicate> predicates);

	protected abstract List<Order> applySort(SearchCriteria<T> searchCriteria, CriteriaBuilder cb, Root<T> root,
			List<Order> orders);

	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
	}

}
