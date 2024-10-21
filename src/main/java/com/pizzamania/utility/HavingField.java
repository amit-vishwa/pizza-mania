package com.pizzamania.utility;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HavingField {

	/**
	 * Having ordering.
	 */
	public enum HavingCondition {
		GREATERTHAN, GT, EQUALS, EQ
	}

	/**
	 * Field name.
	 */
	private String name;
	/**
	 * Having condition.
	 */
	private String condition;

	/**
	 * Field value.
	 */
	public Long value;

	/**
	 * Field logics.
	 */
	public HavingLogic logic;

}
