package edu.eci.arep.webapps;

import edu.eci.arep.anotation.RequestMapping;

public class WebService {

    @RequestMapping("/hello")
    public static String index() {
        return "Greetings from Spring Boot!";
    }
}
