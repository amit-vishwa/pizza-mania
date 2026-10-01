package com.pizzamania.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class UtilityTest {

	@Test
	void boundsMissingPagingToDefaultApiLimit() {
		SearchCriteria<Object> criteria = new SearchCriteria<>();
		criteria.setPaging(null);

		Utility.boundApiSearch(criteria);

		assertEquals(0, criteria.getPaging().getStart());
		assertEquals(Utility.MAX_API_PAGE_SIZE, criteria.getPaging().getLimit());
	}

	@Test
	void capsOversizedApiPageAndNormalizesNegativeStart() {
		SearchCriteria<Object> criteria = new SearchCriteria<>();
		criteria.setPaging(new Paging(-25, Integer.MAX_VALUE));

		Utility.boundApiSearch(criteria);

		assertEquals(0, criteria.getPaging().getStart());
		assertEquals(Utility.MAX_API_PAGE_SIZE, criteria.getPaging().getLimit());
	}

	@Test
	void preservesValidApiPaging() {
		SearchCriteria<Object> criteria = new SearchCriteria<>();
		criteria.setPaging(new Paging(20, 25));

		Utility.boundApiSearch(criteria);

		assertEquals(20, criteria.getPaging().getStart());
		assertEquals(25, criteria.getPaging().getLimit());
	}

	@Test
	void rejectsUnsupportedDynamicSearchFields() {
		SearchCriteria<Object> criteria = new SearchCriteria<>();
		criteria.setSortFields(List.of(new SortField("userPass", SortField.SortOrder.Ascending)));
		criteria.setDataFilterList(List.of(new DataFilter("unknownField", "value", "EQ")));

		assertThrows(IllegalArgumentException.class,
				() -> Utility.boundApiSearch(criteria, Set.of("userId", "userName")));
	}

	@Test
	void acceptsAllowlistedDynamicSearchFields() {
		SearchCriteria<Object> criteria = new SearchCriteria<>();
		criteria.setPaging(new Paging(0, 25, "userName", "ASC"));
		criteria.setSortFields(List.of(new SortField("userId", SortField.SortOrder.Descending)));
		criteria.setDataFilterList(List.of(new DataFilter("userName", "customer", "LIKE")));

		Utility.boundApiSearch(criteria, Set.of("userId", "userName"));

		assertEquals(25, criteria.getPaging().getLimit());
	}
}
