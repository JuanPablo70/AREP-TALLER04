package edu.eci.arep.webapps;

import edu.eci.arep.anotation.Component;
import edu.eci.arep.anotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
@Component
public class WebService {

    @RequestMapping("/html")
    public static String html() {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get("src/main/resources/index.html")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" + content;
    }

    @RequestMapping("/css")
    public static String css() {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get("src/main/resources/style.css")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: text/css\r\n" +
                "\r\n" + content;
    }

    @RequestMapping("/img")
    public static String img() {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get("src/main/resources/logo.png")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: image/png\r\n" +
                "\r\n" + content;
    }

    @RequestMapping("/js")
    public static String js() {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get("src/main/resources/script.js")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: application/javascript\r\n" +
                "\r\n" + content;
    }
}
