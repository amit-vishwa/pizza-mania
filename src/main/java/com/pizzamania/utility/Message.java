package com.pizzamania.utility;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

	private String code;
	private String text;
	private String detail;

	public Message(String code, String text) {
		this.code = code;
		this.text = text;
	}

	public Message(String code, String text, String detail) {
		this.code = code;
		this.text = text;
		this.detail = detail;
	}

}
