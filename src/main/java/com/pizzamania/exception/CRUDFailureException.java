package com.pizzamania.exception;

import com.pizzamania.enumeration.CRUDTypeEnum;

public class CRUDFailureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4121433647789634591L;
	private String errorType = "exception";

	public String getErrorType() {
		return this.errorType;
	}

	public CRUDFailureException() {
	}

	public CRUDFailureException(String msg) {
		super(msg);
	}

	public CRUDFailureException(CRUDTypeEnum crudType) {
		super(crudType.getErrorMessageType());
		errorType = crudType.getErrorMessageType();
	}

}
