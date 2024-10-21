package com.pizzamania.security.dto;

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
public class UserDto {

	private Integer userId;
	private String userName;
	private String userPass;
	private String userType;
	private RoleDto role;
	private String recordStatus;
	private Timestamp createdOnTimestamp;
	private String createdByUser;
	private String createdByProcess;
	private Timestamp lastUpdatedOnTimestamp;
	private String lastUpdatedByUser;
	private String lastUpdatedByProcess;

}