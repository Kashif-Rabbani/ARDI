package com.genesis.eso.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoCollections {

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



}
