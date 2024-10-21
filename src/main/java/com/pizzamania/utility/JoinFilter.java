package com.pizzamania.utility;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.criteria.JoinType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class JoinFilter {

	/**
	 * name field should be the attribute through which we joined the second table
	 * in the Entity class
	 */
	private String name;
	private JoinType joinType;
	private List<String> groupByList;
	private List<DataFilter> dataFilterList;
	private List<SortField> sortFields;
	private List<HavingField> havingList;

}
