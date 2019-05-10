package com.genesis.resources;

import com.genesis.eso.util.MongoUtil;
import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.SQLiteUtils;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.model.bdi_ontology.Namespaces;
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
import org.bson.Document;
import org.apache.jena.ontology.OntModel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchemaIntegrationHelper {
    public SchemaIntegrationHelper() {
    }

    void processAlignment(JSONObject objBody, String integratedIRI, Resource s, String query, String[] checkIfQueryContainsResult) {

        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + query, integratedIRI).forEachRemaining(triple -> {
            System.out.println(triple.get("o") + " oo " + triple.get("oo"));
            checkIfQueryContainsResult[0] = "Query Returned Result > 0";

            if (triple.get("o") != null && triple.get("oo") != null) {
                if (triple.get("o") == triple.get("oo")) {

                    checkIfQueryContainsResult[1] = "Alignments between " + triple.get("o").asResource().getLocalName() + " elements.";

                    // Classes p (source 1 Class) and s (source2 Class)
                    if (triple.get("o").asResource().getLocalName().equals("Class")) {

                        String sql = "INSERT INTO Class (classA,classB,countPropClassA,countPropClassB,listPropClassA,listPropClassB,actionType) VALUES (" +
                                "'" + objBody.getAsString("p") + "'" + "," +
                                "'" + objBody.getAsString("s") + "'" + "," +
                                " );";

                        String newGlobalGraphClassResource = integratedIRI + "/" + s.getURI().split(Namespaces.Schema.val())[1];
                        RDFUtil.addClassOrPropertyTriple(integratedIRI, newGlobalGraphClassResource, "CLASS");

                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphClassResource, "EQUIVALENT_CLASS", objBody.getAsString("p"));
                        RDFUtil.addCustomPropertyTriple(integratedIRI, newGlobalGraphClassResource, "EQUIVALENT_CLASS", objBody.getAsString("s"));
                        //RDFUtil.addCustomPropertyTriple(integratedIRI, objBody.getAsString("s"), "EQUIVALENT_CLASS", objBody.getAsString("p"));
                    }

                    // Properties p (source 1 property) and s (source2 Property)
                    if (triple.get("o").asResource().getLocalName().equals("Property")) {
                        HashMap<String, String> propDomainRange = getPropertiesInfo(objBody, integratedIRI);
                        String sql = "INSERT INTO Property " +
                                "(PropertyA,PropertyB,DomainPropA,DomainPropB,RangePropA,RangePropB,AlignmentType,hasSameName,actionType) VALUES (" +
                                "'" + objBody.getAsString("p") + "'" + ',' +
                                "'" + objBody.getAsString("s") + "'" + ',' +
                                "'" + propDomainRange.get("pDomain") + "'" + ',' +
                                "'" + propDomainRange.get("sDomain") + "'" + ',' +
                                "'" + propDomainRange.get("pRange") + "'" + ',' +
                                "'" + propDomainRange.get("sRange") + "'" + ',' +
                                "'" + " Property" + "'" + ',' +
                                "'" + propDomainRange.get("hasSameName") + "'" + ',' +
                                "'" + objBody.getAsString("actionType") + "'" +
                                " ); ";
                        System.out.println("Inserting into SQLite Table Property");
                        SQLiteUtils.executeQuery(sql);
                        List<String> features = new ArrayList<>();
                        features.add("actionType");
                        System.out.println("SELECT query....");
                        JSONArray returnValue = SQLiteUtils.executeSelect("SELECT actionType FROM Property", features);
                        System.out.println(returnValue.toJSONString());
                    }
                }
            }
        });


    }

    private HashMap<String, String> getPropertiesInfo(JSONObject objBody, String integratedIRI) {
        HashMap<String, String> propCharacteristics = new HashMap<String, String>();
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(integratedIRI);
        OntModel ontModel = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
        ontModel.addSubModel(graph);

        System.out.println("if Properties: -> Printing Domain and Range: ... ");
        System.out.println(ontModel.getOntProperty(objBody.getAsString("s")).getLocalName());
        System.out.println(ontModel.getOntProperty(objBody.getAsString("p")).getLocalName());

        propCharacteristics.put("sDomain", ontModel.getOntProperty(objBody.getAsString("s")).getDomain().toString());
        propCharacteristics.put("sRange", ontModel.getOntProperty(objBody.getAsString("s")).getRange().toString());
        propCharacteristics.put("pDomain", ontModel.getOntProperty(objBody.getAsString("p")).getDomain().toString());
        propCharacteristics.put("pRange", ontModel.getOntProperty(objBody.getAsString("p")).getRange().toString());

        if (ontModel.getOntProperty(objBody.getAsString("s")).getLocalName().equals(ontModel.getOntProperty(objBody.getAsString("p")).getLocalName())) {
            propCharacteristics.put("hasSameName", "TRUE");
        } else {
            propCharacteristics.put("hasSameName", "FALSE");
        }
        ontModel.close();
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
        return propCharacteristics;
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

    public void initAlignmentTables() {
        List<String> propertyTableAttributes = new ArrayList<>();
        //propertyTableAttributes.add("id");
        propertyTableAttributes.add("PropertyA");
        propertyTableAttributes.add("PropertyB");
        propertyTableAttributes.add("DomainPropA");
        propertyTableAttributes.add("DomainPropB");
        propertyTableAttributes.add("RangePropA");
        propertyTableAttributes.add("RangePropB");
        propertyTableAttributes.add("AlignmentType");
        propertyTableAttributes.add("hasSameName");
        propertyTableAttributes.add("actionType");

        List<String> classTableAttributes = new ArrayList<>();
        //classTableAttributes.add("id");
        classTableAttributes.add("classA");
        classTableAttributes.add("classB");
        classTableAttributes.add("countPropClassA");
        classTableAttributes.add("countPropClassB");
        classTableAttributes.add("listPropClassA");
        classTableAttributes.add("listPropClassB");
        classTableAttributes.add("actionType");

        SQLiteUtils.createTable("Property", propertyTableAttributes);
        SQLiteUtils.createTable("Class", classTableAttributes);

    }
}