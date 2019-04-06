package com.genesis.xml.bolster.ontomatchmerge.extraction;

import java.io.FileOutputStream;
import java.util.Properties;

import com.genesis.xml.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import com.genesis.xml.bolster.ontomatchmerge.extraction.xml.XMLExtractionProcess;

/**
 *
 * @author Rizkallah
 * Edited by Javier Flores
 */
public abstract class OntologyExtractionCoordinator {
    
    public static String ONTOLOGY_SERIALIZATION_FORMAT = "RDF/XML-ABBREV";
    
    public OntologyExtractionCoordinator() {
    }
    
    public static void extractOntology(IOArtifact source, Properties params) {
        try {
            ExtractionProcess ep = null;
            ep = new XMLExtractionProcess();
            // Run the extraction process
            ep.init(params);
            ep.extractOwl(source);
            source.setOutputOntologyUri(ep.getOntModel().getNsPrefixURI(""));
            ep.writeOntologyModel(new FileOutputStream(source.getOutputOntologyPath()), 
                                        ONTOLOGY_SERIALIZATION_FORMAT);

            if (source.getSourceMappingsPath() != null && !source.getSourceMappingsPath().isEmpty()) {
                ep.writeSourceMappings(new FileOutputStream(source.getSourceMappingsPath()));
            }

        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    
}
