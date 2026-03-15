package edu.co.escuelaing.arep.controllers;

import java.util.concurrent.atomic.AtomicLong;

import edu.co.escuelaing.arep.annotations.GetMapping;
import edu.co.escuelaing.arep.annotations.RequestParam;
import edu.co.escuelaing.arep.annotations.RestController;

@RestController
public class GreetingController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/greeting")
	public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return "Hola " + name;
	}
}
