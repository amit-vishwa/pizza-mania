package com.pizzamania.utility;

import java.util.List;

public class Page<T> {

	/**
	 * Total records in a selected page.
	 */
	private int recordsInPage = Integer.MAX_VALUE;

	/**
	 * Frst record offset number of the list. Specify an offset from where to start
	 * returning data.
	 */
	private int firstRecordOffset;

	/**
	 * Total number of records in list.
	 */
	private int totalRecords;

	/**
	 * The current page of the page. Start Index => 0
	 */
	private int currentPage;

	/**
	 * The total page of the records.
	 */
	private int totalPages;

	/**
	 * The records in the page.
	 */
	private List<T> pageEntries;

	public Page() {
	}

	/**
	 * @param recordsInPage
	 * @param currentPage
	 * @param totalRecords
	 * @param pageEntries
	 */
	public Page(int recordsInPage, int currentPage, long totalRecords, List<T> pageEntries) {
		super();
		try {
			this.recordsInPage = recordsInPage;
			this.currentPage = currentPage;
			this.pageEntries = pageEntries;
			this.firstRecordOffset = currentPage * recordsInPage;
			this.totalRecords = (int) totalRecords;
			this.totalPages = getTotalPagesCount(recordsInPage, totalRecords);
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * @return the recordsInPage
	 */
	public int getRecordsInPage() {
		return recordsInPage;
	}

	/**
	 * @param recordsInPage the recordsInPage to set
	 */
	public void setRecordsInPage(int recordsInPage) {
		this.recordsInPage = recordsInPage;
	}

	/**
	 * @return the firstRecordOffset
	 */
	public int getFirstRecordOffset() {
		return firstRecordOffset;
	}

	/**
	 * @param firstRecordOffset the firstRecordOffset to set
	 */
	public void setFirstRecordOffset(int firstRecordOffset) {
		this.firstRecordOffset = firstRecordOffset;
	}

	/**
	 * @return the totalRecords
	 */
	public int getTotalRecords() {
		return totalRecords;
	}

	/**
	 * @param totalRecords the totalRecords to set
	 */
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	/**
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @param currentPage the currentPage to set
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * @return the totalPages
	 */
	public int getTotalPages() {
		return totalPages;
	}

	/**
	 * @param totalPages the totalPages to set
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * @return the pageEntries
	 */
	public List<T> getPageEntries() {
		return pageEntries;
	}

	/**
	 * @param pageEntries the pageEntries to set
	 */
	public void setPageEntries(List<T> pageEntries) {
		this.pageEntries = pageEntries;
	}

	public static int getTotalPagesCount(int recordsInPage, long totalRecords) {
		if (recordsInPage > 0 && totalRecords > 0) {
			if (recordsInPage == totalRecords) {
				return 1;
			}
			int totalPagesValue = (int) Math.ceil((double) totalRecords / (double) recordsInPage);
			return totalPagesValue;
		}
		return 0;
	}

}
