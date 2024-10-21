package com.pizzamania.utility;

import java.util.List;

public class SortCriteria<T> {

	private List<String> ascending;
	private List<String> descending;

	public SortCriteria() {
	}

	/**
	 * @return the ascending
	 */
	public List<String> getAscending() {
		return ascending;
	}

	/**
	 * @param ascending the ascending to set
	 */
	public void setAscending(List<String> ascending) {
		this.ascending = ascending;
	}

	/**
	 * @return the descending
	 */
	public List<String> getDescending() {
		return descending;
	}

	/**
	 * @param descending the descending to set
	 */
	public void setDescending(List<String> descending) {
		this.descending = descending;
	}

}
