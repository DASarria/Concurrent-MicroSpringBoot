package edu.co.escuelaing.arep.controllers;

import edu.co.escuelaing.arep.annotations.GetMapping;
import edu.co.escuelaing.arep.annotations.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public static String index() {
		return "Greetings from Spring Boot!";
	}
    @GetMapping("/pi")
	public static String webMethodPi() {
		return "PI= " + Math.PI;
	}
	@GetMapping("/euler")
	public static String webMethodEuler() {
		return "e= " + Math.E;
	}
    @GetMapping("/hello")
	public static String webMethodHello() {
		return "Hello World!";
	}
}
