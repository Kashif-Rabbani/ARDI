package com.genesis.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class MyResource {

    // This method is called if HTML is request
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
       return "<html> " + "<title>" + "Rest Page" + "</title>"
          + "<body><h1>" + "REST ESO is Working!" + "</body></h1>" + "</html> ";
}
}
