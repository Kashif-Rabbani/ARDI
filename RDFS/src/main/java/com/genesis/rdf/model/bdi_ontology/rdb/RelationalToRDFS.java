package com.genesis.rdf.model.bdi_ontology.rdb;

import com.genesis.eso.util.AttributeUtil;
import com.genesis.eso.util.TempFiles;
import com.genesis.rdf.model.bdi_ontology.JsonSchemaExtractor;
import com.genesis.rdf.model.bdi_ontology.NewNamespaces2;
import com.genesis.rdf.model.bdi_ontology.metamodel.NewSourceLevel2;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.util.Objects;

import static com.genesis.eso.util.NewRDFUtil.addTriple;

public class RelationalToRDFS {
    private static OntModel model;
    private static final String LANG = "TURTLE"; //"RDF/XML");//"N-TRIPLE");
    private OntModel schemaModel = null;
    private String root = null;

    public RelationalToRDFS() {
        try {
            model = ModelFactory.createOntologyModel();
            String path = Objects.requireNonNull(JsonSchemaExtractor.class.getClassLoader().getResource("model/new_metadata_model2.owl")).getPath();

            model.read(new File(path).toURI().toURL().toString());
            //init Schema Model
            schemaModel = ModelFactory.createOntologyModel();
            schemaModel.setNsPrefix(NewNamespaces2.rdfs.name(), NewNamespaces2.rdfs.val());

            root = NewSourceLevel2.ROOT.val() + "/" + AbstractDB.getDbName();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void schemaModelToFile() {
        String outputFile = TempFiles.getIncrementalTempFile(AbstractDB.getDbName());
        try {
            assert schemaModel != null;
            schemaModel.write(new FileOutputStream(outputFile), LANG);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void tablesToRDFS(String tableName) {
        addTriple(schemaModel, root + "/" + tableName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
    }

    public void columnsToRDFS(String tableName, String columnName, String columnDataType) {
        addTriple(schemaModel, root + "/" + tableName + "/" + columnName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
        addTriple(schemaModel, root + "/" + tableName + "/" + columnName, NewSourceLevel2.RDFSDomain, root + "/" + tableName);
        addTriple(schemaModel, root + "/" + tableName + "/" + columnName, NewSourceLevel2.RDFSRange, AttributeUtil.getRdbColumnDataTypeURI(columnDataType));
    }

    public void primaryKeysToRDFS(String tableName, String columnName) {
        addTriple(schemaModel, root + "/" + "PrimaryKeys", NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);

        addTriple(schemaModel, root + "/" + tableName + "/" + columnName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
        addTriple(schemaModel, root + "/" + tableName + "/" + columnName, NewSourceLevel2.RDFSDomain, root + "/" + "PrimaryKeys");
        addTriple(schemaModel, root + "/" + tableName + "/" + columnName, NewSourceLevel2.RDFSRange, root + "/" + tableName);
    }

    public void foreignKeysToRDFS(String pkTableName, String pkColumnName, String fkTableName, String fkColumnName) {
        addTriple(schemaModel, root + "/" + fkTableName + "/" + fkColumnName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
        //addTriple(schemaModel, root + "/" + fkTableName + "/" + fkColumnName, NewSourceLevel2.RDFSDomain, root + "/" + root + "/" + fkTableName);
        addTriple(schemaModel, root + "/" + fkTableName + "/" + fkColumnName, NewSourceLevel2.RDFSRange, root + "/" + pkTableName);
    }
}
