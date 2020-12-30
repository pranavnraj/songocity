package com.spotifywebapp.app;

public class MongoDBSingleton {
    private static final MongoDBClient mongoClient = new MongoDBClient(MongoDBConstants.DEFAULT_HOST, MongoDBConstants.DEFAULT_PORT);

    private MongoDBSingleton(){}

    public static MongoDBClient getInstance() {
        return mongoClient;
    }
}
