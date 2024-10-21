package com.pizzamania.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "product")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Integer productId;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "product_description")
	private String productDescription;

	@Column(name = "product_cost")
	private Double productCost;

	@Column(name = "product_quantity")
	private Integer productQuantity;

	@Column(name = "product_available")
	private Boolean productAvailable;

	@Column(name = "record_status")
	private String recordStatus;

	@Column(name = "created_on_timestamp")
	private Timestamp createdOnTimestamp;

	@Column(name = "created_by_user")
	private String createdByUser;

	@Column(name = "created_by_process")
	private String createdByProcess;

	@Column(name = "last_updated_on_timestamp")
	private Timestamp lastUpdatedOnTimestamp;

	@Column(name = "last_updated_by_user")
	private String lastUpdatedByUser;

	@Column(name = "last_Updated_by_process")
	private String lastUpdatedByProcess;

}
