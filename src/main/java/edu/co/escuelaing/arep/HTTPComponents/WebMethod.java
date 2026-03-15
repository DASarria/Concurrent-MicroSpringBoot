package edu.co.escuelaing.arep.HTTPComponents;

public interface WebMethod {
    //public String execute();
    public String execute(HttpRequest request, HttpResponse response);
}
