package com.songbirds.app;

import com.mongodb.ServerAddress;

import com.mongodb.client.*;
import com.mongodb.MongoClientSettings;

import com.mongodb.client.model.Updates;
import com.songbirds.util.MongoDBConstants;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoDBClient {

    MongoClient mongoClient;
    MongoDatabase songbirdDB;

    private static final MongoDBClient mongoDBClient = new MongoDBClient(MongoDBConstants.DB_URI);

    private MongoDBClient(String host, int port) {
        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(host, port))))
                        .build());
        songbirdDB = mongoClient.getDatabase(MongoDBConstants.DB_NAME);
    }

    private MongoDBClient(String uri) {
        mongoClient = MongoClients.create(uri);
        songbirdDB = mongoClient.getDatabase(MongoDBConstants.DB_NAME);
    }

    public static MongoDBClient getInstance() {
        return mongoDBClient;
    }

    public Document sessionCookieExists(String sessionID) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.SESSIONS_COLLECTION);
        Document doc = collection.find(eq("_id", sessionID)).first();

        return doc;
    }

    public Document getProfile(String id) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);
        Document doc = collection.find(eq("_id", id)).first();

        return doc;
    }

    public Document friendListExists(String id) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.FRIENDS_COLLECTION);
        Document doc = collection.find(eq("_id", id)).first();

        return doc;
    }

    public List<String> getFriendList(String id) {
        Document doc = friendListExists(id);

        if(doc == null) {
            return new ArrayList<String>();
        }

        List<String> friendList = doc.getList("friends", String.class);

        return friendList;
    }

    public void createNewProfile(HashMap<String, String> userInfo) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        Document doc = new Document("_id", userInfo.get("id"))
                            .append("display_name", userInfo.get("display_name"))
                            .append("email", userInfo.get("email"))
                            .append("profile_pic_url", userInfo.get("profile_pic"))
                            .append("is_trained", false);

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

    public List<String> findMatchingFriends(String id, String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        String pattern = ".*" + id + ".*";
        Pattern dbPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        FindIterable<Document> col = collection.find(regex("display_name", dbPattern));

        List<String> matchedStrings = new ArrayList<String>();

        for(Document doc: col) {
            if (!doc.getString("_id").equals(userId)) {
                matchedStrings.add(doc.getString("display_name"));
            }
        }

        return matchedStrings;
    }

    public String addFriend(String userId, String newFriendID) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.FRIENDS_COLLECTION);

        if (friendListExists(userId) == null) {

            List<String> friendList = new ArrayList<String>();
            friendList.add(newFriendID);

            Document newFriendEntry = new Document();
            newFriendEntry.append("_id", userId);
            newFriendEntry.append("friends", friendList);

            collection.insertOne(newFriendEntry);
            return "New Entry";
        }

        List<String> friendList = getFriendList(userId);
        if (!friendList.contains(newFriendID)) {
            collection.updateOne(eq("_id", userId), push("friends", newFriendID));
            return "Added Friend";
        }

        return "Friend already in list";
    }

    public void deleteFriend(String userId, String deletedFriendID) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.FRIENDS_COLLECTION);

        collection.updateOne(eq("_id", userId), pull("friends", deletedFriendID));
    }

    public List<String> friendsToIDs(List<String> friendNames) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        List<String> friendIDs = new ArrayList<String>();

        for (String name: friendNames) {
            Document friendDoc = collection.find(eq("display_name", name)).first();
            friendIDs.add(friendDoc.getString("_id"));
        }

        return friendIDs;

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
        if (!refreshToken.equals(MongoDBConstants.IGNORE_REFRESH)) {
            collection.updateOne(eq("_id", userId), set("refresh_token", refreshToken));
        }
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

    public Document playlistExists(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PLAYLISTS_COLLECTION);
        Document doc = collection.find(eq("_id", userId)).first();

        return doc;
    }

    public String addNewPlaylist(String userId, String playListId, String title) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PLAYLISTS_COLLECTION);

        if (playlistExists(userId) == null) {

            //List<String> playlistList = new ArrayList<String>();
            //playlistList.add(playListId);

            Document newPlaylistEntry = new Document();
            newPlaylistEntry.append("_id", userId);

            List<Document> playlistDocList = new ArrayList<Document>();

            Document idAndName = new Document();
            idAndName.append("title", title);
            idAndName.append("playlist_id", playListId);

            playlistDocList.add(idAndName);

            newPlaylistEntry.append("playlists", playlistDocList);

            collection.insertOne(newPlaylistEntry);
            return "New Entry";
        }

        Document idAndName = new Document();
        idAndName.append("title", title);
        idAndName.append("playlist_id", playListId);

        collection.updateOne(eq("_id", userId), push("playlists", idAndName));
        return "Added Friend";
    }

    public List<Document> getPlaylistList(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PLAYLISTS_COLLECTION);
        Document doc = collection.find(eq("_id", userId)).first();

        if (doc == null) {
            return new ArrayList<Document>();
        }

        List<Document> playlistList = doc.getList("playlists", Document.class);

        return playlistList;
    }

    public void deletePlaylist(String userId, String deletedPlaylistID) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PLAYLISTS_COLLECTION);

        Bson delete = Updates.pull("playlists", new Document("playlist_id", deletedPlaylistID));
        collection.updateOne(eq("_id", userId), delete);
    }

    public boolean isTrained(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        Document doc = collection.find(eq("_id", userId)).first();
        if (doc.containsKey("is_trained") && doc.getBoolean("is_trained")) {
            return true;
        }

        return false;
    }

    public void markTrainedFlag(String userId) {
        MongoCollection<Document> collection = songbirdDB.getCollection(MongoDBConstants.PROFILE_COLLECTION);

        collection.updateOne(eq("_id", userId), set("is_trained", true));
    }





}
