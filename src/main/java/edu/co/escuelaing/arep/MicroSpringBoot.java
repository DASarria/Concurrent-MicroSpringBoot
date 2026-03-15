package edu.co.escuelaing.arep;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import edu.co.escuelaing.arep.annotations.GetMapping;
import edu.co.escuelaing.arep.annotations.RequestParam;
import edu.co.escuelaing.arep.annotations.RestController;


public class MicroSpringBoot {
    static Map<String, Method> controllerMethods = new HashMap<>();
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("Loading controller classes...");
        Class<?> c = Class.forName(args[0]);

        if(c.isAnnotationPresent(RestController.class)){
            for(Method m : c.getDeclaredMethods()){
                if(m.isAnnotationPresent(GetMapping.class)){
                    GetMapping a = m.getAnnotation(GetMapping.class);
                    controllerMethods.put(a.value(),m);
                }
            }
        }
        
        String path = args[1];
        Map<String, String> queryParams = new HashMap<>();
        for(int i = 2; i < args.length; i++){
            String[] keyValue = args[i].split("=");
            if(keyValue.length == 2){
                queryParams.put(keyValue[0], keyValue[1]);
            }
        }
        
        System.out.println("Executing web method for path: " + path);
        System.out.println("Query params: " + queryParams);
        
        Method m = controllerMethods.get(path);
        if(m == null){
            System.out.println("No method found for path: " + path);
            return;
        }
        Parameter[] parameters = m.getParameters();
        Object[] methodArgs = new Object[parameters.length];
        
        for(int i = 0; i < parameters.length; i++){
            Parameter param = parameters[i];
            if(param.isAnnotationPresent(RequestParam.class)){
                RequestParam reqParam = param.getAnnotation(RequestParam.class);
                String paramName = reqParam.value();
                String defaultValue = reqParam.defaultValue();

                String value = queryParams.getOrDefault(paramName, defaultValue);
                methodArgs[i] = value;
                System.out.println("Parameter: " + paramName + " = " + value);
            }
        }
        System.out.println("Result: " + m.invoke(null, methodArgs));
    }
}
