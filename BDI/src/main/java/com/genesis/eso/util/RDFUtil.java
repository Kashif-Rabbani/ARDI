package com.genesis.eso.util;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.genesis.rdf.model.bdi_ontology.*;
import org.apache.jena.vocabulary.RDF;
import org.semarglproject.vocab.OWL;
import org.semarglproject.vocab.RDFS;

/**
 * Created by snadal on 24/11/16.
 * Updated by Kashif-Rabbani 01-03-2019
 */
public class RDFUtil {

    public static String sparqlQueryPrefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";

    public static void addTriple(OntModel model, String s, String p, String o) {
        model.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
    }

    public static void addTriple(Model model, String s, String p, String o) {
        System.out.println("inserting triple <" + s + ", " + p + ", " + o + ">");
        model.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
    }


    public static void addTriple(String namedGraph, String s, String p, String o) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void addClassOrPropertyTriple(String namedGraph, String s, String p) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        if (p.equals("CLASS"))
            graph.add(new ResourceImpl(s), RDF.type, new ResourceImpl(RDFS.CLASS));
        if (p.equals("PROPERTY"))
            graph.add(new ResourceImpl(s), RDF.type, RDF.Property);
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void addCustomTriple(String namedGraph, String s, String p, String o) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        if (p.equals("EQUIVALENT_CLASS"))
            graph.add(new ResourceImpl(s), new PropertyImpl(OWL.EQUIVALENT_CLASS), new ResourceImpl(o));
        if (p.equals("SUB_CLASS_OF"))
            graph.add(new ResourceImpl(s), new PropertyImpl(RDFS.SUB_CLASS_OF), new ResourceImpl(o));
        if (p.equals("EQUIVALENT_PROPERTY"))
            graph.add(new ResourceImpl(s), new PropertyImpl(OWL.EQUIVALENT_PROPERTY), new ResourceImpl(o));
        if (p.equals("DOMAIN"))
            graph.add(new ResourceImpl(s), new PropertyImpl(RDFS.DOMAIN), new ResourceImpl(o));
        if (p.equals("RANGE"))
            graph.add(new ResourceImpl(s), new PropertyImpl(RDFS.RANGE), new ResourceImpl(o));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void removeTriple(String namedGraph, String s, String p, String o) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        //OntModel ontModel = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
        //ontModel.addSubModel(graph);

        graph.remove(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));

        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }


    public static void removeProperty(String namedGraph, String property, String domain, String range) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.remove(new ResourceImpl(property), RDF.type, RDF.Property);
        graph.remove(new ResourceImpl(property), new PropertyImpl(RDFS.DOMAIN), new ResourceImpl(domain));
        graph.remove(new ResourceImpl(property), new PropertyImpl(RDFS.RANGE), new ResourceImpl(range));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }


    public static void addProperty(String namedGraph, String property, String domain, String range) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.add(new ResourceImpl(property), RDF.type, RDF.Property);
        graph.add(new ResourceImpl(property), new PropertyImpl(RDFS.DOMAIN), new ResourceImpl(domain));
        graph.add(new ResourceImpl(property), new PropertyImpl(RDFS.RANGE), new ResourceImpl(range));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }


    public static void addPropertyDomain(String namedGraph, String property, String domain) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.add(new ResourceImpl(property), new PropertyImpl(RDFS.DOMAIN), new ResourceImpl(domain));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void addProperty(String namedGraph, String property, String[] domains, String range) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.add(new ResourceImpl(property), RDF.type, RDF.Property);

        for(String domain : domains) {
            graph.add(new ResourceImpl(property), new PropertyImpl(RDFS.DOMAIN), new ResourceImpl(domain));
        }
        graph.add(new ResourceImpl(property), new PropertyImpl(RDFS.RANGE), new ResourceImpl(range));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }


    public static void addBatchOfTriples(String namedGraph, List<Tuple3<String, String, String>> triples) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        System.out.println("DS.CONTAINS-NAMED-MODEL:  " + ds.containsNamedModel(namedGraph));
        System.out.println("Named Graph: " + namedGraph);
        if (ds.containsNamedModel(namedGraph)) {
            ds.removeNamedModel(namedGraph);
        }
        Model graph = ds.getNamedModel(namedGraph);
        for (Tuple3<String, String, String> t : triples) {
            graph.add(new ResourceImpl(t._1), new PropertyImpl(t._2), new ResourceImpl(t._3));
        }
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static boolean isNamedGraphAlreadyExists(String namedGraph) {
        boolean flag = false;
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        if (ds.containsNamedModel(namedGraph)) {
            flag = true;
        }
        ds.commit();
        ds.close();
        return flag;
    }


    public static boolean removeNamedGraph(String namedGraph) {
        boolean flag;
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            ds.removeNamedModel(namedGraph);

            flag = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            flag = false;
        }
        ds.commit();
        ds.close();
        return flag;
    }


    public static ResultSet runAQuery(String sparqlQuery, String namedGraph) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), ds)) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet runAQuery(String sparqlQuery, Dataset ds) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), ds)) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet runAQuery(String sparqlQuery, InfModel o) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), o)) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Short name
    public static String nn(String s) {
        return noNamespace(s);
    }

    public static String noNamespace(String s) {
        return s.replace(Namespaces.G.val(), "")
                .replace(Namespaces.S.val(), "")
                .replace(Namespaces.sup.val(), "")
                .replace(Namespaces.rdfs.val(), "")
                .replace(Namespaces.owl.val(), "");
    }

    public static String getRDFString(String namedGraph) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.READ);
        Model graph = ds.getNamedModel(namedGraph);

        // Output RDF
        String tempFileForO = TempFiles.getTempFile();
        try {
            graph.write(new FileOutputStream(tempFileForO), "RDF/XML-ABBREV");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String content = "";
        try {
            content = new String(java.nio.file.Files.readAllBytes(new java.io.File(tempFileForO).toPath()));
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        graph.close();
        ds.close();

        return content;
    }

/*
    public static String convertToURI(String name) {
        //If it is a semantic annotation, add the right URI
        if (name.equals("hasFeature")) {
            return GlobalGraph.HAS_FEATURE.val();
        }
        else if (name.equals("subClass") || name.equals("subClassOf")) {
            return Namespaces.rdfs.val()+"subClassOf";
        }
        else if (name.equals("ID") || name.equals("identifier")) {
            return Namespaces.sc.val() + "identifier";
        }

        //Otherwise, just add the SUPERSEDE one
        return Namespaces.sup.val()+name;
    }
*/


}
