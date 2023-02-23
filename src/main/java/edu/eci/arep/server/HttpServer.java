package edu.eci.arep.server;

import edu.eci.arep.annotation.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Web Server that uses an external API to find information about a specific movie
 */
public class HttpServer {

    private static HttpServer _instance = new HttpServer();
    private final Map<String, Method> methods = new HashMap<>();

    public HttpServer(){}
    public static HttpServer getInstance() {
        return _instance;
    }

    /**
     * Method that starts the HttpServer
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void run(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        MicroSpringBoot microSpringBoot = MicroSpringBoot.getInstance();
        ArrayList<String> classes = microSpringBoot.getClasses();
        for (String className : classes) {
            // Cargar clase con forname
            Class<?> c = Class.forName(className);
            Method[] classMethods = c.getMethods();
            // Extraer métodos con anotación RequestMapping
            for (Method m : classMethods) { // Extrae una instancia del método
                if (m.isAnnotationPresent(RequestMapping.class)) { // ¿El método tiene @RequestMapping?
                    String path = m.getAnnotation(RequestMapping.class).value(); // Extrae valor del path
                    methods.put(path, m); // Pone en la estructura el método con llave path
                }
            }
        }

        System.out.println(methods);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean status = true;
            String method = "";
            String request = "";
            while ((inputLine = in.readLine()) != null) {
                if (status) {
                    method = inputLine.split(" ")[0];
                    request = inputLine.split(" ")[1];
                    status = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            if (request.startsWith("/apps/")) {
                outputLine = "" + methods.get(request.substring(5)).invoke(null);
            } else {
                outputLine = "";
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

}
