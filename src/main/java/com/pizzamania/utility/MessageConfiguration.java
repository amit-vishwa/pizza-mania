package com.pizzamania.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class MessageConfiguration {

	JsonObject object;

	MessageConfiguration() throws Exception {
		String jsonString;
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("messages.json");
		if (inputStream == null) {
			throw new Exception("Message Configuration not found");
		}
		jsonString = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
				.collect(Collectors.joining("\n"));
		this.object = JsonParser.parseString(jsonString).getAsJsonObject();
	}

	public Message getMessage(String messageId) {
		JsonObject messages = object.getAsJsonObject().getAsJsonObject("messages");
		JsonObject message = messages.getAsJsonObject(messageId);
		return new Message(message.getAsJsonPrimitive("code").getAsString(),
				message.getAsJsonPrimitive("text").getAsString(), message.getAsJsonPrimitive("detail").getAsString());
	}

}
