package com.spotifywebapp.app;

import com.mongodb.ServerAddress;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.MongoClientSettings;

import org.bson.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

public class MongoDBClient {

    MongoClient mongoClient;
    MongoDatabase songbirdDB;

    public MongoDBClient(String host, int port) {
        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(host, port))))
                        .build());
        songbirdDB = mongoClient.getDatabase(MongoDBConstants.DB_NAME);
    }

    public MongoDBClient(String uri) {
        mongoClient = MongoClients.create(uri);
        songbirdDB = mongoClient.getDatabase(MongoDBConstants.DB_NAME);
    }


    public Document getProfile(String id) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);
        Document doc = collection.find(eq("_id", id)).first();

        return doc;
    }

    public void createNewProfile(HashMap<String, String> userInfo) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        Document doc = new Document("_id", userInfo.get("id"))
                            .append("display_name", userInfo.get("display_name"))
                            .append("email", userInfo.get("email"))
                            .append("profile_pic_url", userInfo.get("profile_pic"));

        collection.insertOne(doc);
    }

    public void updateProfile(HashMap<String, String> userInfo, String oldID) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        collection.updateOne(eq("_id", oldID), set("_id", userInfo.get("id")));
        collection.updateOne(eq("_id", userInfo.get("id")), set("display_name", userInfo.get("display_name")));
        collection.updateOne(eq("_id", userInfo.get("id")), set("email", userInfo.get("email")));
        collection.updateOne(eq("_id", userInfo.get("id")), set("profile_pic_url", userInfo.get("profile_pic")));

    }

    public void deleteProfile(String id) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);
        collection.deleteOne(eq("_id", id));
    }





}
