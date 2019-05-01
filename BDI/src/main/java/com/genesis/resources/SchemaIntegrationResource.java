package com.genesis.resources;

import com.genesis.eso.util.ConfigManager;
import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.LogMapMatcher;
import com.genesis.rdf.model.bdi_ontology.Namespaces;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static com.mongodb.client.model.Filters.eq;

@Path("bdi")
public class SchemaIntegrationResource {
    private final SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();
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
                dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(ds1);
            } else {
                dataSource1 = schemaIntegrationHelper.getDataSourceInfo(ds1);
            }

            dataSource2 = schemaIntegrationHelper.getDataSourceInfo(ds2);

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

            String iri = schemaIntegrationHelper.integrateTDBDatasets(dataSource1Info, dataSource2Info);
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
            Resource s = ResourceFactory.createResource(objBody.getAsString("s"));
            Resource p = ResourceFactory.createResource(objBody.getAsString("p"));
            System.out.println(s.getLocalName() + " " + p.getLocalName() + " URI "+ s.getURI());

            String query = " SELECT * WHERE { GRAPH <" + integratedIRI + "> { <" + objBody.getAsString("s") + "> rdf:type ?o ." + "<" + objBody.getAsString("p") + "> rdf:type ?oo .  } }";

            final String[] flag = new String[10];

            schemaIntegrationHelper.processAlignment(objBody, integratedIRI, s, query, flag);

            System.out.println(flag[0] + " " + flag[1]);
            if (flag[0] != null && flag[1] != null) {
                return Response.ok(("AlignmentSucceeded")).build();
            } else {
                return Response.ok(("AlignmentFailed")).build();
            }
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
                dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(objBody.getAsString("ds1_id"));
                dataSource2 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds2_id"));

                if (!dataSource1.isEmpty())
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

                if (!dataSource2.isEmpty())
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);
                System.out.println("Global-vs-Local - About to UpdateInfo");
                schemaIntegrationHelper.updateInfo(dataSource1Info, dataSource2Info, ConfigManager.getProperty("output_path") + integratedModelFileName, vowlObj);
            }

            if (objBody.getAsString("integrationType").equals("LOCAL-vs-LOCAL")) {
                dataSource1 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds1_id"));
                dataSource2 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds2_id"));

                if (!dataSource1.isEmpty())
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

                if (!dataSource2.isEmpty())
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

                schemaIntegrationHelper.addInfo(dataSource1Info, dataSource2Info, ConfigManager.getProperty("output_path") + integratedModelFileName, vowlObj);
            }

            return Response.ok(("Okay")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
