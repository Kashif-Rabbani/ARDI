package com.genesis.main;

import com.genesis.clienteRest.ClientDB;
import com.genesis.eso.util.TempFiles;
import static com.genesis.rdf.model.bdi_ontology.Release.newRelease;
import java.io.IOException;
import java.nio.file.Files;

import com.genesis.xml.bolster.ontomatchmerge.extraction.OntologyExtractionCoordinator;
import com.genesis.xml.bolster.ontomatchmerge.extraction.ioartifacts.XMLIOArtifact;
import java.io.File;
import java.util.Properties;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class XmlToRDF {

    public String ontology_extraction_xml(String JSON_dataset_XML) {
        System.out.println("Extracting xml...");
        JSONObject objBody = (JSONObject) JSONValue.parse(JSON_dataset_XML);
        JSON_dataset_XML = objBody.getAsString("instances");
        ClientDB cl = new ClientDB();
        cl.save(objBody.toJSONString());
        
        String xsdPath = TempFiles.storeInTempFile(JSON_dataset_XML);
        XMLIOArtifact xmlSource = new XMLIOArtifact(xsdPath);
        xmlSource.setOutputOntologyPath(TempFiles.storeInTempFile(""));

        OntologyExtractionCoordinator.extractOntology(xmlSource, new Properties());

        String content = "";
        try {
            content = new String(Files.readAllBytes(new File(xmlSource.getOutputOntologyPath()).toPath()),"UTF-8");
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return content;
    }

}
