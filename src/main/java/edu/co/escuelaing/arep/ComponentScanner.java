package edu.co.escuelaing.arep;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.co.escuelaing.arep.HTTPComponents.HttpServer;
import edu.co.escuelaing.arep.HTTPComponents.WebMethod;
import edu.co.escuelaing.arep.annotations.GetMapping;
import edu.co.escuelaing.arep.annotations.RequestParam;
import edu.co.escuelaing.arep.annotations.RestController;

public class ComponentScanner {

    public static void scanAndRegister() {
        System.out.println("=== Starting Component Scanning ===");
        String basePackage = "edu.co.escuelaing.arep";
        List<String> controllerClasses = findAllClasses(basePackage);

        System.out.println("Found " + controllerClasses.size() + " classes to scan");

        for (String className : controllerClasses) {
            try {
                Class<?> c = Class.forName(className);

                if (c.isAnnotationPresent(RestController.class)) {
                    System.out.println("  -> Found @RestController");

                    Method[] methods = c.getDeclaredMethods();

                    for (Method method : methods) {
                        if (method.isAnnotationPresent(GetMapping.class)) {
                            GetMapping getMapping = method.getAnnotation(GetMapping.class);
                            String path = getMapping.value();

                            System.out.println("  -> Registering: " + path + " -> " + method.getName() + "()");
                            WebMethod webMethod = createWebMethod(method);
                            HttpServer.get(path, webMethod);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + className);
            } catch (NoClassDefFoundError e) {
            } catch (Exception e) {
                System.err.println("Error loading class " + className + ": " + e.getMessage());
            }
        }
        System.out.println("=== Component Scanning Complete ===\n");
    }


    private static List<String> findAllClasses(String basePackage) {
        List<String> classes = new ArrayList<>();

        try {
            String path = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                System.err.println("Could not find package: " + basePackage);
                return classes;
            }

            File directory = new File(resource.getFile());

            if (directory.exists()) {
                findClassesInDirectory(directory, basePackage, classes);
            }

        } catch (Exception e) {
            System.err.println("Error scanning classes: " + e.getMessage());
            e.printStackTrace();
        }

        return classes;
    }

    private static void findClassesInDirectory(File directory, String packageName, List<String> classes) {
        File[] files = directory.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName + "." + file.getName();
                findClassesInDirectory(file, subPackage, classes);

            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(className);
            }
        }
    }

    private static WebMethod createWebMethod(Method method) {
        return (req, res) -> {
            try {
                Parameter[] parameters = method.getParameters();
                Object[] methodArgs = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];

                    if (param.isAnnotationPresent(RequestParam.class)) {
                        RequestParam reqParam = param.getAnnotation(RequestParam.class);
                        String paramName = reqParam.value();
                        String defaultValue = reqParam.defaultValue();

                        String value = req.getValues(paramName);

                        if (value.isEmpty()) {
                            value = defaultValue;
                        }

                        methodArgs[i] = value;
                    }
                }
                Object result = method.invoke(null, methodArgs);
                return (String) result;

            } catch (Exception e) {
                e.printStackTrace();
                return "Error executing method: " + e.getMessage();
            }
        };
    }
}
