package com.genesis.xml.bolster.ontomatchmerge.extraction;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;

import java.io.OutputStream;
import java.util.Properties;

import com.genesis.xml.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import com.genesis.xml.bolster.ontomatchmerge.extraction.mappings.io.SourceMappingXMLWriter;

/**
 *
 * @author Rizkallah
 */
public abstract class ExtractionProcess {

    private OntModel ontModel;
    private String ontologyType;

    public ExtractionProcess() {
        ontModel = ModelFactory.createOntologyModel();
    }
    
    public void init(Properties params) {
        if (params.get("ontologyType") != null) {
            if (params.get("ontologyType").equals("PHYSICAL_ONTOLOGY")) {
                this.ontologyType = "PHYSICAL_ONTOLOGY";
            }
            if (params.get("ontologyType").equals("LOGICAL_ONTOLOGY")) {
                this.ontologyType = "LOGICAL_ONTOLOGY";
            }
        }else{
            this.ontologyType = "PHYSICAL_ONTOLOGY";
        }
    }

    /**
     * This method is the main method that converts the input data source into an OWL ontology.
     * It must be implemented by all subclasses of this abstract class.
     */
    public abstract void extractOwl(IOArtifact source);

    public void writeOntologyModel(OutputStream os, String format) throws IOException {
        this.ontModel.write(os, format);
        os.close();
    }

    public void writeSourceMappings(OutputStream os) throws Exception {
        SourceMappingXMLWriter writer = new SourceMappingXMLWriter( os);
        writer.writeXML();
        os.close();
    }

    public OntModel getOntModel() {
        return ontModel;
    }

    public void setOntModel(OntModel ontModel) {
        this.ontModel = ontModel;
    }
    public String getOntologyType() {
        return ontologyType;
    }

    public void setOntologyType(String ontologyType) {
        this.ontologyType = ontologyType;
    }
}
