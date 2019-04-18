package com.genesis.eso.util;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.mongodb.MongoClient;
import net.minidev.json.JSONObject;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.visualdataweb.vowl.owl2vowl.Owl2Vowl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * Created by snadal on 17/05/16.
 */
public class Utils {

    public static MongoClient getMongoDBClient() {
        return new MongoClient(ConfigManager.getProperty("system_bdi_db_server"));
    }

    public static Dataset copyOfTheDataset = null;

    public static Dataset getTDBDataset() {
        if (copyOfTheDataset == null) {
            try {
                return TDBFactory.createDataset(ConfigManager.getProperty("bdi_db_file")/*"BolsterMetadataStorage"*/ +
                        ConfigManager.getProperty("bdi_db_name")/*"BolsterMetadataStorage"*/);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("An error has occurred obtaining TDB dataset");

            }
        }
        return copyOfTheDataset;
//        return null;
    }

    public static SQLiteConnection getSQLiteConnection() {
        SQLiteConnection conn = new SQLiteConnection(new File(ConfigManager.getProperty("sqlite_db")));
        try {
            conn.open(true);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static InputStream getResourceAsStream(String filename) {
        InputStream in = Utils.class.getClassLoader().getResourceAsStream(filename);
        return in;
    }

    public static JSONObject oWl2vowl(String rdfsFilePath) {
        JSONObject vowlData = new JSONObject();
        String jsonFilePath = "";
        try {
            File temp = new File(rdfsFilePath);
            String vowlFileName = temp.getName().replaceAll(".ttl", "-vowl.json");
            //InputStream in = new FileInputStream(rdfsFilePath);
            Owl2Vowl owl2Vowl = new Owl2Vowl(new FileInputStream(rdfsFilePath));
            //System.out.println(owl2Vowl.getJsonAsString());
            File jsonVowlFile = new File(ConfigManager.getProperty("vowl_output_path") + vowlFileName);
            owl2Vowl.writeToFile(jsonVowlFile);
            jsonFilePath = jsonVowlFile.getAbsolutePath();

            vowlData.put("vowlJsonFileName", vowlFileName.replaceAll(".json", ""));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        vowlData.put("vowlJsonFilePath", jsonFilePath);
        //System.out.println(jsonFilePath);
        return vowlData;
    }
}
