package com.pizzamania.utility;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Resource<T> extends ResponseEntity<ResourceInner<T>> {

	public Resource() {
		super(new ResourceInner<T>(null, null, null), HttpStatus.OK);
	}

	public Resource(T requestBody, T payload) {
		super(new ResourceInner<T>(requestBody, payload), HttpStatus.OK);
	}

	public Resource(T requestBody, T payload, Message message) {
		super(new ResourceInner<T>(requestBody, payload, message), HttpStatus.OK);
	}

	public Resource(T requestBody, T payload, Message message, HttpStatus status) {
		super(new ResourceInner<T>(requestBody, payload, message), status);
	}

	public T getRequestBody() {
		return this.getBody().getRequestBody();
	}

	public void setRequestBody(T requestBody) {
		this.getBody().setRequestBody(requestBody);
	}

	public T getPayload() {
		return this.getBody().getPayload();
	}

	public void setPayload(T payload) {
		this.getBody().setPayload(payload);
	}

	public Message getMessage() {
		return this.getBody().getMessage();
	}

	public void setMessage(Message message) {
		this.getBody().setMessage(message);
	}

	public SearchCriteria<T> getSearchCriteria() {
		return this.getBody().getSearchCriteria();
	}

	public void setSearchCriteria(SearchCriteria<T> searchCriteria) {
		this.getBody().setSearchCriteria(searchCriteria);
	}

}
