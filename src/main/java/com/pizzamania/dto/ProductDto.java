package com.pizzamania.dto;

import java.sql.Timestamp;

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
public class ProductDto {

	private Integer productId;
	private String productName;
	private String productDescription;
	private Double productCost;
	private Integer productQuantity;
	private Boolean productAvailable;
	private String recordStatus;
	private Timestamp createdOnTimestamp;
	private String createdByUser;
	private String createdByProcess;
	private Timestamp lastUpdatedOnTimestamp;
	private String lastUpdatedByUser;
	private String lastUpdatedByProcess;

}
