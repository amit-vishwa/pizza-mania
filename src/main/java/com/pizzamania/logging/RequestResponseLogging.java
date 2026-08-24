package com.pizzamania.logging;

import java.sql.Blob;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.pizzamania.constant.GlobalConstants;
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
		// Authentication payloads can contain passwords and tokens. Store request
		// metadata only; never persist request or response bodies for these routes.
		Blob requestPayload = null;
		Blob responsePayload = null;
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
}
