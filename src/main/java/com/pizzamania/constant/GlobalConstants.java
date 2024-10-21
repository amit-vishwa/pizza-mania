package com.pizzamania.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalConstants {

	// -------------------------------------------------------------
	// ### A ###
	public static final String ACTIVE_RECORD_STATUS = "A";

	// -------------------------------------------------------------
	// ### C ###
	public static final String CANCELED_STATUS = "CANCELED";
	public static final String COMPLETED_STATUS = "COMPLETED";
	public static final String COMMAND_LINE_RUNNER_PROCESS = "command-line-runner";

	// -------------------------------------------------------------
	// ### D ###
	public static final String DEACTIVE_RECORD_STATUS = "D";

	// -------------------------------------------------------------
	// ### E ###
	public static final String EXTERNAL_USER = "external";

	// -------------------------------------------------------------
	// ### I ###
	public static final String INTERNAL_USER = "internal";

	// -------------------------------------------------------------
	// ### P ###
	public static final String PENDING_STATUS = "PENDING";
	public static final String PIZZA_MANIA_API = "pizza-mania-api";

	// -------------------------------------------------------------
	// ### N ###
	public static final String NULL = "NULL";

	// -------------------------------------------------------------
	// ### O ###
	public static final List<String> ORDER_STATUS_LIST = new ArrayList<String>(
			Arrays.asList(CANCELED_STATUS, COMPLETED_STATUS, PENDING_STATUS));

	// -------------------------------------------------------------
	// ### U ###
	public static final String REQUIRED_USER_USER = "user";
	public static final String REQUIRED_USER_ADMIN = "admin";
	public static final String REQUIRED_USER_MANAGER = "manager";

	// -------------------------------------------------------------
	// ### Cognito ###
	public static final String CAPITAL_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	public static final String SPECIAL_CHARACTERS = "!@#$";
	public static final String NUMBERS = "1234567890";

}
