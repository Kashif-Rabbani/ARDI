package com.genesis.resources;

import com.genesis.eso.util.MongoUtil;
import com.genesis.eso.util.Utils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("bdi")
public class DataSourcesResource {
    @GET
    @Path("bdiIntegratedDataSources/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_integrated_dataSource() {
        System.out.println("[GET /GET_bdiIntegratedDataSources/]");
        MongoClient client = Utils.getMongoDBClient();
        List<String> integratedDataSources = Lists.newArrayList();
        MongoUtil.getIntegratedDataSourcesCollection(client).find().iterator().forEachRemaining(document -> integratedDataSources.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(integratedDataSources)).build();
    }


    @GET
    @Path("bdiDataSource/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSource() {
        System.out.println("[GET /GET_dataSources/]");
        MongoClient client = Utils.getMongoDBClient();
        List<String> dataSources = Lists.newArrayList();
        MongoUtil.getDataSourcesCollection(client).find().iterator().forEachRemaining(document -> dataSources.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(dataSources)).build();
    }


    @GET
    @Path("bdiIntegratedDataSources/{integratedIRI}")
    @Consumes("text/plain")
    public Response GET_IntegratedDataSourceWithIRI(@PathParam("integratedIRI") String iri) {
        System.out.println("[GET /bdiIntegratedDataSources" + "/" + iri);
        String ids = getIntegratedDataSourceInfo(iri);
        JSONObject idsInfo = new JSONObject();

        if (!ids.isEmpty())
            idsInfo = (JSONObject) JSONValue.parse(ids);
        return Response.ok(new Gson().toJson(idsInfo)).build();
    }


    private String getIntegratedDataSourceInfo(String dataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoUtil.getIntegratedDataSourcesCollection(client).
                find(new Document("integratedDataSourceID", dataSourceId)).iterator();
        return MongoUtil.getMongoObject(client, cursor);
    }


}
