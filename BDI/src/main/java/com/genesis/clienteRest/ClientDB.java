/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.genesis.clienteRest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Javier
 */
public class ClientDB {
    
    public boolean save(String data){
        System.out.println("ESO: Saving to Mongo");
         Client client = Client.create();
         WebResource webResource = client.resource(ConfigManager.getProperty("system_db_url")+"/releases");
         
       //   MultivaluedMap formData = new MultivaluedMapImpl();
        //formData.add("data", data);
        ClientResponse response = webResource.type("text/plain").post(ClientResponse.class, data);
         return true;
    }
    
    
    public String getAllReleases(){
        System.out.println("ESO: Getting all releases");
         Client client = Client.create();
         WebResource webResource = client.resource(ConfigManager.getProperty("system_db_url")+"/releases");
        return webResource.get(String.class);
    }
    
    public String getAllSources(String apiname) throws UnsupportedEncodingException{
        System.out.println("ESO: Getting all sources");
         Client client = Client.create();
         WebResource webResource = client.resource(ConfigManager.getProperty("system_db_url")+"/releases").path(apiname);
        return webResource.get(String.class);
    }
    
    public ClientResponse postArtifact(String graph,String rdf, String format){
        System.out.println("ESO: post artifact");
         Client client = Client.create();
         WebResource webResource = null;
        try {        
            webResource = client.resource(ConfigManager.getProperty("system_db_url")+"/artifacts/"+format).path(URLEncoder.encode(graph, "UTF-8"));
            System.out.println(webResource.getURI());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ClientDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return webResource.type("text/plain").post(ClientResponse.class, rdf);
        
    }
    
    public ClientResponse deleteArtifact(String graph){
        System.out.println("ESO: deleteArtifact");
         Client client = Client.create();
         WebResource webResource = null;
        try {
            
            webResource = client.resource(ConfigManager.getProperty("system_db_url")+"/artifacts").path(URLEncoder.encode(graph, "UTF-8"));
            //System.out.println(webResource.getURI());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ClientDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return webResource.type("text/plain").delete(ClientResponse.class);
        
    }
    
    public ClientResponse deleteAPI(String apiname){
        System.out.println("ESO: deleteAPI");
         Client client = Client.create();
         WebResource webResource = null;
         webResource = client.resource(ConfigManager.getProperty("system_db_url")+"/api").path(apiname);
         System.out.println(webResource.getURI());
        ClientResponse a = webResource.delete(ClientResponse.class);
        //System.out.println("Estatus: "+a.getStatus());
         return a;
        
    }
    
    
    
    public String getGraphContent(String artifacType, String graph) throws UnsupportedEncodingException{
        System.out.println("ESO: Getting graph content...");
         Client client = Client.create();
         WebResource webResource = client.resource(ConfigManager.getProperty("system_db_url")+"/artifacts").path(artifacType).path(URLEncoder.encode(graph, "UTF-8")).path("content");
        //System.out.println(webResource.getURI());
         return webResource.get(String.class);

    }
    
}
