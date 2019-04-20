/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.genesis.clienteRest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Javier
 */
public class ClientConversor {
    
    public static String getD3(String artifactType,String graph) throws UnsupportedEncodingException{
         System.out.println("ESO: ToD3");
         Client client = Client.create();
         WebResource webResource = null;
         webResource = client.resource(ConfigManager.getProperty("conversor_url")+"/artifactToD3").path(artifactType).path(URLEncoder.encode(graph, "UTF-8"));
         //System.out.println(webResource.getURI());
        
         return webResource.get(String.class);
    }
    
    
}
