package com.genesis.resources;


import com.genesis.eso.util.MongoCollections;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.model.bdi_ontology.JsonSchemaExtractor;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;
import sun.misc.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


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

        JSONObject resData = new JSONObject();

        resData.put("name", objBody.getAsString("givenName"));
        resData.put("address", fileName);
        resData.put("type", objBody.getAsString("type"));

        resData.put("dataSourceID", "");
        resData.put("iri", "");
        addMongoCollection(resData);
        System.out.println(res.toJSONString());
        System.out.println("FileName: " + fileName);
        return Response.ok(new Gson().toJson(resData)).build();
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

    private void addMongoCollection(JSONObject objBody){
        MongoClient client = Utils.getMongoDBClient();
        MongoCollections.getDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }

    @GET
    @Path("dataSource/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSource() {
        System.out.println("[GET /GET_dataSource/]");
        MongoClient client = Utils.getMongoDBClient();
        List<String> dataSources = Lists.newArrayList();
        MongoCollections.getDataSourcesCollection(client).find().iterator().forEachRemaining(document -> dataSources.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(dataSources)).build();
    }
}