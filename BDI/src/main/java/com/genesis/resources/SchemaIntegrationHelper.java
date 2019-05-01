package com.genesis.resources;

import com.genesis.eso.util.MongoUtil;
import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.model.bdi_ontology.Namespaces;
import com.genesis.rdf.model.bdi_ontology.metamodel.NewSourceLevel2;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.bson.Document;
import org.apache.jena.ontology.OntModel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SchemaIntegrationHelper {
    public SchemaIntegrationHelper() {
    }

    void processAlignment(JSONObject objBody, String integratedIRI, Resource s, String query, String[] flag) {

        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + query, integratedIRI).forEachRemaining(triple -> {
            System.out.println(triple.get("o") + " oo " + triple.get("oo") );
            flag[0] = "Query contains result.";

            if (triple.get("o") != null && triple.get("oo") != null) {
                if (triple.get("o") == triple.get("oo")) {

                    flag[1] = "Alignments between " + triple.get("o").asResource().getLocalName() + " elements.";

                    if (triple.get("o").asResource().getLocalName().equals("Class")) {
                        String newGlobalGraphClassResource = integratedIRI + "/" + s.getURI().split(Namespaces.Schema.val())[1];
                        RDFUtil.addClassOrPropertyTriple(integratedIRI, newGlobalGraphClassResource, "CLASS");

                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphClassResource, "EQUIVALENT_CLASS", objBody.getAsString("p"));
                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphClassResource, "EQUIVALENT_CLASS", objBody.getAsString("s"));
                        //RDFUtil.addCustomPropertyTriple(integratedIRI, objBody.getAsString("s"), "EQUIVALENT_CLASS", objBody.getAsString("p"));
                    }
                    if (triple.get("o").asResource().getLocalName().equals("Property")) {

                        HashMap<String, String> propDomainRange = getPropertyDomainRange(objBody, integratedIRI);

                        String newGlobalGraphPropertyResource = integratedIRI + "/" + s.getURI().split(Namespaces.Schema.val())[1];
                        //Add property
                        RDFUtil.addClassOrPropertyTriple(integratedIRI, newGlobalGraphPropertyResource, "PROPERTY");
                        // Add Domains and Ranges of the new Global Property
                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphPropertyResource, "DOMAIN", propDomainRange.get("pDomain"));
                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphPropertyResource, "DOMAIN", propDomainRange.get("sDomain"));
                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphPropertyResource, "RANGE", propDomainRange.get("pRange"));

                        // Add Equivalent Properties
                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphPropertyResource, "EQUIVALENT_PROPERTY", objBody.getAsString("p"));
                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphPropertyResource, "EQUIVALENT_PROPERTY", objBody.getAsString("s"));

                        //Remove Source1 i.e. s and source2 i.e p properties including domain and ranges
                        RDFUtil.removeTriple(integratedIRI, objBody.getAsString("p"), NewSourceLevel2.RDFSDomain.val(), propDomainRange.get("pDomain"));
                        RDFUtil.removeTriple(integratedIRI, objBody.getAsString("s"), NewSourceLevel2.RDFSDomain.val(), propDomainRange.get("sDomain"));

                        RDFUtil.removeTriple(integratedIRI, objBody.getAsString("p"), NewSourceLevel2.RDFSRange.val(), propDomainRange.get("pRange"));
                        RDFUtil.removeTriple(integratedIRI, objBody.getAsString("s"), NewSourceLevel2.RDFSRange.val(), propDomainRange.get("sRange"));

                        RDFUtil.removeTriple(integratedIRI, objBody.getAsString("p"), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", NewSourceLevel2.RDFProperty.val());
                        RDFUtil.removeTriple(integratedIRI, objBody.getAsString("s"), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", NewSourceLevel2.RDFProperty.val());

                        //RDFUtil.addCustomPropertyTriple(integratedIRI, objBody.getAsString("s"), "EQUIVALENT_PROPERTY", objBody.getAsString("p"));
                    }
                }
            }
        });


    }

    private HashMap<String, String> getPropertyDomainRange(JSONObject objBody, String integratedIRI) {
        HashMap<String, String> propDomainRange = new HashMap<String, String>();
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(integratedIRI);
        OntModel ontModel = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
        ontModel.addSubModel(graph);
        /*System.out.println("if Properties: -> Printing Domain and Range: ... ");
        System.out.println(ontModel.getOntProperty(objBody.getAsString("s")).getDomain());
        System.out.println(ontModel.getOntProperty(objBody.getAsString("s")).getRange());*/
        propDomainRange.put("sDomain", ontModel.getOntProperty(objBody.getAsString("s")).getDomain().toString());
        propDomainRange.put("sRange", ontModel.getOntProperty(objBody.getAsString("s")).getRange().toString());
        propDomainRange.put("pDomain", ontModel.getOntProperty(objBody.getAsString("p")).getDomain().toString());
        propDomainRange.put("pRange", ontModel.getOntProperty(objBody.getAsString("p")).getRange().toString());
        ontModel.close();
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
        return propDomainRange;
    }

    void addInfo(JSONObject dataSource1Info, JSONObject dataSource2Info, String integratedModelFileName, JSONObject vowlObj) {
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

    void updateInfo(JSONObject integratedDataSourceInfo, JSONObject dataSource2Info, String integratedModelFileName, JSONObject vowlObj) {
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

        collection.updateOne(Filters.eq("dataSourceID", integratedDataSourceInfo.getAsString("dataSourceID")), new Document("$set", new Document("dataSourceID", newDataSourceID)));
        collection.updateOne(Filters.eq("dataSourceID", newDataSourceID), new Document("$set", new Document("iri", integratedDataSourceInfo.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID"))));
        collection.updateOne(Filters.eq("dataSourceID", newDataSourceID), new Document("$set", new Document("integratedVowlJsonFileName", vowlObj.getAsString("vowlJsonFileName"))));
        collection.updateOne(Filters.eq("dataSourceID", newDataSourceID), new Document("$set", new Document("integratedVowlJsonFilePath", vowlObj.getAsString("vowlJsonFilePath"))));
        collection.updateOne(Filters.eq("dataSourceID", newDataSourceID), new Document("$set", new Document("parsedFileAddress", integratedModelFileName)));
        collection.updateOne(Filters.eq("dataSourceID", newDataSourceID), new Document("$set", new Document("dataSources", dataSourcesArray)));
        collection.updateOne(Filters.eq("dataSourceID", newDataSourceID), new Document("$set", new Document("name", integratedDataSourceInfo.getAsString("name").replaceAll(" ", "") + dataSource2Info.getAsString("name").replaceAll(" ", ""))));

        client.close();
    }

    String integrateTDBDatasets(JSONObject dataSource1Info, JSONObject dataSource2Info) {

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

    String getDataSourceInfo(String dataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoUtil.getDataSourcesCollection(client).
                find(new Document("dataSourceID", dataSourceId)).iterator();
        return MongoUtil.getMongoObject(client, cursor);
    }

    String getIntegratedDataSourceInfo(String integratedDataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoUtil.getIntegratedDataSourcesCollection(client).
                find(new Document("dataSourceID", integratedDataSourceId)).iterator();
        return MongoUtil.getMongoObject(client, cursor);
    }

    void addIntegratedDataSourceInfoAsMongoCollection(JSONObject objBody) {
        System.out.println("Successfully Added to MongoDB");
        MongoClient client = Utils.getMongoDBClient();
        MongoUtil.getIntegratedDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }

    static void updateIntegratedDataSourceInfo(String iri, JSONObject vowlObj) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCollection collection = MongoUtil.getIntegratedDataSourcesCollection(client);
        collection.updateMany(Filters.eq("integratedDataSourceID", iri), new Document("$set", new Document("integratedVowlJsonFilePath", vowlObj.getAsString("vowlJsonFilePath"))));
        collection.updateMany(Filters.eq("integratedDataSourceID", iri), new Document("$set", new Document("integratedVowlJsonFileName", vowlObj.getAsString("vowlJsonFileName"))));
        client.close();
    }
}