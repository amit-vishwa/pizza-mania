package com.pizzamania.dto;

import java.sql.Timestamp;
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
public class PurchaseDto {

	private Integer purchaseId;
	private Integer purchaseItems;
	private Double purchaseAmount;
	private String status;
	private Timestamp statusTimestamp;
	private String recordStatus;
	private Timestamp createdOnTimestamp;
	private String createdByUser;
	private String createdByProcess;
	private Timestamp lastUpdatedOnTimestamp;
	private String lastUpdatedByUser;
	private String lastUpdatedByProcess;
	private List<PurchaseDetailDto> purchaseDetailsDto;

}
