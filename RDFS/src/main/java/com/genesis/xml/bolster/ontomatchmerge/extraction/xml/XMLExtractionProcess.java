package com.genesis.xml.bolster.ontomatchmerge.extraction.xml;

import java.io.File;
import java.util.Properties;

import com.hp.hpl.jena.ontology.OntModel;

import com.genesis.xml.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import com.genesis.xml.tr.com.srdc.ontmalizer.XSD2OWLMapper;

import com.genesis.xml.bolster.ontomatchmerge.extraction.ExtractionProcess;
import com.genesis.xml.bolster.ontomatchmerge.extraction.ioartifacts.XMLIOArtifact;

/**
 *
 * @author Rizkallah
 * Edited by Javier Flores
 */

/**
 * Restriction on input to XMLExtractionProcess:
 - XSD does not contain two elements with the same name but with different types
 - All XML instances must be valid to the schema (XMLExtractionProcess does not check for validity)
 */
public class XMLExtractionProcess extends ExtractionProcess {


    public XMLExtractionProcess () {

    }
    
    @Override
    public void init (Properties params) {
        super.init(params);
    }

    @Override
    public void extractOwl (IOArtifact source) {
        XMLIOArtifact xmlSource = (XMLIOArtifact)source;
        // Resulting ontology model
        OntModel ontModel;
        
        // 1. Convert XSD Schema to OWL
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File(xmlSource.getXmlSchemaPath()));
        //mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();
        ontModel = mapping.getOntology();
        
        setOntModel(ontModel);
    }
}
