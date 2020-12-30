package com.spotifywebapp.app;

public class MongoDBSingleton {
    private static final MongoDBClient mongoClient = new MongoDBClient(MongoDBConstants.DB_URI);

    private MongoDBSingleton(){}

    public static MongoDBClient getInstance() {
        return mongoClient;
    }
}
