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
public class PurchaseDetailDto {

	private Integer purchaseDetailId;
	private Integer purchaseId;
	private Integer productId;
	private Integer productQuantity;
	private Double productCost;
	private Double totalProductCost;
	private String status;
	private Timestamp statusTimestamp;
	private String recordStatus;
	private Timestamp createdOnTimestamp;
	private String createdByUser;
	private String createdByProcess;
	private Timestamp lastUpdatedOnTimestamp;
	private String lastUpdatedByUser;
	private String lastUpdatedByProcess;

}
