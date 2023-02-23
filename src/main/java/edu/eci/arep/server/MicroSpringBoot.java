package edu.eci.arep.server;

import edu.eci.arep.annotation.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MicroSpringBoot {

    private static MicroSpringBoot _instance = new MicroSpringBoot();
    private final Path path = Paths.get("src/main/java/edu/eci/arep/webapps");

    public MicroSpringBoot(){}

    public static MicroSpringBoot getInstance(){return _instance;}

    public ArrayList<String> getClasses() throws IOException, ClassNotFoundException {
        ArrayList<String> files = new ArrayList<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
        for (Path file : directoryStream) {
            System.out.println("------------");
            System.out.println(file.toString());
            if (Files.isRegularFile(file) && file.toString().split("\\.")[1].equals("java")) {
                String className = file.toString().replace("\\", ".").replace(".java", "").split("main.")[1];
                if(Class.forName(className).isAnnotationPresent(Component.class)){
                    files.add(className);
                }
            }
        }
        return files;

    }

}
