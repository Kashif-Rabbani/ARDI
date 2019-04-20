package com.genesis.main;

import com.genesis.clienteRest.ClientDB;
import com.genesis.rdf.model.bdi_ontology.JsonSchemaExtractor;
import com.genesis.resources.exceptions.ExceptionBadRequest;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author Javier Flores
 */
public class JsonToRDF {
    
   public String ontology_extraction_json(String body) throws IOException {
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        JSONObject content = new JSONObject();
        System.out.println("Body:"+body);

       try {
           //OLD
           //            System.out.println("Event: "+objBody.getAsString("event"));
           //            System.out.println("schemaVersion: "+objBody.getAsString("schemaVersion"));
           //            System.out.println("instances: "+objBody.getAsString("instances"));
           //            content = newRelease(objBody.getAsString("event"),objBody.getAsString("schemaVersion"),objBody.getAsString("instances"));

           content = JsonSchemaExtractor.extract_schema("ESO", objBody.toJSONString());
        } catch (NullPointerException e) {
             e.printStackTrace();
             throw new ExceptionBadRequest("Source is invalid. Check that source is the right format.");
        }

        if (content.containsKey("kafkaTopic")) {
            System.out.println(objBody.getAsString("kafkaTopic"));
            try{
            if (!objBody.getAsString("kafkaTopic").isEmpty()) {
                objBody.put("kafkaTopic", objBody.getAsString("kafkaTopic"));
            } else {
                objBody.put("kafkaTopic", content.getAsString("kafkaTopic"));
            }
            }catch(Exception e){
                System.out.println("catch");
                objBody.put("kafkaTopic", content.getAsString("kafkaTopic"));
            }

            objBody.put("releaseID", UUID.randomUUID().toString());

            // If we have to dispatch to the Data Lake, generate a random path (.txt for now)
            if (Boolean.parseBoolean(objBody.getAsString("dispatch"))) {
                // TODO replace with path to HDFS
                String dispatcherPath = "/home/snadal/Bolster/DispatcherData/"+UUID.randomUUID().toString()+".txt";
                //Files.touch(new File(dispatcherPath));
                objBody.put("dispatcherPath", dispatcherPath);
            } else {
                objBody.put("dispatcherPath", "");
            }
            System.out.println("Output: "+objBody.toJSONString());
            ClientDB cl = new ClientDB();
            cl.save(objBody.toJSONString());
            //getReleasesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        }
        System.out.println("Output2: "+content.toJSONString());
        JSONObject contentJson = (JSONObject) JSONValue.parse(content.toJSONString());
        return contentJson.getAsString("rdf");
    }
    
}
