package com.pizzamania.exception;

public class EntityMissingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3808528649947890891L;

	public EntityMissingException() {
	}

	public EntityMissingException(String msg) {
		super(msg);
	}

}
