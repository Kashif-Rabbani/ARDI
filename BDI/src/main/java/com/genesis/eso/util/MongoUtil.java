package com.genesis.eso.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

public class MongoUtil {

    public static MongoCollection<Document> getGlobalGraphCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_bdi_db_name")).getCollection("globalGraphs");
    }

    public static MongoCollection<Document> getIntegratedDataSourcesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_bdi_db_name")).getCollection("integratedDataSources");
    }

    public static MongoCollection<Document> getDataSourcesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_bdi_db_name")).getCollection("dataSources");
    }

    public static MongoCollection<Document> getLAVMappingCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_bdi_db_name")).getCollection("LAVMappings");
    }

    public static String getMongoObject(MongoClient client, MongoCursor<Document> cursor) {
        boolean itIs = true;
        String out = "";
        if (!cursor.hasNext()) itIs = false;
        else out = cursor.next().toJson();
        client.close();

        if (itIs) {
            System.out.println(out);
        } else {
            System.out.println("Not Found");
        }
        return out;
    }

}
