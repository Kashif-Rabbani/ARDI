package com.genesis.metamodel;

import com.genesis.rdf.model.bdi_ontology.*;

/**
 * Created by snadal on 6/06/17.
 */
public enum GlobalGraph {

    CONCEPT(Namespaces.G.val()+"Concept"),
    FEATURE(Namespaces.G.val()+"Feature"),
    HAS_FEATURE(Namespaces.G.val()+"hasFeature"),
    INTEGRITY_CONSTRAINT(Namespaces.G.val()+"IntegrityConstraint"),
    HAS_INTEGRITY_CONSTRAINT(Namespaces.G.val()+"hasConstraint"),
    DATATYPE(Namespaces.rdfs.val()+"Datatype"),
    HAS_DATATYPE(Namespaces.G.val()+"hasDatatype"),


    PART_OF(Namespaces.G.val()+"partOf"),
    AGGREGATION_FUNCTION(Namespaces.G.val()+"aggregationFunction"),
    HAS_AGGREGATION_FUNCTION(Namespaces.G.val()+"hasAggregationFunction");



    private String element;

    GlobalGraph(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}
