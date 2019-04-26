package com.genesis.eso.util;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.genesis.rdf.model.bdi_ontology.*;
import org.semarglproject.vocab.OWL;

/**
 * Created by snadal on 24/11/16.
 */
public class RDFUtil {

    public static String sparqlQueryPrefixes =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
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

    public static void testing(String iri) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        System.out.println("INSIDE TESTING AND IRI IS : " + iri);
        Model graph = ds.getNamedModel(iri);


        Resource r = graph.getResource("http://www.bdiontology.com/schema/BicycleAuto/Bicycle/Bicycle_Collection");

        StmtIterator iter = graph.listStatements();
        while (iter.hasNext()) {
            Statement stmt = (Statement) iter.next();
            System.out.println(stmt.getSubject() + " " + stmt.getObject() + " " + stmt.getPredicate());
        }
        System.out.println(" getLocalName" + r.getLocalName() + "\n" +
                " getURI" + r.getURI() + "\n" +
                " getNameSpace" + r.getNameSpace() + "\n" +
                "listProperties " + r.listProperties().toString() + "\n" +
                " " + r + "\n"
        );


        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void addCustomPropertyTriple(String namedGraph, String s, String p, String o) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        if (p.equals("EQUIVALENT_CLASS"))
            graph.add(new ResourceImpl(s), new PropertyImpl(OWL.EQUIVALENT_CLASS), new ResourceImpl(o));
        if (p.equals("EQUIVALENT_PROPERTY"))
            graph.add(new ResourceImpl(s), new PropertyImpl(OWL.EQUIVALENT_PROPERTY), new ResourceImpl(o));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }


    public static void addBatchOfTriples(String namedGraph, List<Tuple3<String, String, String>> triples) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        for (Tuple3<String, String, String> t : triples) {
            graph.add(new ResourceImpl(t._1), new PropertyImpl(t._2), new ResourceImpl(t._3));
        }
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
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
