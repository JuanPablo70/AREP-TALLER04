package edu.eci.arep.app;

import edu.eci.arep.anotation.Test;

import java.lang.reflect.Method;

public class RunTests {
    public static void main(String[] args) throws Exception {
        int passed = 0, failed = 0;
        for (String className: args) { // Recorre más de una clase
            for (Method m : Class.forName(className).getMethods()) { // Recorre métodos de la clase que se le pase "edu.eci.arep.app.Foo"
                if (m.isAnnotationPresent(Test.class)) { // El método tiene @Test?
                    try {
                        m.invoke(null); // null porque es static
                        passed++;
                    } catch (Throwable ex) {
                        System.out.printf("Test %s failed: %s %n", m, ex.getCause());
                        failed++;
                    }
                }
            }
        }
        System.out.printf("Passed: %d, Failed %d%n", passed, failed);
    }
}

