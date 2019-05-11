package com.genesis.main;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.genesis.eso.util.RDFUtil;
import com.genesis.eso.util.SQLiteUtils;
import com.genesis.eso.util.SqliteSafeDBWithPool;
import com.genesis.eso.util.Utils;
import com.genesis.rdf.model.bdi_ontology.JsonSchemaExtractor;
import com.genesis.rdf.model.bdi_ontology.XmlSchemaExtractor;
import com.genesis.rdf.model.bdi_ontology.rdb.MySqlDB;
import com.genesis.resources.SchemaIntegrationHelper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import uk.ac.ox.krr.logmap2.*;
import uk.ac.ox.krr.logmap2.io.FlatAlignmentFormat;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

public class Main {
    public static String configPath = "config.kashif.properties";

    public static void main(String[] args) throws Exception {
        String extractionType = args[0];
        String path = args[1];
        String databaseType = "jdbc:mysql"; // It can be changed and adjusted/configured if MS-SQL database is plugged in

        switch (extractionType) {
            case "JSON": //JSON data source
                new JsonSchemaExtractor(path);
                break;
            case "XML": //XML Data source
                new XmlSchemaExtractor(path);
                break;
            case "RDB":// Relational Database as a source
                new MySqlDB(
                        path.split(",")[0],
                        path.split(",")[1],
                        path.split(",")[2],
                        path.split(",")[3],
                        databaseType);
                //new MySqlDB("employees","jdbc:mysql", "localhost", "root",  "");
                break;
            case "TRY":
                //This is just to test some functionality independently - Pass the argument like this : TRY N
                break;
            default:
                System.out.println("Please provide the arguments in a correct way: " +
                        "\n To extract JSON Schema: \n " +
                        "\t 1st Argument: JSON \n\t 2nd Argument: Path to JSON file e.g. home/files/file.json" +
                        "\n To extract XML Schema: \n " +
                        "\t 1st Argument: XML \n\t 2nd Argument: Path to XML file e.g. home/files/file.xml" +
                        "\n To extract Relational Database Schema: \n " +
                        "\t 1st Argument: RDB \n\t 2nd Argument: Prepare a String with UserName, " +
                        " Password, Database Name and Server e.g. localhost in a following order: \n\t" +
                        " UserName,Password,databaseName,databaseServer");
        }

    }

}
