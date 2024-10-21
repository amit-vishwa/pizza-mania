package com.pizzamania.utility;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pizzamania.exception.CRUDFailureException;
import com.pizzamania.exception.EntityMissingException;
import com.pizzamania.exception.ResourceAlreadyExistsException;
import com.pizzamania.exception.ResourceNotFoundException;
import com.pizzamania.utility.SortField.SortOrder;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public class Utility {

	private static Logger logger = LoggerFactory.getLogger(Utility.class);

	// Item has value (not null and size > 0)
	public static boolean hasValue(Object value) {
		return value != null && value.toString().trim().length() > 0;
	}

	// List has values
	public static <T> boolean listHasValues(List<T> listValue) {
		return null != listValue && !listValue.isEmpty();
	}

	// Page (list) has entries
	public static <T> boolean hasEntries(Page<T> page) {
		if (page == null) {
			return false;
		} else {
			return hasEntries(page.getPageEntries());
		}
	}

	// List has entries (not null and size greater than zero)
	public static <T> boolean hasEntries(List<T> list) {
		if (list != null && list.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	// Request has search criteria
	public static <T> boolean hasCriteria(Resource<T> request) {
		if (request != null && request.getSearchCriteria() != null
				&& !ObjectUtils.isEmpty(request.getSearchCriteria().getModel())) {
			return true;
		}
		return false;
	}

	// Print or log data
	public static String printableData(Object details) {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = "";
		try {
			json = ow.writeValueAsString(details);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}

	// Display error messages for known exceptions
	public static <T> Resource<T> getExceptionMessage(Exception e) {
		Message message = new Message();
		message.setCode("Failed_To_Process_Request");
		message.setText("Failed_To_Process_Request");
		message.setDetail("Unable to process request at this time!");
		HttpStatus statusCode = HttpStatus.BAD_REQUEST;
		if (hasValue(e.getMessage())) {
			if (e instanceof ResourceNotFoundException || e instanceof CRUDFailureException
					|| e instanceof EntityMissingException || e instanceof ResourceAlreadyExistsException)
				message.setDetail(e.getMessage());
			if (e instanceof AccessDeniedException) {
				message.setDetail(e.getMessage());
				statusCode = HttpStatus.FORBIDDEN;
			}
			if (e instanceof AuthenticationException) {
				message.setDetail(e.getMessage());
				statusCode = HttpStatus.UNAUTHORIZED;
			}
		}
		return new Resource<T>(null, null, message, statusCode);
	}

	// Utility function to populate order list
	public static <T> List<Order> populateOrderList(SearchCriteria<T> searchCriteria, CriteriaBuilder cb,
			Root<T> root) {
		List<Order> orders = new ArrayList<Order>();
		// when sort criteria is used for sorting, below logic will be implemented
		if (searchCriteria.getSortCriteria() != null) {
			if (hasEntries(searchCriteria.getSortCriteria().getAscending())) {
				for (String string : searchCriteria.getSortCriteria().getAscending()) {
					orders.add(cb.asc(root.get(string)));
				}
			}
			if (hasEntries(searchCriteria.getSortCriteria().getDescending())) {
				for (String string : searchCriteria.getSortCriteria().getDescending()) {
					orders.add(cb.desc(root.get(string)));
				}
			}
		}
		// when sort fields are used for sorting, below logic will be implemented
		if (searchCriteria.getSortFields().size() > 0) {
			for (SortField sortField : searchCriteria.getSortFields()) {
				if (SortOrder.Descending.toString().equals(sortField.getSortDirection())) {
					orders.add(cb.desc(root.get(sortField.getName())));
				} else {
					orders.add(cb.asc(root.get(sortField.getName())));
				}
			}
		}
		return orders;
	}

	// Create deep copy of an entity
	@SuppressWarnings("unchecked")
	public static <T> T copyObject(Object object) {
		Gson gson = new Gson();
		JsonObject jsonObject = gson.toJsonTree(object).getAsJsonObject();
		return (T) gson.fromJson(jsonObject, object.getClass());
	}

	@SuppressWarnings({ "unchecked" })
	public static <T> T getCustomObject(Class<T> clazz, Object data) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
			JavaType javaType = objectMapper.getTypeFactory().constructType(clazz);
			String json;
			if (data instanceof Blob) {
				Blob blob = (Blob) data;
				byte[] byteArrayData = blob.getBytes(1, (int) blob.length());
				json = new String(byteArrayData, StandardCharsets.UTF_8);
			} else if (data instanceof String) {
				json = (String) data;
			} else {
				json = ow.writeValueAsString(data);
			}
			if (clazz.getName().equals("java.lang.String")) {
				return (T) json;
			}
			T response = objectMapper.readValue(json, javaType);
			return response;
		} catch (Exception e) {
			logger.error("Error occurred while fetching Object from String " + e.getLocalizedMessage());
			return null;
		}
	}

	public static Blob getBlobObject(Object details) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(details);
			Blob blobObject = new SerialBlob(json.getBytes());
			return blobObject;
		} catch (JsonProcessingException | SQLException e) {
			logger.error("Error occurred while fetching Blob from Object " + e.getLocalizedMessage());
			return null;
		}
	}

}
