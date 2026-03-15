package edu.co.escuelaing.arep.HTTPComponents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    static Map<String, WebMethod> endPoints = new HashMap<>();
    static String staticFilesPath = "";

    private static volatile boolean running = true;
    private static ServerSocket serverSocket;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(8080);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 8080.");
            System.exit(1);
        }

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server gracefully...");
            running = false;
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Server stopped.");
        }));

        System.out.println("Server started on port 8080. Press Ctrl+C to stop.");
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept failed: " + e.getMessage());
                }
                // If running is false, the socket was closed intentionally — exit loop
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            String outputLine;
            boolean firstline = true;
            String reqPath = "";
            Map<String, String> reqParams = new HashMap<>();

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                if (firstline) {
                    String[] firstLineTokens = inputLine.split(" ");
                    String srturi = firstLineTokens[1];
                    URI requri = new URI(srturi);
                    reqPath = requri.getPath();

                    String query = requri.getQuery();
                    reqParams = queryParams(query);

                    System.out.println("Path: " + reqPath);
                    System.out.println("Query params: " + reqParams);
                    firstline = false;
                }
                if (!in.ready()) {
                    break;
                }
            }

            HttpRequest req = new HttpRequest(reqParams);
            HttpResponse res = new HttpResponse();

            WebMethod wm = endPoints.get(reqPath);
            if (wm != null) {
                outputLine = "HTTP/1.1 200 OK\n\r"
                        + "Content-Type:text/html\n\r"
                        + "\n\r"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<title>Response</title>\n"
                        + "</head>"
                        + "<body>"
                        + wm.execute(req, res)
                        + "</body>"
                        + "</html>";
                out.println(outputLine);
            } else {
                String fileContent = readStaticFile(reqPath);
                if (fileContent != null) {
                    String contentType = getContentType(reqPath);
                    outputLine = "HTTP/1.1 200 OK\n\r"
                            + "Content-Type:" + contentType + "\n\r"
                            + "\n\r"
                            + fileContent;
                    out.println(outputLine);
                } else {
                    outputLine = "HTTP/1.1 404 Not Found\n\r"
                            + "Content-Type:text/html\n\r"
                            + "\n\r"
                            + "<!DOCTYPE html>"
                            + "<html>"
                            + "<head>"
                            + "<meta charset=\"UTF-8\">"
                            + "<title>404 Not Found</title>\n"
                            + "</head>"
                            + "<body>"
                            + "<h1>404 Not Found</h1>"
                            + "The requested resource was not found on this server."
                            + "</body>"
                            + "</html>";
                    out.println(outputLine);
                }
            }
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    public static void get(String path, WebMethod wm){
        endPoints.put(path,wm);
    }

    public static void staticfiles(String path){
        staticFilesPath = path;
    }

    private static Map<String, String> queryParams(String query){
        Map<String, String> params = new HashMap<>();
        if(query == null || query.isEmpty()){
            return params;
        }
        String[] tuples = query.split("&");
        for (String tuple : tuples){
            String[] keyAndValue = tuple.split("=");
            if(keyAndValue.length == 2){
                params.put(keyAndValue[0], keyAndValue[1]);
            } else if(keyAndValue.length == 1){
                params.put(keyAndValue[0], "");
            }
        }
        return params;
    }

    private static String getContentType(String file){
        if(file.endsWith(".html")){
            return "text/html";
        } else if(file.endsWith(".css")){
            return "text/css";
        } else if(file.endsWith(".png")){
            return "image/png";
        } else if(file.endsWith(".jpg") || file.endsWith(".jpeg")){
            return "image/jpeg";
        } else {
            return "text/plain";
        }
    }

    private static String readStaticFile(String filePath){
        try{
            String fullPath = staticFilesPath + filePath;
            InputStream inputStream = HttpServer.class.getResourceAsStream(fullPath);
            if(inputStream == null){
                return null;
            }
            StringBuilder content = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))){
                String line;
                while((line = reader.readLine()) != null){
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        } catch (IOException e){
            System.err.println("Error reading file: " + filePath);
            return null;
        }
    }



}
