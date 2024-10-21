package com.pizzamania.exception;

public class ResourceAlreadyExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4121433647789634591L;

	public ResourceAlreadyExistsException() {
	}

	public ResourceAlreadyExistsException(String msg) {
		super(msg);
	}

}
