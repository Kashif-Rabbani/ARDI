package com.genesis.resources;

import com.genesis.eso.util.MongoCollections;
import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.LogMapMatcher;
import com.genesis.rdf.parsers.OWLtoD3;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.bson.Document;
import scala.Tuple3;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("bdi")
public class SchemaIntegrationResource {
    @POST
    @Path("schemaIntegration/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_SchemaIntegration(String body) {
        System.out.println("[POST /schemaIntegration] body = " + body);
        try {
            JSONObject objBody = (JSONObject) JSONValue.parse(body);
            JSONObject dataSource1Info = new JSONObject();
            JSONObject dataSource2Info = new JSONObject();

            String dataSource1 = getDataSourceInfo(objBody.getAsString("id1"));
            String dataSource2 = getDataSourceInfo(objBody.getAsString("id2"));

            if (!dataSource1.isEmpty())
                dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

            if (!dataSource1.isEmpty())
                dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

            //String alignmentsIRI = "ALIGNMENTS_" + dataSource1Info.getAsString("dataSourceID") + "_" + dataSource2Info.getAsString("dataSourceID");
            String alignmentsIRI = dataSource1Info.getAsString("iri") + "-"+ dataSource2Info.getAsString("name").replaceAll(" ", "");

            System.out.println("********** Alignments IRI ********** " + alignmentsIRI);

            new LogMapMatcher(
                    dataSource1Info.getAsString("parsedFileAddress"),
                    dataSource2Info.getAsString("parsedFileAddress"),
                    alignmentsIRI
            );

            JSONObject integratedDataSourceObj = new JSONObject();
            integratedDataSourceObj.put("integratedDataSourceID", "INTEGRATED_" + dataSource1Info.getAsString("dataSourceID") + "_" + dataSource2Info.getAsString("dataSourceID"));
            integratedDataSourceObj.put("alignmentsIRI", alignmentsIRI.split("http://www.BDIOntology.com/schema/")[1]);
            integratedDataSourceObj.put("dataSourceID1", dataSource1Info.getAsString("dataSourceID"));
            integratedDataSourceObj.put("dataSourceID2", dataSource2Info.getAsString("dataSourceID"));
            integratedDataSourceObj.put("dataSource1Name", dataSource1Info.getAsString("name"));
            integratedDataSourceObj.put("dataSource2Name", dataSource2Info.getAsString("name"));

            addIntegratedDataSourceInfoAsMongoCollection(integratedDataSourceObj);
            return Response.ok(new Gson().toJson(integratedDataSourceObj)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("getSchemaAlignments/{alignmentsIRI}")
    @Consumes("text/plain")
    public Response GET_IntegratedSchemaAlignment(@PathParam("alignmentsIRI") String iri) {
        System.out.println("[GET /graph" + "/" + iri + "/graphical");
        iri = "http://www.BDIOntology.com/schema/" + iri;
        try {
            JSONArray alignmentsArray = new JSONArray();
            //List<Tuple3<Resource, Property, Resource>> triples = Lists.newArrayList();

            RDFUtil.runAQuery("SELECT * WHERE { GRAPH <" + iri + "> {?s ?p ?o} }", iri).forEachRemaining(triple -> {
                JSONObject alignments = new JSONObject();

                //System.out.println(new ResourceImpl(triple.get("s").toString()) + " -- " + new PropertyImpl(triple.get("p").toString()) + " -- " + new ResourceImpl(triple.get("o").toString()));
                alignments.put("s", triple.get("s").toString());
                alignments.put("p", triple.get("p").toString());
                alignments.put("o", triple.get("o").toString());
                alignmentsArray.add(alignments);
           /* triples.add(
                    new Tuple3<>(
                            new ResourceImpl(triple.get("s").toString()),
                            new PropertyImpl(triple.get("p").toString()),
                            new ResourceImpl(triple.get("o").toString())
                    )
            );*/
            });
            System.out.println(alignmentsArray.toJSONString());
            return Response.ok((alignmentsArray)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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
        System.out.println("Successfully Added to MongoDB");
        MongoClient client = Utils.getMongoDBClient();
        MongoCollections.getIntegratedDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }
}
