package com.pizzamania.logging;

import java.sql.Blob;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzamania.constant.GlobalConstants;
import com.pizzamania.security.dto.UserDto;
import com.pizzamania.utility.Utility;

@Component
public class RequestResponseLogging {

	@Autowired
	LogRepository logRepository;

	private static Logger logger = LoggerFactory.getLogger(RequestResponseLogging.class);

	public void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {

		String remoteIpAddress = request.getRemoteAddr();
		String httpMethod = request.getMethod();
		String apiUrl = Utility.hasValue(request.getQueryString())
				? request.getRequestURI() + "?" + request.getQueryString()
				: request.getRequestURI();
		if (!apiUrl.contains("auth")) {
			return;
		}
		Blob requestPayload = null;
		Blob responsePayload = null;
		try {
			requestPayload = new SerialBlob(request.getContentAsByteArray());
			responsePayload = new SerialBlob(response.getContentAsByteArray());
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
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
		maskSensitiveInformation(log);
		// Logging information in log table
		try {
			logRepository.save(log);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

	}

	private void maskSensitiveInformation(Log log) {
		if (Utility.hasValue(log.getApiUrl())) {
			if (log.getApiUrl().equals("/api/auth/user")) {
				maskPasswordFromList(log);
			}
			if (log.getApiUrl().equals("/api/auth/user/delete")) {
				UserDto userDto = Utility.getCustomObject(UserDto.class, log.getRequest());
				if (userDto != null) {
					userDto.setUserPass(Utility.hasValue(userDto.getUserPass()) ? "***Masked sensitive information***"
							: userDto.getUserPass());
					log.setRequest(Utility.getBlobObject(userDto));
				}
			}
		}
	}

	private void maskPasswordFromList(Log log) {
		try {
			String response = Utility.getCustomObject(String.class, log.getRequest());
			if (response != null) {
				ObjectMapper mapper = new ObjectMapper();
				List<UserDto> userDtoList = mapper.readValue(response, new TypeReference<List<UserDto>>() {
				});
				if (Utility.listHasValues(userDtoList)) {
					userDtoList.forEach(userDto -> userDto
							.setUserPass(Utility.hasValue(userDto.getUserPass()) ? "***Masked sensitive information***"
									: userDto.getUserPass()));
					log.setRequest(Utility.getBlobObject(userDtoList));
				}
			}
		} catch (Exception e) {
			logger.error("Error occurred while masking sensitive information " + e.getLocalizedMessage());
		}
	}

}
