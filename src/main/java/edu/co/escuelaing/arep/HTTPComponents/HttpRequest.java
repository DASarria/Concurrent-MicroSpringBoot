package edu.co.escuelaing.arep.HTTPComponents;

import java.util.Map;

public class HttpRequest {
    private Map<String, String> queryParams;
    
    public HttpRequest(Map<String, String> queryParams){
        this.queryParams = queryParams;
    }

    public String getValues(String varname){
        return queryParams.getOrDefault(varname, "");
    }
}
