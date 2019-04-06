package com.genesis.resources;


import com.google.common.collect.Lists;
import com.google.gson.Gson;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


@Path("bdi")
public class SchemaExtractionResource {

    @GET
    @Path("json/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSource() {
        System.out.println("[GET /GET_JSON/]");

        return Response.ok(new Gson().toJson("Kashif")).build();
    }
}