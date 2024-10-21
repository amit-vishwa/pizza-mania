package com.pizzamania.utility;

public class ResourceInner<T> {

	private T requestBody;
	private T payload;
	private Message message;
	private SearchCriteria<T> searchCriteria;

	public ResourceInner() {
	}

	public ResourceInner(T requestBody, T payload) {
		this.requestBody = requestBody;
		this.payload = payload;
	}

	public ResourceInner(T requestBody, T payload, Message message) {
		this.requestBody = requestBody;
		this.payload = payload;
		this.message = message;
	}

	public T getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(T requestBody) {
		this.requestBody = requestBody;
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public SearchCriteria<T> getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(SearchCriteria<T> searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

}
