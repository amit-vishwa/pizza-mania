package com.pizzamania.utility;

import com.pizzamania.utility.SortField.SortOrder;

public class Paging {

	/**
	 * Default of no paging.
	 */
	public static final Paging NO_PAGING = new Paging(0, Integer.MAX_VALUE, null, null);
	/**
	 * Jqgrid/JS start.
	 */
	private int start;
	/**
	 * Jqgrid/JS limit.
	 */
	private int limit;
	/**
	 * Jqgrid/JS sort.
	 */
	private String sort;
	/**
	 * Jqgrid/JS dir.
	 */
	private String dir;
	/**
	 * Jqgrid/JS initialLoad.
	 */
	private boolean initialLoad;

	public Paging() {
	}

	public Paging(int start, int limit) {
		this.start = start;
		this.limit = limit;
	}

	public Paging(int start, int limit, String sort, String dir) {
		this.start = start;
		this.limit = limit;
		this.sort = sort;
		this.dir = dir;
	}

	public Paging(int start, int limit, String sort, String dir, boolean initialLoad) {
		this(start, limit, sort, dir);
		this.initialLoad = initialLoad;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit <= 0 ? Integer.MAX_VALUE : limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * @return the sort
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * @param sort the sort to set
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}

	/**
	 * @return the dir
	 */
	public String getDir() {
		return dir;
	}

	/**
	 * @param dir the dir to set
	 */
	public void setDir(String dir) {
		this.dir = dir;
	}

	/**
	 * @return sort order
	 */
	public SortField.SortOrder getSortOrder() {
		return dir == null || "ASC".equals(dir) || "Ascending".equals(dir) ? SortField.SortOrder.Ascending
				: SortField.SortOrder.Descending;
	}

	public void setSortOrder(SortOrder sortOrder) {
		// dummy setter
	}

	/**
	 * @return sort field
	 */
	public SortField getSortField() {
		if (sort == null)
			return null;

		return new SortField(sort, getSortOrder());
	}

	public void setSortField(SortField sortField) {
		// dummy setter
	}

	/**
	 * @return the initialLoad
	 */
	public boolean isInitialLoad() {
		return initialLoad;
	}

	/**
	 * @param initialLoad the initialLoad to set
	 */
	public void setInitialLoad(boolean initialLoad) {
		this.initialLoad = initialLoad;
	}

	/**
	 * @return page number based on start & limit.
	 */
	public int calcPageNumber() {
		return limit == 0 ? 0 : start / limit;
	}

	/**
	 * @return page number based on start & limit.
	 */
	public void setPageOffset() {
		int indexToFirst = ((this.getStart() - 1) * this.getLimit());
		this.setStart(indexToFirst);
	}

}
