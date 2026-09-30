package com.pizzamania.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
