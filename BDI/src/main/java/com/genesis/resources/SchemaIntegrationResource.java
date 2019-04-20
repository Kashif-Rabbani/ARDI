package com.genesis.resources;

import com.genesis.eso.util.MongoCollections;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.LogMapMatcher;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("bdi")
public class SchemaIntegrationResource {
    @POST
    @Path("schemaIntegration/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_SchemaIntegration(String body) {
        System.out.println("[POST /schemaIntegration] body = " + body);

        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        JSONObject dataSource1Info = new JSONObject();
        JSONObject dataSource2Info = new JSONObject();

        String dataSource1 = getDataSourceInfo(objBody.getAsString("id1"));
        String dataSource2 = getDataSourceInfo(objBody.getAsString("id2"));
        String alignmentsIRI = "ALIGNMENTS_" + dataSource1 + "_" + dataSource2;

        if (!dataSource1.isEmpty())
            dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

        if (!dataSource1.isEmpty())
            dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

        new LogMapMatcher(dataSource1Info.getAsString("parsedFileAddress"),
                dataSource2Info.getAsString("parsedFileAddress"),
                alignmentsIRI);

        JSONObject integratedDataSourceObj = new JSONObject();
        integratedDataSourceObj.put("integratedDataSourceID", "INTEGRATED_" + dataSource1 + "_" + dataSource2);
        integratedDataSourceObj.put("alignmentsIRI", alignmentsIRI);
        integratedDataSourceObj.put("dataSourceID1", dataSource1);
        integratedDataSourceObj.put("dataSourceID2", dataSource2);

        addIntegratedDataSourceInfoAsMongoCollection(integratedDataSourceObj);

        return Response.ok(new Gson().toJson(integratedDataSourceObj)).build();
    }

    private String getDataSourceInfo(String dataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoCollections.getDataSourcesCollection(client).
                find(new Document("dataSourceID", dataSourceId)).iterator();
        boolean itIs = true;
        String out = "";
        if (!cursor.hasNext()) itIs = false;
        else out = cursor.next().toJson();
        client.close();

        if (itIs) {
            System.out.println(out);
        } else {
            System.out.println("Not Found");
        }
        return out;
    }

    private void addIntegratedDataSourceInfoAsMongoCollection(JSONObject objBody) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCollections.getIntegratedDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }
}
