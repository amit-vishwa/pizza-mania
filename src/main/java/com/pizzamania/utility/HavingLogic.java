package com.pizzamania.utility;

import java.util.List;

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
public class HavingLogic {

	/**
	 * Having logic.
	 */
	public enum HavingLogicEnum {
		COUNT
	}

	/**
	 * Having logic.
	 */
	public enum HavingConditionEnum {
		COUNT
	}

	private String type;
	private List<Condition> conditions = null;

}
