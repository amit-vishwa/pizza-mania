package com.pizzamania.utility;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SortLogic<T> {

	/**
	 * Sort logic.
	 */
	public enum SortLogicEnum {
		COUNT, MAX, MIN, SUM, AVG, FIELD, TIME_TO_SEC, DATE_FORMAT, CONVERT_TZ
	}

	private String type; // COUNT, MAX, MIN, SUM, AVG, FIELD
	private List<Condition> conditions = new ArrayList<>();
	private List<T> values = new ArrayList<>();

	/**
	 * @param type
	 * @param conditions
	 */
	public SortLogic(String type, List<Condition> conditions) {
		this.type = type;
		this.conditions = conditions;
	}

}
