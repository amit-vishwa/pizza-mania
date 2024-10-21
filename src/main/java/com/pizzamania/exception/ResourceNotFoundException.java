package com.pizzamania.exception;

public class ResourceNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1712391711903524243L;

	public ResourceNotFoundException() {
	}

	public ResourceNotFoundException(String msg) {
		super(msg);
	}

}
