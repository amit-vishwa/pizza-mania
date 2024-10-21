package com.pizzamania.enumeration;

public enum CRUDTypeEnum {

	CREATE("create", "failedToCreateData"), READ("read", "errorGettingResponse"),
	UPDATE("update", "failedToUpdateData"), DELETE("delete", "failedToDeleteData");

	private String description;
	private String errorMessageType;

	CRUDTypeEnum(String description, String errorMessageType) {
		this.description = description;
		this.errorMessageType = errorMessageType;
	}

	public String getDescription() {
		return this.description;
	}

	public String getErrorMessageType() {
		return this.errorMessageType;
	}

}
