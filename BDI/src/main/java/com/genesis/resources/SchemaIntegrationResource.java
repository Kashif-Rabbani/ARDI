package com.genesis.resources;

import com.genesis.eso.util.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            JSONArray alignmentsArray = new JSONArray();

            if (ds1.contains("INTEGRATED-") && ds2.contains("INTEGRATED-")) {
                System.out.println("------------------- GLOBAL-vs-GLOBAL ------------------- ");
                dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(ds1);
                dataSource2 = schemaIntegrationHelper.getIntegratedDataSourceInfo(ds2);

            } else if (ds1.contains("INTEGRATED-")) {
                System.out.println("------------------- GLOBAL-vs-LOCAL ------------------- ");
                dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(ds1);
                dataSource2 = schemaIntegrationHelper.getDataSourceInfo(ds2);

                if (!dataSource1.isEmpty() && !dataSource2.isEmpty()) {
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

                    /* Create an IRI for alignments which will be produced by LogMap for the two sources. Note that this IRI is required to store the alignments in the TripleStore. */
                    String alignmentsIRI = Namespaces.Alignments.val() + dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID");

                    // Calling LogMapMatcher class to extract, and save the alignments
                    LogMapMatcher logMapMatcher = new LogMapMatcher(dataSource1Info.getAsString("parsedFileAddress"), dataSource2Info.getAsString("parsedFileAddress"), alignmentsIRI);

                    RDFUtil.runAQuery("SELECT * WHERE { GRAPH <" + alignmentsIRI + "> {?s ?p ?o} }", alignmentsIRI).forEachRemaining(triple -> {
                        JSONObject alignments = new JSONObject();
                        alignments.put("s", triple.get("s").toString());
                        alignments.put("p", triple.get("p").toString());
                        alignments.put("confidence", triple.get("o").toString().split("__")[0]);
                        alignments.put("mapping_type", triple.get("o").toString().split("__")[1]);
                        alignments.put("lexical_confidence", triple.get("o").toString().split("__")[2]);
                        alignments.put("structural_confidence", triple.get("o").toString().split("__")[3]);
                        alignments.put("mapping_direction", triple.get("o").toString().split("__")[4]);
                        alignmentsArray.add(alignments);
                    });

                    List<Tuple3<String, String, String>> classAlignments = logMapMatcher.getClassesAlignments();

                    Map<String, List<Tuple3<String, String, String>>> c = classAlignments.stream().collect(Collectors.groupingBy(w -> w._2));

                    String iri = dataSource1Info.getAsString("iri");

                    JSONArray superAndSubClassesArray = new JSONArray();
                    JSONArray onlyClassesArray = new JSONArray();

                    c.forEach((groupedClass, listOfClassPairs) -> {
                        System.out.println("Grouped Class: ------------> "+ groupedClass);

                        Map<Tuple2<String, String>, List<String>> superClassesPlusSubClasses = new HashMap<>();
                        List<String> classes = new ArrayList<>();
                        List<Tuple2> classesAndConfidence = new ArrayList<>();

                        for (Tuple3 classPair : listOfClassPairs) {
                            //System.out.println(classPair._1);
                            if (classPair._1.toString().contains(Namespaces.G.val())) {
                                String query = " SELECT DISTINCT ?p WHERE { GRAPH <" + iri + "> { ?p rdfs:subClassOf <" + classPair._1.toString() + "> . } }";
                                //System.out.println(query);
                                List<String> subClasses = schemaIntegrationHelper.getSparqlQueryResult(iri, query);
                                //System.out.println(subClasses.toString());
                                superClassesPlusSubClasses.put(new Tuple2(classPair._1.toString(), classPair._3.toString()), subClasses);
                            } else {
                                classes.add(classPair._1.toString());
                                classesAndConfidence.add(new Tuple2(classPair._1.toString(),classPair._3.toString()));
                            }
                        }
                        System.out.println();
                        System.out.println(" ********************************************************* ");
                        System.out.println("Printing  superClassesPlusSubClasses");
                        System.out.println(superClassesPlusSubClasses.values());

                        System.out.println();

                        System.out.println(" ********************************************************* ");
                        System.out.println("Printing classes [Which were not super Classes]");
                        System.out.println(classes);

                        System.out.println();

                        System.out.println(" *************************** Iterating over SuperClasses Containing their Sub Classes ****************************** ");
                        superClassesPlusSubClasses.forEach((superClass, subClasses) -> {

                            JSONArray wrapperArray = new JSONArray();
                            JSONObject containerObject = new JSONObject();
                            containerObject.put("s", superClass._1);
                            containerObject.put("p", groupedClass);
                            containerObject.put("o", superClass._2);

                            wrapperArray.add(containerObject);

                            List<String> classesSameAsSubClasses = classes.stream().filter(subClasses::contains).collect(Collectors.toList());

                            System.out.println(" ********************************************************* ");
                            System.out.println("Printing classes SameAs SubClasses");
                            System.out.println(classesSameAsSubClasses);
                            System.out.println();

                            List<String> classesDifferentFromSubClasses = classes.stream().filter( obj -> !subClasses.contains(obj)).collect(Collectors.toList());

                            System.out.println(" ********************************************************* ");
                            System.out.println("Printing classes Different from SubClasses");
                            System.out.println(classesDifferentFromSubClasses);
                            System.out.println();

                            for (Tuple2 tuple: classesAndConfidence){
                               if(classesSameAsSubClasses.contains(tuple._1)){
                                   JSONObject temp = new JSONObject();
                                   classesSameAsSubClasses.get(classesSameAsSubClasses.indexOf(tuple._1));
                                   temp.put("s", tuple._1);
                                   temp.put("p", groupedClass);
                                   temp.put("o", tuple._2);
                                   wrapperArray.add(temp);
                               }

                                if(classesDifferentFromSubClasses.contains(tuple._1)){
                                    JSONObject temp = new JSONObject();
                                    classesSameAsSubClasses.get(classesDifferentFromSubClasses.indexOf(tuple._1));
                                    temp.put("s", tuple._1);
                                    temp.put("p", groupedClass);
                                    temp.put("o", tuple._2);
                                    onlyClassesArray.add(temp);
                                }
                            }
                            System.out.println();
                            superAndSubClassesArray.add(wrapperArray);
                        });
                    });
                    System.out.println(superAndSubClassesArray.toJSONString());
                    System.out.println(onlyClassesArray.toJSONString());
                }

            } else {
                System.out.println("------------------- LOCAL-vs-LOCAL ------------------- ");
                dataSource1 = schemaIntegrationHelper.getDataSourceInfo(ds1);
                dataSource2 = schemaIntegrationHelper.getDataSourceInfo(ds2);

                if (!dataSource1.isEmpty() && !dataSource2.isEmpty()) {
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

                    /* Create an IRI for alignments which will be produced by LogMap for the two sources. Note that this IRI is required to store the alignments in the TripleStore. */

                    String alignmentsIRI = Namespaces.Alignments.val() + dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID");
                    // Calling LogMapMatcher class to extract, and save the alignments
                    LogMapMatcher logMapMatcher = new LogMapMatcher(
                            dataSource1Info.getAsString("parsedFileAddress"),
                            dataSource2Info.getAsString("parsedFileAddress"),
                            alignmentsIRI
                    );
                    RDFUtil.runAQuery("SELECT * WHERE { GRAPH <" + alignmentsIRI + "> {?s ?p ?o} }", alignmentsIRI).forEachRemaining(triple -> {
                        JSONObject alignments = new JSONObject();
                        alignments.put("s", triple.get("s").toString());
                        alignments.put("p", triple.get("p").toString());
                        alignments.put("confidence", triple.get("o").toString().split("__")[0]);
                        alignments.put("mapping_type", triple.get("o").toString().split("__")[1]);
                        alignments.put("lexical_confidence", triple.get("o").toString().split("__")[2]);
                        alignments.put("structural_confidence", triple.get("o").toString().split("__")[3]);
                        alignments.put("mapping_direction", triple.get("o").toString().split("__")[4]);
                        alignmentsArray.add(alignments);
                    });
                }
            } // end else condition here
            String integratedIRI = schemaIntegrationHelper.integrateTDBDatasets(dataSource1Info, dataSource2Info);
            schemaIntegrationHelper.initAlignmentTables();

            return Response.ok((alignmentsArray)).build();

        } catch (Exception e) {
            e.printStackTrace();
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
            System.out.println(s.getLocalName() + " " + p.getLocalName() + " URI " + s.getURI());

            String query = " SELECT * WHERE { GRAPH <" + integratedIRI + "> { <" + objBody.getAsString("s") + "> rdf:type ?o ." + "<" + objBody.getAsString("p") + "> rdf:type ?oo .  } }";

            final String[] checkIfQueryContainsResult = new String[5];

            if (objBody.getAsString("ds1_id").contains("INTEGRATED-") && objBody.getAsString("ds2_id").contains("INTEGRATED-")) {
                System.out.println("GLOBAL-vs-GLOBAL");
            } else if (objBody.getAsString("ds1_id").contains("INTEGRATED-")) {
                System.out.println("GLOBAL-vs-LOCAL");
            } else {
                schemaIntegrationHelper.processAlignment(objBody, integratedIRI, s, p, query, checkIfQueryContainsResult, "LOCAL-vs-LOCAL");
                System.out.println("LOCAL-vs-LOCAL");
            }

            System.out.println(checkIfQueryContainsResult[0] + " " + checkIfQueryContainsResult[1]);
            if (checkIfQueryContainsResult[0] != null && checkIfQueryContainsResult[1] != null) {
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
        String integratedIRI = Namespaces.G.val() + objBody.getAsString("iri");

        JSONObject dataSource1Info = new JSONObject();
        JSONObject dataSource2Info = new JSONObject();
        try {
            objBody.put("integratedIRI", integratedIRI);
            //System.out.println(objBody.toJSONString()); {"iri":"wfFEEDGx-FBFLAdRr","integrationType":"LOCAL-vs-LOCAL", "integratedIRI":"http:\/\/www.BDIOntology.com\/global\/wfFEEDGx-FBFLAdRr","ds2_id":"FBFLAdRr","ds1_id":"wfFEEDGx"}
            new AlignmentAlgorithm(objBody);
            String integratedModelFileName = writeToFile(objBody.getAsString("iri"), integratedIRI);
            //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
            JSONObject vowlObj = Utils.oWl2vowl(ConfigManager.getProperty("output_path") + integratedModelFileName);
            if (objBody.getAsString("integrationType").equals("GLOBAL-vs-LOCAL")) {
                String dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(objBody.getAsString("ds1_id"));
                String dataSource2 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds2_id"));

                if (!dataSource1.isEmpty())
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

                if (!dataSource2.isEmpty())
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);
                System.out.println("Global-vs-Local - About to UpdateInfo");
                schemaIntegrationHelper.updateInfo(dataSource1Info, dataSource2Info, ConfigManager.getProperty("output_path") + integratedModelFileName, vowlObj);
            }
            if (objBody.getAsString("integrationType").equals("LOCAL-vs-LOCAL")) {
                String dataSource1 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds1_id"));
                String dataSource2 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds2_id"));

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

    private String writeToFile(String iri, String integratedIRI) {
        // Write the integrated Graph into file by reading from TDB
        Dataset integratedDataset = Utils.getTDBDataset();
        integratedDataset.begin(ReadWrite.WRITE);
        Model model = integratedDataset.getNamedModel(integratedIRI);
        System.out.println("iri: " + iri);
        String integratedModelFileName = iri + ".ttl";
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
        return integratedModelFileName;
    }
}
