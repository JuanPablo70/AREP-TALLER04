package edu.eci.arep.server;

import edu.eci.arep.anotation.RequestMapping;
import edu.eci.arep.anotation.Test;
import org.json.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Web Server that uses an external API to find information about a specific movie
 */
public class HttpServer {

    private static HttpServer _instance = new HttpServer();
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String API_KEY = "c2d09dcc";
    private static final String GET_URL = "http://www.omdbapi.com/?t=%1$s&apikey=%2$s";
    private static final Map<String, String> cache = new HashMap<>();
    private Map<String, Method> methods = new HashMap<>();

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
        String className = args[0];
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
            String query = "";
            while ((inputLine = in.readLine()) != null) {
                if (status) {
                    method = inputLine.split(" ")[0];
                    query = inputLine.split(" ")[1];
                    status = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            if (query.startsWith("/apps/")) {
                outputLine = "HTTP/1.1 200 OK\r\n" +
                        "Content-type: text/html\r\n" +
                        "\r\n" + methods.get(query.substring(5)).invoke(null);
            } else {
                outputLine = htmlGetForm();
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    /**
     * Method that finds the movie the user is looking for
     * @param movieName movie's name
     * @return String information of the movie in a Json format
     */
    public static String searchMovie(String movieName) throws IOException {
        String movieJson = "";
        if (cache.containsKey(movieName)) {
            movieJson = cache.get(movieName);
            System.out.println("Pelicula en cache");
        } else {
            String url = String.format(GET_URL, movieName, API_KEY);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            //The following invocation perform the connection implicitly before getting the code
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                movieJson = response.toString();
            } else {
                System.out.println("GET request not worked");
            }
            System.out.println("GET DONE");
            cache.put(movieName, movieJson);
            System.out.println("Guardada en cache");
        }
        return movieJson;
    }

    /**
     * Method that displays information of the movie the user typed
     * @param movie movie name
     * @return String according to the movie
     * @throws IOException
     */
    public static String htmlSimple(String movie) throws IOException {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                htmlTable(searchMovie(movie));
    }

    /**
     * Method that converts a json into a html table
     * @param movieJson data of the movie in a Json format
     * @return html table
     */
    public static String htmlTable(String movieJson) throws JSONException{
        String table = "<table>";
        JSONObject jsonObject = new JSONObject(movieJson);
        for (String key: jsonObject.keySet()) {
            table += "<tr> \n <td>" + key + "</td> \n <td>" + jsonObject.get(key).toString() + "</td> </tr> \n";
        }
        table += "</table>";
        return table;
    }

    /**
     * Method that displays a get form
     * @return String
     */
    public static String htmlGetForm() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Movie Form</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>GET the movie you are looking for</h1>\n" +
                "        <form action=\"/movie\">\n" +
                "            <label for=\"name\">Name:</label><br>\n" +
                "            <input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n" +
                "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"name\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/movie?name=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n" +
                "\n" +
                "    </body>\n" +
                "</html>";
    }

    /**
     * Method that returns the cache
     * @return cache
     */
    public static Map<String, String> getCache() {
        return cache;
    }
}
