package com.pizzamania.utility;

public class SortField {

	/**
	 * Sort ordering.
	 */
	public enum SortOrder {
		Ascending, Descending
	}

	/**
	 * Field name.
	 */
	private String name;

	/**
	 * Field logics.
	 */
	public SortLogic<?> sortLogic = null;

	/**
	 * Field sequence.
	 */
	public int sequence;

	/**
	 * Sort order.
	 */
	private SortOrder sortOrder = SortOrder.Ascending;

	/**
	 * Sort field.
	 */
	public SortField() {
	}

	/**
	 * Sort field.
	 * 
	 * @param name
	 * @param sortOrder
	 */
	public SortField(String name, SortOrder sortOrder) {
		this.name = name;
		this.sortOrder = sortOrder;
	}

	/**
	 * Sort field.
	 * 
	 * @param name
	 * @param sortOrder
	 * @param sortLogic
	 */
	public SortField(String name, SortOrder sortOrder, SortLogic<?> sortLogic) {
		this.name = name;
		this.sortOrder = sortOrder;
		this.sortLogic = sortLogic;
	}

	/**
	 * @param name
	 * @param sortLogic
	 * @param sequence
	 */
	public SortField(String name, SortLogic<?> sortLogic, int sequence) {
		super();
		this.name = name;
		this.sortLogic = sortLogic;
		this.sequence = sequence;
	}

	/**
	 * @param name
	 * @param sortLogic
	 * @param sequence
	 * @param sortOrder
	 */
	public SortField(String name, SortLogic<?> sortLogic, int sequence, SortOrder sortOrder) {
		super();
		this.name = name;
		this.sortLogic = sortLogic;
		this.sequence = sequence;
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the sortLogic
	 */
	public SortLogic<?> getSortLogic() {
		return sortLogic;
	}

	/**
	 * @param sortLogic the sortLogic to set
	 */
	public void setSortLogic(SortLogic<?> sortLogic) {
		this.sortLogic = sortLogic;
	}

	/**
	 * @return the sortOrder
	 */
	public SortOrder getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortDirection(String direction) {
		setSortOrder(
				"Descending".equalsIgnoreCase(direction) || "DESC".equalsIgnoreCase(direction) ? SortOrder.Descending
						: SortOrder.Ascending);
	}

	/**
	 * @return sort direction
	 */
	public String getSortDirection() {
		return sortOrder == SortOrder.Descending ? "Descending" : "Ascending";
	}

	/**
	 * @return sort is ascending.
	 */
	public boolean isAscending() {
		return sortOrder == SortOrder.Ascending;
	}

	/**
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

}
