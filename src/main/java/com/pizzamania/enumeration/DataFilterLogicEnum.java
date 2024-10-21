package com.pizzamania.enumeration;

public enum DataFilterLogicEnum {

	AND("and"), OR("or"), ORFIELD("or-column"), LIKE("like"), EQ("eq"), NE("ne"), LT("lt"), GT("gt"), GE("ge"), LE("le"), IN("in"), COUNT("count"),
	MAX("max"), MIN("min"), SUM("sum"), BETWEEN("between");

	private String val;

	DataFilterLogicEnum(String val) {
		this.val = val;
	}

	public String getValue() {
		return val;
	}

}
