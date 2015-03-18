package me.kazaff.wu.extension;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 * Created by kazaff on 2015/3/18.
 */
public class CORSFilter implements ContainerResponseFilter {
    public void filter(ContainerRequestContext request, ContainerResponseContext response){

        response.getHeaders().add("Access-Control-Allow-Origin", "*");

        if(request.getMethod().equals("OPTIONS")){
            response.getHeaders().add("Access-Control-Allow-Methods", "HEAD,GET,POST,PUT,DELETE,OPTIONS");
            response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type,Origin,Accept");
            response.getHeaders().add("Access-Control-Max-Age", "120");
        }
    }
}
