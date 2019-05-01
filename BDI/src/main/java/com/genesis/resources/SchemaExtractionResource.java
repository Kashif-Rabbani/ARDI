package com.genesis.resources;


import com.genesis.eso.util.MongoUtil;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.model.bdi_ontology.JsonSchemaExtractor;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;


@Path("bdi")
public class SchemaExtractionResource {

    @POST
    @Path("jsonSchema/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_JsonFileInfo(String body) {
        System.out.println("[POST /json] body = " + body);
        //Parsing body as JSON
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        //Creating JsonSchemaExtractor Object
        JsonSchemaExtractor jsonSchemaExtractor = new JsonSchemaExtractor();
        //Initiating Extraction Process
        // This process will extract JSON schema from the file and convert it into RDFS Knowledge Graph.
        JSONObject res = jsonSchemaExtractor.initiateExtraction(
                objBody.getAsString("filePath"),
                objBody.getAsString("givenName").replaceAll(" ", ""));

        //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
        JSONObject vowlObj = Utils.oWl2vowl(JsonSchemaExtractor.getOutputFile());

        // Preparing the response to be sent back
        JSONObject resData = prepareResponse(JsonSchemaExtractor.getOutputFile(), JsonSchemaExtractor.getIRI(), objBody, vowlObj);

        // Adding the RDFS Schema in Jena TDB Triple Store using IRI
        addExtractedSchemaIntoTDBStore(JsonSchemaExtractor.getIRI());

        // Adding the response to MongoDB
        addDataSourceInfoAsMongoCollection(resData);

        return Response.ok(new Gson().toJson("JSON")).build();
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

    private JSONObject prepareResponse(String fileName, String IRI, JSONObject objBody, JSONObject vowlObj) {
        JSONObject resData = new JSONObject();
        resData.put("name", objBody.getAsString("givenName"));
        resData.put("type", objBody.getAsString("type").toUpperCase());
        resData.put("sourceFileAddress", objBody.getAsString("filePath"));
        resData.put("parsedFileAddress", fileName);
        resData.put("dataSourceID", RandomStringUtils.randomAlphanumeric(8).replace("-", ""));
        resData.put("iri", IRI);
        resData.put("vowlJsonFilePath", vowlObj.getAsString("vowlJsonFilePath"));
        resData.put("vowlJsonFileName", vowlObj.getAsString("vowlJsonFileName"));
        return resData;
    }

    private void addDataSourceInfoAsMongoCollection(JSONObject objBody) {
        MongoClient client = Utils.getMongoDBClient();
        MongoUtil.getDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }

    private void addExtractedSchemaIntoTDBStore(String iri){
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getNamedModel(iri);
        //OntModel ontModel = ModelFactory.createOntologyModel();
        model.read(JsonSchemaExtractor.getOutputFile());
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
    }
}