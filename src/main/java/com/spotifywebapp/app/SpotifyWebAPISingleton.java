package com.spotifywebapp.app;

import com.spotifywebapp.app.SpotifyWebAPI;

public class SpotifyWebAPISingleton {

    private static final SpotifyWebAPI api = new SpotifyWebAPI();

    private SpotifyWebAPISingleton(){}

    public static SpotifyWebAPI getInstance() {
        return api;
    }

}
