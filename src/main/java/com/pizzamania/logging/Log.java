package com.pizzamania.logging;

import java.sql.Blob;
import java.sql.Timestamp;

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
@Table(name = "log")
public class Log {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id")
	private Long logId;

	@Column(name = "remote_ip_address")
	private String remoteIPAddress;

	@Column(name = "http_method")
	private String httpMethod;

	@Column(name = "api_url")
	private String apiUrl;

	@Column(name = "request_payload")
	private Blob request;

	@Column(name = "response_payload")
	private Blob response;

	@Column(name = "api_status_code")
	private Integer apiStatusCode;

	@Column(name = "user_agent")
	private String userAgent;

	@Column(name = "start_time")
	private Timestamp startTime;

	@Column(name = "end_time")
	private Timestamp endTime;

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
