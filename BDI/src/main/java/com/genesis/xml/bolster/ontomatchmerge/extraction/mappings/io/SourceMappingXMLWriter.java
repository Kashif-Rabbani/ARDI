/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.genesis.xml.bolster.ontomatchmerge.extraction.mappings.io;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Rizkallah
 */
public class SourceMappingXMLWriter {

    //private SourceMappingStruct sourceMappings;
    private OutputStream oStream;
    
    public SourceMappingXMLWriter() {
        //this.sourceMappings = new SourceMappingStruct();
        this.oStream = null;
    }
    
    public SourceMappingXMLWriter(String filePath) {
        try {
            this.oStream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public SourceMappingXMLWriter( OutputStream os) {
        this.oStream = os;
    }
    
    public void writeXML() throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        // Add DOCTYPE Declaration
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(this.oStream));
        writer.println(createXMLDeclaration());
        writer.println(createDTD());
        writer.flush();
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElem = doc.createElement("OntologyMappings");
        doc.appendChild(rootElem);
        
        // Write content into XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        DOMSource source = new DOMSource(doc);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(source, new StreamResult(this.oStream));
    }

    private String createXMLDeclaration() {
     return MessageFormat.format(
          "<?xml version=\"1.0\" encoding=\"{0}\" standalone=\"yes\" ?>",
          "UTF-8");
    }
    
    private String createDTD() {
     return "<!DOCTYPE OntologyMappings [<!ELEMENT OntologyMappings (Connections?, OntologyMapping+)>\n"
            + "<!ELEMENT Connections (Connection*)>\n"
            + "<!ELEMENT Connection (name, server, type, access+, database, port, username, password)>\n"
            + "<!ELEMENT name (#PCDATA)>\n"
            + "<!ELEMENT server (#PCDATA)>\n"
            + "<!ELEMENT type (#PCDATA)>\n"
            + "<!ELEMENT access (#PCDATA)>\n"
            + "<!ELEMENT database (#PCDATA)>\n"
            + "<!ELEMENT port (#PCDATA)>\n"
            + "<!ELEMENT username (#PCDATA)>\n"
            + "<!ELEMENT password (#PCDATA)>\n"
            + "<!ELEMENT OntologyMapping (Ontology,RefOntology?,Mapping)>\n"
            + "<!ELEMENT SQLOperator (#PCDATA)>\n"
            + "<!ELEMENT Ontology (#PCDATA)>\n"
            + "<!ELEMENT RefOntology (#PCDATA)>\n"
            + "<!ELEMENT Mapping (((Mapping,SQLOperator)*,Mapping)|(Tablename,Projections,Selections?))>\n"
            + "<!ELEMENT Tablename (#PCDATA)>\n"
            + "<!ELEMENT Projections (Attribute+)>\n"
            + "<!ELEMENT Attribute (#PCDATA)>\n"
            + "<!ELEMENT Selections (Selection+)>\n"
            + "<!ELEMENT Selection (Column,Operator,Constant)>\n"
            + "<!ELEMENT Column (#PCDATA)>\n"
            + "<!ELEMENT Operator (#PCDATA)>\n"
            + "<!ELEMENT Constant (#PCDATA)>\n"
            + "<!ATTLIST Mapping connectionName CDATA #IMPLIED>\n"
            + "<!ATTLIST Mapping sourceKind CDATA #IMPLIED>\n"
            + "<!ATTLIST Ontology type (concept|property) #REQUIRED>\n"
            + "<!ATTLIST RefOntology type (concept|property) #REQUIRED>\n"
            + "]>\n";
    }

}
