package com.pizzamania.logging;

import java.sql.Blob;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.utility.Utility;

@Component
public class RequestResponseLogging {

	@Autowired
	LogRepository logRepository;

	private static Logger logger = LoggerFactory.getLogger(RequestResponseLogging.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String MASKED_VALUE = "***Masked sensitive information***";
	private static final Set<String> SENSITIVE_FIELDS = Set.of("password", "userpass", "authorization", "token",
			"jwttoken", "accesstoken", "refreshtoken", "idtoken", "secret", "clientsecret");

	public void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {

		String remoteIpAddress = request.getRemoteAddr();
		String httpMethod = request.getMethod();
		String apiUrl = Utility.hasValue(request.getQueryString())
				? request.getRequestURI() + "?" + request.getQueryString()
				: request.getRequestURI();
		if (!apiUrl.contains("auth")) {
			return;
		}
		Blob requestPayload = sanitizePayload(request.getContentAsByteArray());
		Blob responsePayload = sanitizePayload(response.getContentAsByteArray());
		Integer apiStatusCode = response.getStatus();
		String userAgent = Utility.hasValue(request.getHeader("User-Agent"))
				? request.getHeader("User-Agent").substring(0, Math.min(200, request.getHeader("User-Agent").length()))
				: null;
		Timestamp startTime = Utility.hasValue(request.getAttribute("startTime"))
				? (Timestamp) request.getAttribute("startTime")
				: new Timestamp(System.currentTimeMillis());
		Timestamp endTime = new Timestamp(System.currentTimeMillis());
		Log log = new Log(null, remoteIpAddress, httpMethod, apiUrl, requestPayload, responsePayload, apiStatusCode,
				userAgent, startTime, endTime, GlobalConstants.ACTIVE_RECORD_STATUS,
				new Timestamp(System.currentTimeMillis()), GlobalConstants.PIZZA_MANIA_API,
				GlobalConstants.PIZZA_MANIA_API, null, null, null);
		// Logging information in log table
		try {
			logRepository.save(log);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

	}

	Blob sanitizePayload(byte[] payload) {
		if (payload == null || payload.length == 0) {
			return null;
		}
		try {
			JsonNode root = OBJECT_MAPPER.readTree(payload);
			redactSensitiveFields(root);
			return new SerialBlob(OBJECT_MAPPER.writeValueAsBytes(root));
		} catch (Exception e) {
			// Fail closed: an unparseable payload may contain a secret, so do not store it.
			logger.warn("API payload was not stored because it could not be safely redacted");
			return null;
		}
	}

	private void redactSensitiveFields(JsonNode node) {
		if (node instanceof ObjectNode objectNode) {
			Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				if (isSensitiveField(field.getKey())) {
					objectNode.put(field.getKey(), MASKED_VALUE);
				} else {
					redactSensitiveFields(field.getValue());
				}
			}
		} else if (node instanceof ArrayNode arrayNode) {
			arrayNode.forEach(this::redactSensitiveFields);
		}
	}

	private boolean isSensitiveField(String fieldName) {
		String normalizedFieldName = fieldName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
		return SENSITIVE_FIELDS.contains(normalizedFieldName);
	}
}
