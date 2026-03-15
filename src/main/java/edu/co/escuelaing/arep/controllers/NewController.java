package edu.co.escuelaing.arep.controllers;

import edu.co.escuelaing.arep.annotations.GetMapping;
import edu.co.escuelaing.arep.annotations.RestController;

@RestController
public class NewController {
    

    @GetMapping("/newController")
	public static String webMethodNewController() {
		return "Hello from the new controller!";
	}
}
