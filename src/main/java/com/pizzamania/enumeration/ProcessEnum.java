package com.pizzamania.enumeration;

public enum ProcessEnum {

	ADD_USERS("add-users"), EDIT_USERS("edit-users"), DELETE_USERS("delete-users"), ADD_CARTS("add-carts"),
	EDIT_CARTS("edit-carts"), DELETE_CARTS("delete-carts"), ADD_PRODUCTS("add-products"),
	EDIT_PRODUCTS("edit-products"), DELETE_PRODUCTS("delete-products"), ADD_PURCHASES("add-purchases"),
	EDIT_PURCHASES("edit-purchases"), DELETE_PURCHASES("delete-purchases"), CANCEL_PURCHASES("cancel-purchases"),
	COMPLETE_PURCHASES("complete-purchases"), ADD_PURCHASE_DETAILS("add-purchase-details"),
	EDIT_PURCHASE_DETAILS("edit-purchase-details"), DELETE_PURCHASE_DETAILS("delete-purchase-details"),
	REGISTER_USERS("register-users"), DELETE_USER_DETAILS("delete-user-details");

	public String processCode;

	ProcessEnum(String process) {
		processCode = process;
	}

	public String getProcessCode() {
		return processCode;
	}

	public static String getProcess(String code) {
		for (ProcessEnum steps : ProcessEnum.values()) {
			if (steps.toString().equalsIgnoreCase(code)) {
				return steps.getProcessCode();
			}
		}
		return null;
	}

	public static String getEnum(String code) {
		for (ProcessEnum steps : ProcessEnum.values()) {
			if (steps.getProcessCode().equalsIgnoreCase(code)) {
				return steps.toString();
			}
		}
		return null;
	}

}
