package com.genesis.metamodel;

import com.genesis.rdf.model.bdi_ontology.*;

/**
 * Created by snadal on 22/11/16.
 */
public enum Mappings {

    MAPS_TO(Namespaces.M.val()+"mapsTo");

    private String element;

    Mappings(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}
