package com.genesis.eso.util;

import com.genesis.rdf.model.bdi_ontology.metamodel.NewSourceLevel2;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import java.net.URLEncoder;

public class NewRDFUtil {
	public static void addTriple(OntModel model, String subject, NewSourceLevel2 predicate, OntClass object) {
		model.add(new ResourceImpl(subject.replaceAll(" ", "")), new PropertyImpl(predicate.val()), new ResourceImpl(object.toString().replaceAll(" ", "")));
	}

	public static void addTriple(OntModel model, String subject, ObjectProperty predicate, String object) {
		model.add(new ResourceImpl(subject.replaceAll(" ", "")), new PropertyImpl(predicate.toString()), new ResourceImpl(object.replaceAll(" ", "")));
	}
	//added to handle NewsourceLevel 
	public static void addTriple(OntModel model, String subject, NewSourceLevel2 predicate, String object) {
		model.add(new ResourceImpl(subject.replaceAll(" ", "")), new PropertyImpl(predicate.val()), new ResourceImpl(object.replaceAll(" ", "")));
	}
	//added to handle  news2
		public static void addTriple(OntModel model, String subject, NewSourceLevel2 predicate, NewSourceLevel2 predicate1) {
			model.add(new ResourceImpl(subject.replaceAll(" ", "")), new PropertyImpl(predicate.val().replaceAll(" ", "")), new ResourceImpl(predicate1.val().replaceAll(" ", "")));
		}
	
	public static void removeTriple(OntModel model, String subject, NewSourceLevel2 predicate, String object) {
		model.remove(new ResourceImpl(subject), new PropertyImpl(predicate.val()), new ResourceImpl(object.toString()));
	}

	public static void removeTriple(OntModel model, String subject, NewSourceLevel2 predicate) {
		model.removeAll(new ResourceImpl(subject), new PropertyImpl(predicate.val()), null);
	}
}
