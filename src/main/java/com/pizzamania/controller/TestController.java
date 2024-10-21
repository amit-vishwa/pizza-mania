package com.pizzamania.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzamania.utility.Message;
import com.pizzamania.utility.Resource;

@RestController
@RequestMapping("/api/test")
public class TestController {

	@GetMapping
	public Resource<String> get() {
		return new Resource<String>(null, "Success!", new Message("SUCCESS", "Success", ""), HttpStatus.OK);
	}

}
