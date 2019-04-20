package com.genesis.rdf.parsers;


import com.genesis.rdf.model.bdi_ontology.Namespaces;
import com.genesis.rdf.model.bdi_ontology.metamodel.SourceLevel;
import com.genesis.eso.util.RDFUtil;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Resource;

/**
 * Created by snadal on 10/11/16. 
 * Edited by Javier Flores on 15/11/17
 */
public class JSON_to_SourceLevel {

    private static String getProperClass(JSONObject jsonDataset, String key) {
        if (jsonDataset.get(key) == null) return SourceLevel.RDFProperty.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONObject.class.getName())) return SourceLevel.EMBEDDED_OBJECT.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONArray.class.getName())) return SourceLevel.ARRAY.val();
        return SourceLevel.RDFProperty.val();
    }

    private static String getProperLink(JSONObject jsonDataset, String key) {
        if (jsonDataset.get(key) == null) return SourceLevel.HAS_ATTRIBUTE.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONObject.class.getName())) return SourceLevel.HAS_EMBEDDED_OBJECT.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONArray.class.getName())) return SourceLevel.HAS_ARRAY.val();
        return SourceLevel.HAS_ATTRIBUTE.val();
    }

    public static void extractRec(OntModel theModel, JSONObject jsonDataset, String parentElement) {
        jsonDataset.forEach((k,v) -> {

            if (jsonDataset.get(k) != null && jsonDataset.get(k).getClass().getName().equals(JSONArray.class.getName())){

                //Create a blank node
                Resource blankNode = theModel.createResource();
                
                RDFUtil.addTriple(theModel,parentElement+"/"+k, Namespaces.rdf.val()+"type",getProperClass(jsonDataset,k));
                
                RDFUtil.addTriple(theModel,parentElement,getProperLink(jsonDataset,k),parentElement+"/"+k);
                //Define blank node as ordered object
                RDFUtil.addTriple(theModel,blankNode.toString(), Namespaces.rdf.val()+"type",SourceLevel.ORDERED_OBJECT.val());
                //Link parent element to blank node
                RDFUtil.addTriple(theModel,parentElement+"/"+k,SourceLevel.HAS_ORDERED_OBJECT.val(),blankNode.toString());
                
                extractRec(theModel,extractArray((JSONArray) jsonDataset.get(k)),blankNode.toString());
                          
            }else{
                RDFUtil.addTriple(theModel,parentElement+"/"+k, Namespaces.rdf.val()+"type",getProperClass(jsonDataset,k));
                RDFUtil.addTriple(theModel,parentElement,getProperLink(jsonDataset,k),parentElement+"/"+k);
              
            }
        });
    }
      
    private static JSONObject extractArray(JSONArray jsonArray ){
       
        JSONObject jsonKey = new JSONObject();
        System.out.println("**"+jsonArray.toString());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = null;
            try{
             json = (JSONObject) jsonArray.get(i);
            }catch(Exception ev){
                //It's an array of values
                        return new JSONObject();
            }
            Set<String> keys = json.keySet();
            Iterator<String> ite = keys.iterator();
           
            while (ite.hasNext()) {
                String key = ite.next();

                if(!jsonKey.containsKey(key)){
                    
                  
                    try{
                        jsonKey.put(key,"" );
                    }catch(Exception e){
                        System.out.println("e:"+e);
                        JSONObject jsonKey2 = new JSONObject();
                        jsonKey2.put("","" );
                        return jsonKey2;
                    }
                    
                }
            }

        }
        return jsonKey;
    }

}
