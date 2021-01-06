package com.spotifywebapp.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import com.spotifywebapp.app.SpotifyWebAPI;
import com.spotifywebapp.app.SpotifyWebAPISingleton;
import java.lang.reflect.Array;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.TimeUnit;

@Controller
public class AuthController {

    private SpotifyWebAPI api = SpotifyWebAPISingleton.getInstance();
    private MongoDBClient mongoClient = MongoDBSingleton.getInstance();
    private Object syncObject = new Object();

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        System.out.println("Greeting done");
        return "greeting";
    }

    @GetMapping("/login")
    @CrossOrigin(origins="http://localhost:3000")
    public @ResponseBody ResponseEntity<String> login(Model model) {
        model.addAttribute("name", "World");
        api.authorizeAPI();

        synchronized (syncObject) {
            try {
                // Calling wait() will block this thread until another thread
                // calls notify() on the object.
                syncObject.wait();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        
        HashMap<String, String> userInfo = api.currentUserAPI();
        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
        // return new ResponseEntity<>("Custom header set", HttpStatus.OK);
    }
    @GetMapping("/profile")
    @CrossOrigin(origins="http://localhost:3000")
    public @ResponseBody ResponseEntity<String> profile(){
        HashMap<String, String> userInfo = api.currentUserAPI();
        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());

    }
    @GetMapping("/callback")
    @CrossOrigin(origins="http://localhost:3000")
    public @ResponseBody ResponseEntity<String> callback(@RequestParam(name="code") String code) {
        System.out.println("Here");

        System.out.println(code);

        api.setAuthCode(code);
        api.refreshTokenAPI();
        api.accessTokenAPI();

        synchronized (syncObject) {
            syncObject.notify();
        }

        HashMap<String, String> userInfo = api.currentUserAPI();

        //mongoClient.createNewProfile(userInfo);


        // Profile
        System.out.println("Display Name: " + userInfo.get("display_name"));
        System.out.println();
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

        return new ResponseEntity<>("Custom header set", HttpStatus.OK);

    }

}
