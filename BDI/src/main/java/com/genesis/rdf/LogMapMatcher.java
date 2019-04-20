package com.genesis.rdf;

import com.genesis.eso.util.ConfigManager;
import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.Tuple3;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.List;
import java.util.Set;

public class LogMapMatcher {
    private String iri_onto1;
    private String iri_onto2;
    private String alignments_iri;
    private List<Tuple3<String, String, String>> alignmentsTriples;

    private LogMap2_Matcher logMap2_matcher = null;

    public LogMapMatcher(String iri_onto1, String iri_onto2, String alignments_iri) {
        this.iri_onto1 = iri_onto1;
        this.iri_onto2 = iri_onto2;
        this.alignments_iri = alignments_iri;

        startLogMapMatcher();
        extractMappings();
        storeMappingsInTripleStore();
    }

    private void startLogMapMatcher() {
        try {
            logMap2_matcher = new LogMap2_Matcher(
                    "file:" + iri_onto1,
                    "file:" + iri_onto2,
                    ConfigManager.getProperty("logmap_output_path"),
                    true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractMappings() {
        try {
            iterateOverMappings(logMap2_matcher.getLogmap2_Mappings());
            iterateOverMappings(logMap2_matcher.getLogmap2_DiscardedMappings());
            iterateOverMappings(logMap2_matcher.getLogmap2_HardDiscardedMappings());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeMappingsInTripleStore() {
        RDFUtil.addBatchOfTriples(alignments_iri, alignmentsTriples);
    }

    private void iterateOverMappings(Set<MappingObjectStr> mappings) throws Exception {

        for (MappingObjectStr mapping : mappings) {
            if (mapping.isClassMapping()) {
                alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence())));
                System.out.println(mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            } else if (mapping.isObjectPropertyMapping()) {
                alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence())));
                System.out.println(mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            } else if (mapping.isDataPropertyMapping()) {
                alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence())));
                System.out.println(mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            } else if (mapping.isInstanceMapping()) {
                alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence())));
                System.out.println(mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            }
        }
    }
}
