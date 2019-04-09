package com.genesis.resources;


import com.genesis.rdf.model.bdi_ontology.JsonSchemaExtractor;
import com.google.gson.Gson;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import sun.misc.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("bdi")
public class SchemaExtractionResource {

/*    @GET
    @Path("json/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSource() {
        System.out.println("[GET /GET_JSON/]");

        return Response.ok(new Gson().toJson("Kashif")).build();
    }*/

    @POST
    @Path("json/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_JsonFileInfo(String body) {
        System.out.println("[POST /json] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        JsonSchemaExtractor obj = new JsonSchemaExtractor();
        JSONObject res = obj.initiateExtraction(
                objBody.getAsString("filePath"),
                objBody.getAsString("givenName").replaceAll(" ", ""));
        String fileName = obj.getOutputFile();
        System.out.println(res.toJSONString());
        System.out.println("FileName: " + fileName);
        return Response.ok(new Gson().toJson(fileName)).build();
    }

    @POST
    @Path("xml/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_XmlFileInfo(String body) {
        System.out.println("[POST /xml] body = " + body);

        return Response.ok(new Gson().toJson("XML")).build();
    }


    @POST
    @Path("sql/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_SqlConnectionInfo(String body) {
        System.out.println("[POST /sql] body = " + body);

        return Response.ok(new Gson().toJson("SQL")).build();
    }
}