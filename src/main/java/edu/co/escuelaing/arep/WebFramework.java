package edu.co.escuelaing.arep;

import java.io.IOException;
import java.net.URISyntaxException;

import edu.co.escuelaing.arep.HTTPComponents.HttpServer;

public class WebFramework {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ComponentScanner.scanAndRegister();
        HttpServer.staticfiles("/webroot");
        HttpServer.main(args);
    }

    public static String euler() {
        return "e= " + Math.E;
    }
}
