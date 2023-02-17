package edu.eci.arep.app;

import edu.eci.arep.server.HttpServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        HttpServer server = HttpServer.getInstance();
        server.run(args);
    }
}
