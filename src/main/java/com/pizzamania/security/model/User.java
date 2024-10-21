package com.pizzamania.security.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "user")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "user_pass")
	private String userPass;

	@Column(name = "user_type")
	private String userType;

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
	@JoinColumn(name = "role_id", referencedColumnName = "role_id")
	@JsonBackReference
	private Role role;

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

	@Column(name = "last_updated_by_process")
	private String lastUpdatedByProcess;

}
