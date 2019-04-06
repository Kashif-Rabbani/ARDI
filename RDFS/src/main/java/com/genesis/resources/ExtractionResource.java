package com.genesis.resources;

import com.genesis.clienteRest.ClientConversor;
import com.genesis.clienteRest.ClientDB;
import com.genesis.main.JsonToRDF;
import com.genesis.main.XmlToRDF;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * REST Web Service
 *
 * @author Javier Flores
 */
@Path("extraction")
public class ExtractionResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getText() throws IOException {
        return "RDF Resource";
    }
    
    /**
     * Consigue los nombres de las APIs
     * @return
     */
    @GET
    @Path("releases")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String GET_all_releases() {
        System.out.println("ESO: [GET /releases/]");
        ClientDB cl = new ClientDB();
        return cl.getAllReleases(); 
    }
    
    /**
     * Consigue todas los sources de una API en especifico
     * @param apiName nombre de la API
     * @return lista de sources
     * @throws java.io.UnsupportedEncodingException
     */
    @GET
    @Path("releases/{apiName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String GET_all_sources(@PathParam("apiName") String apiName) throws UnsupportedEncodingException {
        System.out.println("ESO: [GET /releases/"+apiName+"]");
        ClientDB cl = new ClientDB();
        return cl.getAllSources(apiName); 
    }
    
    /**
     * Elimina una fuente de MongoDB y Jena TDB
     * @param graph grafo del source
     * @return
     */
    @DELETE @Path("release/{graph}")
    @Consumes("text/plain")
    public Response DELETE_artifacts(@PathParam("graph") String graph) {
        System.out.println("ESO: [DELETE /release/"+graph);
        ClientDB cl = new ClientDB();
         cl.deleteArtifact(graph);
        return Response.ok().build();
        
    }
    /**
     * Guarda un source en MongoDB y Jena TDB
     * @param typeFormat formato de source. Puede ser "xml" o "json"
     * @param graph URI del grafo
     * @param body source
     * @return
     * @throws IOException
     */
    @POST @Path("release/{typeFormat}/{graph}")
    @Consumes("text/plain")
    public Response POST_release(@PathParam("typeFormat") String typeFormat,@PathParam("graph") String graph, String body) throws IOException {
        System.out.println("ESO: [release/"+typeFormat+"/"+graph);
        String response = null;
        ClientDB cl = new ClientDB();

        switch(typeFormat.toUpperCase()){
            case "JSON":
                        JsonToRDF m = new JsonToRDF();
                        response = m.ontology_extraction_json(body);
                        cl.postArtifact(graph, response,"N-TRIPLE");
                break;
            case "XML":
                        XmlToRDF a = new XmlToRDF();
                        response = a.ontology_extraction_xml(body);
                        cl.postArtifact(graph, response,"default");
                break;
            default:
                System.out.println("Default");
                break;
        }
        return Response.ok(response).build();
                
    }

    /**
     *  Regresa las triplas obtenidas de Jena TDB
     * @param artifactType SOURCE 
     * @param graph URI del grafo
     * @return triplas
     * @throws UnsupportedEncodingException
     */
    @GET @Path("artifacts/{artifactType}/{graph}/content")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String GET_artifact_content(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) throws UnsupportedEncodingException {
       System.out.println("ESO: Getting graph "+graph+"...");
        ClientDB cl = new ClientDB();
        return cl.getGraphContent(artifactType,graph);        
    }
    
    /**
     *
     * @param artifacType SOURCE
     * @param graph URI del grafo
     * @return código d3
     * @throws UnsupportedEncodingException
     */
    @GET @Path("artifactsToD3/{artifactType}/{graph}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String GET_artifact_d3(@PathParam("artifactType") String artifacType,@PathParam("graph") String graph) throws UnsupportedEncodingException {
       System.out.println("ESO: Getting graph "+graph+"...");
        ClientDB cl = new ClientDB();
        
        
        return  ClientConversor.getD3(artifacType, graph);        
    }    
    
    /**
     * Elimina una API con todas sus fuentes.
     * @param apiName nombre de la API
     * @return
     */
    @DELETE @Path("api/{apiName}")
    @Consumes("text/plain")
    public Response DELETE_API_sources(@PathParam("apiName") String apiName) {
        System.out.println("ESO: [DELETE /api/"+apiName);
        ClientDB cl = new ClientDB();
        cl.deleteAPI(apiName);
            return Response.ok().build();
    }
}