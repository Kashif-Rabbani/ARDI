package com.genesis.resources;

import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.SQLiteUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashMap;

public class AlignmentAlgorithm {
    private JSONObject basicInfo;

    public AlignmentAlgorithm(JSONObject obj) {
        this.basicInfo = obj;
        alignClasses();
        alignProperties();
    }

    private void alignProperties() {
        JSONArray propertiesData = SQLiteUtils.executeSelect("SELECT * FROM Property", SchemaIntegrationHelper.getPropertyTableFeatures());
        propertiesData.forEach(node -> {
            HashMap<String, String> data = new HashMap<>();

            Object[] row = ((JSONArray) node).toArray();
            for (Object element : row) {
                JSONObject obj = (JSONObject) element;
                data.put(obj.getAsString("feature"), obj.getAsString("value"));
            }

            switch (data.get("actionType")) {
                case "ACCEPTED":
                    String query = "SELECT * FROM Class WHERE classA = '" + data.get("DomainPropA") + "' and classB = '" + data.get("DomainPropB") + "'";
                    System.out.println(query);
                    JSONArray result = SQLiteUtils.executeSelect(query,
                            SchemaIntegrationHelper.getClassTableFeatures());
                    if (result.size() > 0) {
                        System.out.println("CLASSES PRESENT");
                        // Remove Properties from aligned Classes
                        RDFUtil.removeProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyA"), data.get("DomainPropA"), data.get("RangePropA"));
                        RDFUtil.removeProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyB"), data.get("DomainPropB"), data.get("RangePropB"));

                        // Add Property
                        //Move the Properties to the Parent class
                        String domainOfNewProperty = basicInfo.getAsString("integratedIRI") + "/"
                                + ResourceFactory.createResource(data.get("DomainPropA")).getLocalName() + "_"
                                + ResourceFactory.createResource(data.get("DomainPropB")).getLocalName();
                        RDFUtil.addProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyA"),domainOfNewProperty,data.get("RangePropA") );

                    }
                    break;
                case "REJECTED":
                    break;
            }
        });
    }


    private void alignClasses() {
        JSONArray classesData = SQLiteUtils.executeSelect("SELECT * FROM Class", SchemaIntegrationHelper.getClassTableFeatures());
        classesData.forEach(node -> {
            Object[] row = ((JSONArray) node).toArray();
            HashMap<String, String> classRow = new HashMap<>();
            for (Object element : row) {
                JSONObject obj = (JSONObject) element;
                classRow.put(obj.getAsString("feature"), obj.getAsString("value"));
            }

            switch (classRow.get("actionType")) {
                case "ACCEPTED":
                    String newGlobalGraphClassResource = "";
                    Resource classA = ResourceFactory.createResource(classRow.get("classA"));
                    Resource classB = ResourceFactory.createResource(classRow.get("classB"));

                    if (basicInfo.getAsString("integrationType").equals("LOCAL-vs-LOCAL")) {
                        //newGlobalGraphClassResource = integratedIRI + "/" + classA.getURI().split(Namespaces.Schema.val())[1];
                        newGlobalGraphClassResource = basicInfo.getAsString("integratedIRI") + "/" + classA.getLocalName() + "_" + classB.getLocalName();
                    }

                    RDFUtil.addClassOrPropertyTriple(basicInfo.getAsString("integratedIRI"), newGlobalGraphClassResource, "CLASS");
                    RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), classRow.get("classA"), "SUB_CLASS_OF", newGlobalGraphClassResource);
                    RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), classRow.get("classB"), "SUB_CLASS_OF", newGlobalGraphClassResource);

                    break;
                case "REJECTED":
                    break;
            }


        });
    }
}
