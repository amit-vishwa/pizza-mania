package com.pizzamania.security.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pizzamania.security.enums.AppRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private Integer roleId;

	@ToString.Exclude
	@Enumerated(EnumType.STRING)
	@Column(length = 20, name = "role_name")
	private AppRole roleName;

	@OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
	@JsonBackReference
	@ToString.Exclude
	private Set<User> users = new HashSet<>();

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

	public Role(AppRole roleName, String recordStatus, String createdByUser, String createdByProcess,
			Timestamp createdOnTimestamp) {
		this.roleName = roleName;
		this.recordStatus = recordStatus;
		this.createdByUser = createdByUser;
		this.createdByProcess = createdByProcess;
		this.createdOnTimestamp = createdOnTimestamp;
	}

}
