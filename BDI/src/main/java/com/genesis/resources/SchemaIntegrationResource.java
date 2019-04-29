package com.genesis.resources;

import com.genesis.eso.util.ConfigManager;
import com.genesis.eso.util.MongoUtil;
import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.LogMapMatcher;
import com.genesis.rdf.model.bdi_ontology.Namespaces;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.bson.Document;
import com.mongodb.client.MongoCollection;

import javax.naming.Name;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static com.mongodb.client.model.Filters.eq;

@Path("bdi")
public class SchemaIntegrationResource {
    @GET
    @Path("getSchemaAlignments/{dataSource1_id}/{dataSource2_id}")
    @Consumes("text/plain")
    public Response GET_IntegratedSchemaAlignment(@PathParam("dataSource1_id") String ds1, @PathParam("dataSource2_id") String ds2) {
        System.out.println("[GET /getSchemaAlignments" + "/" + ds1 + ds2);
        try {
            JSONObject dataSource1Info = new JSONObject();
            JSONObject dataSource2Info = new JSONObject();
            String dataSource1 = null;
            String dataSource2 = null;
            // Receive the ids of two sources
            if (ds1.contains("INTEGRATED-")) {
                dataSource1 = getIntegratedDataSourceInfo(ds1);
            } else {
                dataSource1 = getDataSourceInfo(ds1);
            }

            dataSource2 = getDataSourceInfo(ds2);

            if (!dataSource1.isEmpty())
                dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

            if (!dataSource2.isEmpty())
                dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

            /*Caution: URN should not contain any slashes e.g. http://www.BDIOntology.com/schema/CarRegistration-Bus (allowed) http://www.BDIOntology.com/schema/CarRegistration/Bus (Not allowed)*/

            /* Create an IRI for alignments which will be produced by LogMap for the two sources. Note that this IRI is required to store the alignments in the TripleStore. */

            String alignmentsIRI = Namespaces.Alignments.val() + dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID");

            System.out.println("********** Alignments IRI ********** " + alignmentsIRI);

            // Calling LogMapMatcher class to extract, and save the alignments
            new LogMapMatcher(
                    dataSource1Info.getAsString("parsedFileAddress"),
                    dataSource2Info.getAsString("parsedFileAddress"),
                    alignmentsIRI
            );

            JSONArray alignmentsArray = new JSONArray();
            RDFUtil.runAQuery("SELECT * WHERE { GRAPH <" + alignmentsIRI + "> {?s ?p ?o} }", alignmentsIRI).forEachRemaining(triple -> {
                JSONObject alignments = new JSONObject();
                alignments.put("s", triple.get("s").toString());
                alignments.put("p", triple.get("p").toString());
                alignments.put("o", triple.get("o").toString());
                alignmentsArray.add(alignments);
            });

            String iri = integrateTDBDatasets(dataSource1Info, dataSource2Info);
            System.out.println("Returned IRI: " + iri);

            return Response.ok((alignmentsArray)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("acceptAlignment/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_AcceptAlignment(String body) {
        System.out.println("[POST /acceptAlignment] body = " + body);
        try {
            // Alignments are stored in a triple store because it is more convenient to deal with them this way
            // p: Contains Alignment of Class/Property A , s: Contains Alignment of Class/Property B, and o: Contains the confidence value between both mappings
            JSONObject objBody = (JSONObject) JSONValue.parse(body);

            String integratedIRI = Namespaces.G.val() + objBody.getAsString("integrated_iri");

            String query = " SELECT * WHERE { GRAPH <" + integratedIRI + "> { <" + objBody.getAsString("s") + "> rdf:type ?o ." +
                    "<" + objBody.getAsString("p") + "> rdf:type ?oo .} }";
            final String[] flag = new String[10];
            RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + query, integratedIRI).forEachRemaining(triple -> {
                System.out.println(triple.get("o") + " oo " + triple.get("oo"));
                flag[0] = "Query contains result.";
                if (triple.get("o") != null && triple.get("oo") != null) {
                    if (triple.get("o") == triple.get("oo")) {
                        System.out.println("Alignments between " + triple.get("o").asResource().getLocalName() + " elements.");
                        flag[1] = "Alignments between " + triple.get("o").asResource().getLocalName() + " elements.";
                        if (triple.get("o").asResource().getLocalName().equals("Class")) {
                            RDFUtil.addCustomPropertyTriple(integratedIRI, objBody.getAsString("s"), "EQUIVALENT_CLASS", objBody.getAsString("p"));
                        }
                        if (triple.get("o").asResource().getLocalName().equals("Property")) {
                            RDFUtil.addCustomPropertyTriple(integratedIRI, objBody.getAsString("s"), "EQUIVALENT_PROPERTY", objBody.getAsString("p"));
                        }
                    }
                }
            });

            System.out.println(flag[0] + " " + flag[1]);

            return Response.ok(("Okay")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @POST
    @Path("finishIntegration/")
    @Consumes("text/plain")
    public Response GET_finishIntegration(String body) {
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        System.out.println("[GET /finishIntegration" + "/" + objBody.getAsString("iri"));
        String integratedIRI = objBody.getAsString("iri");
        integratedIRI = Namespaces.G.val() + integratedIRI;
        try {
            // Add the integratedModel into TDB
            Dataset integratedDataset = Utils.getTDBDataset();
            integratedDataset.begin(ReadWrite.WRITE);
            Model model = integratedDataset.getNamedModel(integratedIRI);

            String integratedModelFileName = objBody.getAsString("iri") + ".ttl";
            //String integratedModelFileName = objBody.getAsString("dataSource1Name") + "-" + objBody.getAsString("dataSource2Name") + ".ttl";
            try {
                model.write(new FileOutputStream(ConfigManager.getProperty("output_path") + integratedModelFileName), "TURTLE");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            model.commit();
            model.close();
            integratedDataset.commit();
            integratedDataset.end();
            integratedDataset.close();
            //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
            JSONObject vowlObj = Utils.oWl2vowl(ConfigManager.getProperty("output_path") + integratedModelFileName);

            //updateIntegratedDataSourceInfo(objBody.getAsString("integratedDataSourceID"), vowlObj);

            JSONObject dataSource1Info = new JSONObject();
            JSONObject dataSource2Info = new JSONObject();
            // Receive the ids of two sources need to be integrated
            String dataSource1 = null;
            String dataSource2 = null;


            if (objBody.getAsString("integrationType").equals("GLOBAL-vs-LOCAL")) {
                dataSource1 = getIntegratedDataSourceInfo(objBody.getAsString("ds1_id"));
                dataSource2 = getDataSourceInfo(objBody.getAsString("ds2_id"));

                if (!dataSource1.isEmpty())
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

                if (!dataSource2.isEmpty())
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);
                System.out.println("Global-vs-Local - About to UpdateInfo");
                updateInfo(dataSource1Info, dataSource2Info, ConfigManager.getProperty("output_path") + integratedModelFileName, vowlObj);
            }

            if (objBody.getAsString("integrationType").equals("LOCAL-vs-LOCAL")) {
                dataSource1 = getDataSourceInfo(objBody.getAsString("ds1_id"));
                dataSource2 = getDataSourceInfo(objBody.getAsString("ds2_id"));

                if (!dataSource1.isEmpty())
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

                if (!dataSource2.isEmpty())
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

                addInfo(dataSource1Info, dataSource2Info, ConfigManager.getProperty("output_path") + integratedModelFileName, vowlObj);
            }

            return Response.ok(("Okay")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void addInfo(JSONObject dataSource1Info, JSONObject dataSource2Info, String integratedModelFileName, JSONObject vowlObj) {
        // Constructing JSON Response Object

        JSONObject integratedDataSourceObj = new JSONObject();
        JSONArray dataSourcesArray = new JSONArray();

        JSONObject ds1 = new JSONObject();
        ds1.put("dataSourceID", dataSource1Info.getAsString("dataSourceID"));
        ds1.put("dataSourceName", dataSource1Info.getAsString("name"));
        ds1.put("alignmentsIRI", dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID"));

        JSONObject ds2 = new JSONObject();
        ds2.put("dataSourceID", dataSource2Info.getAsString("dataSourceID"));
        ds2.put("dataSourceName", dataSource2Info.getAsString("name"));
        ds2.put("alignmentsIRI", dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID"));


        dataSourcesArray.add(ds1);
        dataSourcesArray.add(ds2);

        integratedDataSourceObj.put("dataSourceID", "INTEGRATED-" + dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID"));
        //integratedDataSourceObj.put("alignmentsIRI", alignmentsIRI.split(Namespaces.Alignments.val())[1]);
        integratedDataSourceObj.put("iri", Namespaces.G.val() + dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID"));
        integratedDataSourceObj.put("dataSources", dataSourcesArray);
        integratedDataSourceObj.put("name", dataSource1Info.getAsString("name").replaceAll(" ", "") + dataSource2Info.getAsString("name").replaceAll(" ", ""));
        integratedDataSourceObj.put("parsedFileAddress", integratedModelFileName);
        integratedDataSourceObj.put("integratedVowlJsonFileName", vowlObj.getAsString("vowlJsonFileName"));
        integratedDataSourceObj.put("integratedVowlJsonFilePath", vowlObj.getAsString("vowlJsonFilePath"));

        // Adding JSON Response in MongoDB Collection named as IntegratedDataSources
        addIntegratedDataSourceInfoAsMongoCollection(integratedDataSourceObj);
    }

    private void updateInfo(JSONObject integratedDataSourceInfo, JSONObject dataSource2Info, String integratedModelFileName, JSONObject vowlObj) {
        JSONArray dataSourcesArray = (JSONArray) JSONValue.parse(integratedDataSourceInfo.getAsString("dataSources"));
        // New Local Graph data source to be integrated
        JSONObject ds2 = new JSONObject();
        ds2.put("dataSourceID", dataSource2Info.getAsString("dataSourceID"));
        ds2.put("dataSourceName", dataSource2Info.getAsString("name"));
        ds2.put("alignmentsIRI", integratedDataSourceInfo.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID"));

        dataSourcesArray.add(ds2);

        MongoClient client = Utils.getMongoDBClient();
        MongoCollection collection = MongoUtil.getIntegratedDataSourcesCollection(client);
        System.out.println("Mongo Collection About to Upadte: ");
        String newDataSourceID = integratedDataSourceInfo.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID");

        System.out.println("OLD DS ID: " + integratedDataSourceInfo.getAsString("dataSourceID"));
        System.out.println("NEW DS ID: " + newDataSourceID);

        collection.updateOne(eq("dataSourceID", integratedDataSourceInfo.getAsString("dataSourceID")), new Document("$set", new Document("dataSourceID", newDataSourceID)));
        collection.updateOne(eq("dataSourceID", newDataSourceID), new Document("$set", new Document("iri", integratedDataSourceInfo.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID"))));
        collection.updateOne(eq("dataSourceID", newDataSourceID), new Document("$set", new Document("integratedVowlJsonFileName", vowlObj.getAsString("vowlJsonFileName"))));
        collection.updateOne(eq("dataSourceID", newDataSourceID), new Document("$set", new Document("integratedVowlJsonFilePath", vowlObj.getAsString("vowlJsonFilePath"))));
        collection.updateOne(eq("dataSourceID", newDataSourceID), new Document("$set", new Document("parsedFileAddress", integratedModelFileName)));
        collection.updateOne(eq("dataSourceID", newDataSourceID), new Document("$set", new Document("dataSources", dataSourcesArray)));
        collection.updateOne(eq("dataSourceID", newDataSourceID), new Document("$set", new Document("name", integratedDataSourceInfo.getAsString("name").replaceAll(" ", "") + dataSource2Info.getAsString("name").replaceAll(" ", ""))));

        client.close();
    }

    //Supporting Methods
    private String integrateTDBDatasets(JSONObject dataSource1Info, JSONObject dataSource2Info) {

        String integratedIRI = Namespaces.G.val()
                + dataSource1Info.getAsString("dataSourceID") + "-"
                + dataSource2Info.getAsString("dataSourceID");
        System.out.println("********** Integrated IRI ********** " + integratedIRI);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);

        Model ds1Model = ds.getNamedModel(dataSource1Info.getAsString("iri"));
        Model ds2Model = ds.getNamedModel(dataSource2Info.getAsString("iri"));
        System.out.println("Size of ds1 Model: " + ds1Model.size());
        System.out.println("Size of ds2 Model: " + ds2Model.size());

        Model integratedModel = ds1Model.union(ds2Model);
        ds1Model.commit();
        ds2Model.commit();
        //integratedModel.commit();
        ds.commit();
        ds.close();

        // Add the integratedModel into TDB
        Dataset integratedDataset = Utils.getTDBDataset();
        integratedDataset.begin(ReadWrite.WRITE);
        Model model = integratedDataset.getNamedModel(integratedIRI);
        model.add(integratedModel);
        System.out.println("Size of Integrated Model: " + integratedModel.size());

        try {
            model.write(new FileOutputStream("Output/integrated-model.ttl"), "TURTLE");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        model.commit();
        model.close();
        integratedDataset.commit();
        integratedDataset.end();
        integratedDataset.close();
        return integratedIRI;
    }

    private String getDataSourceInfo(String dataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoUtil.getDataSourcesCollection(client).
                find(new Document("dataSourceID", dataSourceId)).iterator();
        return MongoUtil.getMongoObject(client, cursor);
    }

    private String getIntegratedDataSourceInfo(String integratedDataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoUtil.getIntegratedDataSourcesCollection(client).
                find(new Document("dataSourceID", integratedDataSourceId)).iterator();
        return MongoUtil.getMongoObject(client, cursor);
    }

    private void addIntegratedDataSourceInfoAsMongoCollection(JSONObject objBody) {
        System.out.println("Successfully Added to MongoDB");
        MongoClient client = Utils.getMongoDBClient();
        MongoUtil.getIntegratedDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }

    private static void updateIntegratedDataSourceInfo(String iri, JSONObject vowlObj) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCollection collection = MongoUtil.getIntegratedDataSourcesCollection(client);
        collection.updateMany(eq("integratedDataSourceID", iri), new Document("$set", new Document("integratedVowlJsonFilePath", vowlObj.getAsString("vowlJsonFilePath"))));
        collection.updateMany(eq("integratedDataSourceID", iri), new Document("$set", new Document("integratedVowlJsonFileName", vowlObj.getAsString("vowlJsonFileName"))));
        client.close();
    }
}
