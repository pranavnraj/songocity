package com.spotifywebapp.app;

import com.mongodb.ServerAddress;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.MongoClientSettings;

import org.bson.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

import javax.print.Doc;

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

    public Document getFriendList(String id) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.FRIENDS_COLLECTION);
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

    public ArrayList<String> findMatchingFriends(String id) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        String pattern = ".*" + id + ".*";
        FindIterable<Document> col = collection.find(regex("_id", pattern));

        ArrayList<String> matchedStrings = new ArrayList<String>();

        for(Document doc: col) {
            matchedStrings.add(doc.getString("_id"));
        }

        return matchedStrings;
    }

    public String addFriend(String userId, String newFriendID) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.FRIENDS_COLLECTION);

        if (getFriendList(userId) == null) {

            ArrayList<String> friendList = new ArrayList<String>();
            friendList.add(newFriendID);

            Document newFriendEntry = new Document();
            newFriendEntry.append("_id", userId);
            newFriendEntry.append("friends", friendList);

            collection.insertOne(newFriendEntry);
            return "New Entry";
        }

        collection.updateOne(eq("_id", userId), push("friends", newFriendID));
        return "Added Friend";
    }

    public void deleteFriend(String userId, String deletedFriendID) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.FRIENDS_COLLECTION);

        collection.updateOne(eq("_id", userId), pull("friends", deletedFriendID));
    }

    public void storeAccessAndRefreshTokens(String userId, String accessToken, String refreshToken, long expiresIn,
                                           long currentTime) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.TOKENS_COLLECTION);

        Document queriedDoc = collection.find(eq("_id", userId)).first();

        if (queriedDoc == null) {
            Document newUserTokenEntry = new Document();
            newUserTokenEntry.append("_id", userId);
            newUserTokenEntry.append("access_token", accessToken);
            newUserTokenEntry.append("refresh_token", refreshToken);
            newUserTokenEntry.append("expires_in", expiresIn);
            newUserTokenEntry.append("current_time", currentTime);

            collection.insertOne(newUserTokenEntry);
            return;
        }

        collection.updateOne(eq("_id", userId), set("access_token", accessToken));
        collection.updateOne(eq("_id", userId), set("refresh_token", refreshToken));
        collection.updateOne(eq("_id", userId), set("expires_in", expiresIn));
        collection.updateOne(eq("_id", userId), set("current_time", currentTime));
    }

    public String retrieveRefreshToken(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.TOKENS_COLLECTION);

        Document doc = collection.find(eq("_id", userId)).first();
        return doc.getString("refresh_token");
    }

    public String retrieveAccessToken(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.TOKENS_COLLECTION);

        Document doc = collection.find(eq("_id", userId)).first();
        return doc.getString("access_token");
    }

    public long retrieveTokenLifetime(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.TOKENS_COLLECTION);

        Document doc = collection.find(eq("_id", userId)).first();
        return doc.getLong("expires_in");
    }

    public long retrieveStoredTime(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.TOKENS_COLLECTION);

        Document doc = collection.find(eq("_id", userId)).first();
        return doc.getLong("current_time");
    }







}
