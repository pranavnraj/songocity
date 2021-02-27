package com.songbirds.controller;

import com.songbirds.app.MongoDBClient;
import com.songbirds.app.SpotifyWebAPI;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import org.json.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

@RestController
public class AuthController {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private Object syncObject = new Object();
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    private String currentID = "";

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity login(HttpSession session) {
        api.initializeAPI();

        synchronized (syncObject) {
            try {
                // Calling wait() will block this thread until another thread
                // calls notify() on the object.
                syncObject.wait();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        
        HashMap<String, String> userInfo = api.currentUserAPI(currentID);
        session.setAttribute("user_id", userInfo.get("id"));
        LOGGER.log(Level.INFO, "User ID: " + session.getAttribute("user_id"));

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity logout(HttpSession session) {

        LOGGER.log(Level.INFO, "User ID: " + session.getAttribute("user_id"));
        session.invalidate();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/callback", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000")
    public ResponseEntity<String> callback(@RequestParam(name="code") String code) {
        LOGGER.setLevel(Level.INFO);

        System.out.println(code);

        String id = api.storeTokensUponLogin(code);
        currentID = id;

        synchronized (syncObject) {
            syncObject.notify();
        }

        HashMap<String, String> userInfo = api.currentUserAPI(id);

        LOGGER.setLevel(Level.INFO);
        System.out.println("userInfo.get(id): " + userInfo.get("id"));
        if (mongoClient.getProfile(userInfo.get("id")) == null) {
            mongoClient.createNewProfile(userInfo);
        }

        // Profile
        System.out.println("Display Name: " + userInfo.get("display_name"));
        System.out.println();

        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        obj.put("email", userInfo.get("email"));

        /*
        ArrayList<String> userRecentTracks = api.currentUserRecentTracks();
        System.out.println("Recently Listened to: " + userRecentTracks);
        System.out.println();
        ArrayList<String> userTopArtists = api.currentUserTopArtists();
        System.out.println("Your Top Artists... " + userTopArtists);
        System.out.println();
        ArrayList<String> userTopTracks = api.currentUserTopTracks();
        System.out.println("Your Top Tracks... " + userTopTracks);
        System.out.println();


        // Creating Hashmap to be passed to python
        int numTracks = 0;
        System.out.println("Analyzing Playlists... ");
        HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo = new HashMap<String, HashMap<String, HashMap<String, Float>>>();
        HashMap<String, String> playlistInfo = api.currentUserPlaylists();
        for(String playlistId: playlistInfo.keySet() ){
            String playlistName = playlistInfo.get(playlistId);
            playlistsInfo.put(playlistId, new HashMap<String, HashMap<String, Float>>());

            System.out.println(playlistName + ": " + playlistId);
            HashMap<String,String> playlistTracks = api.getTracks(playlistId);
            numTracks += playlistTracks.keySet().size();
            HashMap<String,HashMap<String,Float>> playlistTrackInfo = api.getTracksInfo(playlistTracks);
            for(String trackId: playlistTrackInfo.keySet()){
                String trackName = playlistTracks.get(trackId);
                playlistsInfo.get(playlistId).put(trackId, new HashMap<String, Float>());
                for(String feature : playlistTrackInfo.get(trackId).keySet()){
                    playlistsInfo.get(playlistId).get(trackId).put(feature, playlistTrackInfo.get(trackId).get(feature));
//                    System.out.println(feature + ": " + playlistTrackInfo.get(trackId).get(feature));
                }
            }

        }

        System.out.println("The Hashmap to be passed to Python");

        for(String playlistName : playlistsInfo.keySet()) {
//            System.out.println("playlist: " + playlistName);
            for (String trackName : playlistsInfo.get(playlistName).keySet()) {
//                System.out.println("track: " + trackName);
//                for(String feature: playlistsInfo.get(playlistName).get(trackName).keySet()){
//                    System.out.println(feature + ": " + playlistsInfo.get(playlistName).get(trackName).get(feature));
//                }
            }
        }

        // HttpClientHandler handler = new HttpClientHandler();
        // handler.formRequest("http://localhost:8888/greeting");
        // HttpResponse response = handler.sendRequest();
        // System.out.println(response.toString());
        // JSONObject obj = new JSONObject();
        // obj.put("id", userInfo.get("id"));
        // obj.put("display_name", userInfo.get("display_name"));
        // return obj.toString();
        */

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

}

