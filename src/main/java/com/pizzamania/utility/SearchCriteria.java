package com.pizzamania.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ObjectUtils;

import com.pizzamania.utility.SortField.SortOrder;

public class SearchCriteria<T> {

	private T model;
	private Paging paging = new Paging();
	private int pageNumber = 0;
	private int recordsInPage = Integer.MAX_VALUE;
	private List<SortField> sortFields = new ArrayList<>();
	private SortCriteria<T> sortCriteria;
	private Map<String, Object> attributes = new TreeMap<>();
	private boolean isFuzzySearch = false;

	private List<DataFilter> dataFilterList;

	public SearchCriteria() {
	}

	public SearchCriteria(T model) {
		this.model = model;
	}

	public SearchCriteria(T model, Paging paging) {
		this.model = model;
		applyPaging(paging);
	}

	public SearchCriteria(T model, boolean isFuzzySearch) {
		this.model = model;
		this.isFuzzySearch = isFuzzySearch;
	}

	public SearchCriteria(T model, Paging paging, boolean isFuzzySearch) {
		this.model = model;
		this.isFuzzySearch = isFuzzySearch;
		applyPaging(paging);
	}

	public SearchCriteria(T model, Paging paging, List<SortField> sortFields) {
		this.model = model;
		if (ObjectUtils.isNotEmpty(sortFields)) {
			this.sortFields = sortFields;
		}
		applyPaging(paging);
	}

	public SearchCriteria(T model, Paging paging, boolean isFuzzySearch, List<SortField> sortFields) {
		this.model = model;
		this.isFuzzySearch = isFuzzySearch;
		if (ObjectUtils.isNotEmpty(sortFields)) {
			this.sortFields = sortFields;
		}
		applyPaging(paging);
	}

	public SearchCriteria(T model, Paging paging, List<SortField> sortFields, List<DataFilter> dataFilterList) {
		this.model = model;
		applyPaging(paging);
		if (ObjectUtils.isNotEmpty(sortFields)) {
			this.sortFields = sortFields;
		}
		if (dataFilterList != null && dataFilterList.size() > 0) {
			this.dataFilterList = dataFilterList;
		}
	}

	public SearchCriteria(T model, Paging paging, boolean isFuzzySearch, List<SortField> sortFields,
			List<DataFilter> dataFilterList) {
		this.model = model;
		this.isFuzzySearch = isFuzzySearch;
		applyPaging(paging);
		if (ObjectUtils.isNotEmpty(sortFields)) {
			this.sortFields = sortFields;
		}
		if (dataFilterList != null && dataFilterList.size() > 0) {
			this.dataFilterList = dataFilterList;
		}
	}

	/**
	 * Factory method for creating search criteria.
	 *
	 * @param model model object
	 * @return SearchCriteria
	 */
	public static <T> SearchCriteria<T> create(T model) {
		return new SearchCriteria<>(model);
	}

	/**
	 * Factory method for creating search criteria.
	 *
	 * @param model         model object
	 * @param isFuzzySearch
	 * @return SearchCriteria
	 */
	public static <T> SearchCriteria<T> create(final T model, boolean isFuzzySearch) {
		return new SearchCriteria<>(model, isFuzzySearch);
	}

	/**
	 * Factory method for creating search criteria.
	 *
	 * @param model         model object
	 * @param isFuzzySearch
	 * @param paging
	 * @return SearchCriteria
	 */
	public static <T> SearchCriteria<T> create(final T model, Paging paging, boolean isFuzzySearch) {
		return new SearchCriteria<>(model, paging, isFuzzySearch);
	}

	public void applyPaging() {
		applyPaging(this.paging);
	}

	public void applyPaging(Paging paging) {
		if (ObjectUtils.isNotEmpty(paging)) {
			this.pageNumber = paging.getStart();
			if (paging.getLimit() == 0) {
				paging.setLimit(Integer.MAX_VALUE);
				this.recordsInPage = Integer.MAX_VALUE;
			} else {
				this.recordsInPage = paging.getLimit();
			}
			this.paging = paging;
		} else {
			if (this.recordsInPage == 0) {
				this.recordsInPage = Integer.MAX_VALUE;
			}
			this.paging = new Paging(this.pageNumber, this.recordsInPage);
		}

		SortField sortField = paging.getSortField();
		if (sortField != null) {
			addSortField(sortField);
		}
	}

	public int getCurrentPage() {
		if (ObjectUtils.isNotEmpty(paging)) {
			return this.paging.getStart();
		}
		applyPaging();
		return this.paging.getStart();
	}

	public int getCurrentRecordsInPage() {
		if (ObjectUtils.isNotEmpty(paging)) {
			return this.paging.getLimit();
		}
		applyPaging();
		return this.paging.getLimit();
	}

	public int calcStartIndex(long totalRecordsInResult) {
		if (ObjectUtils.isNotEmpty(paging)) {
			return calcStartIndex(this.paging.getStart(), this.paging.getLimit(), totalRecordsInResult);
		}
		applyPaging();
		return calcStartIndex(this.paging.getStart(), this.paging.getLimit(), totalRecordsInResult);
	}

	public int calcEndIndex(int startIndex, long totalRecordsInResult) {
		if (ObjectUtils.isNotEmpty(paging)) {
			return calcEndIndex(startIndex, this.paging.getLimit(), totalRecordsInResult);
		}
		applyPaging();
		return calcEndIndex(startIndex, this.paging.getLimit(), totalRecordsInResult);
	}

	public int calcTotalPages(long totalRecords) {
		if (ObjectUtils.isNotEmpty(paging)) {
			return calcTotalPages(this.paging.getLimit(), totalRecords);
		}
		applyPaging();
		return calcTotalPages(this.paging.getLimit(), totalRecords);
	}

	public int calcStartIndex(int pageNumber, int recordsInPage, long totalRecordsInResult) {
		int startIndex = pageNumber * recordsInPage;
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (startIndex > totalRecordsInResult) {
			startIndex = -1;
		}
		return startIndex;
	}

	public int calcEndIndex(int startIndex, int recordsInPage, long totalRecordsInResult) {
		int endIndex = startIndex + recordsInPage;
		if (endIndex > totalRecordsInResult) {
			endIndex = (int) totalRecordsInResult;
		}
		return endIndex;
	}

	public int calcTotalPages(int recordsInPage, long totalRecords) {
		return (int) totalRecords > 0 ? (int) Math.ceil((double) totalRecords / (double) recordsInPage) : 0;
	}

	public T getModel() {
		return this.model;
	}

	public void setModel(T model) {
		this.model = model;
	}

	public T getDomain() {
		return model;
	}

	public void setDomain(T domain) {
		this.model = domain;
	}

	public Paging getPaging() {
		return this.paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getRecordsInPage() {
		return this.recordsInPage;
	}

	public void setRecordsInPage(int recordsInPage) {
		this.recordsInPage = recordsInPage;
	}

	public List<SortField> getSortFields() {
		return this.sortFields;
	}

	public void setSortFields(List<SortField> sortFields) {
		this.sortFields.clear();
		this.sortFields.addAll(sortFields);
	}

	/**
	 * Add a sort field.
	 *
	 * @param sortField sort field.
	 * @see com.leaseplan.dao.SearchCriteria#getSortFields()
	 */
	public void addSortField(SortField sortField) {
		sortFields.add(sortField);
	}

	public void addSortField(String field, SortOrder order) {
		addSortField(new SortField(field, order));
	}

	public void addSortField(String field) {
		addSortField(new SortField(field, SortOrder.Ascending));
	}

	public List<DataFilter> getDataFilterList() {
		return this.dataFilterList;
	}

	public void setDataFilterList(List<DataFilter> dataFilterList) {
		this.dataFilterList = dataFilterList;
	}

	/**
	 * @param key lookup key.
	 * @return attribute by lookup key.
	 */
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	/**
	 * Add an attribute.
	 *
	 * @param key   the key
	 * @param value the value.
	 */
	public void addAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes.clear();
		this.attributes.putAll(attributes);
	}

	public boolean isFuzzySearch() {
		return this.isFuzzySearch;
	}

	public void setFuzzySearch(boolean isFuzzySearch) {
		this.isFuzzySearch = isFuzzySearch;
	}

	public SortCriteria<?> getSortCriteria() {
		return this.sortCriteria;
	}

	public void setSortCriteria(SortCriteria<T> sortCriteria) {
		this.sortCriteria = sortCriteria;
	}

}
