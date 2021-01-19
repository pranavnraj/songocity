package com.spotifywebapp.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import com.spotifywebapp.app.SpotifyWebAPI;
import java.lang.reflect.Array;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.WebUtils;

import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private Object syncObject = new Object();
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    private String currentID = "";

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        System.out.println("Greeting done");
        return "greeting";
    }

    @GetMapping("/login")
    @CrossOrigin(origins="http://localhost:3000")
    public @ResponseBody ResponseEntity.BodyBuilder login(HttpServletResponse response) {
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
        
        HashMap<String, String> userInfo = api.currentUserAPI(currentID);
        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));

        Cookie idCookie = new Cookie("user_id", userInfo.get("id"));
        response.addCookie(idCookie);

        return ResponseEntity.status(HttpStatus.OK);
    }

    @GetMapping("/logout")
    public @ResponseBody ResponseEntity.BodyBuilder logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie idCookie = WebUtils.getCookie(request, "user_id");
        idCookie.setMaxAge(0);
        response.addCookie(idCookie);

        return ResponseEntity.status(HttpStatus.OK);
    }

    @GetMapping("/callback")
    @CrossOrigin(origins="http://localhost:3000")
    public @ResponseBody ResponseEntity<String> callback(@RequestParam(name="code") String code) {
        LOGGER.setLevel(Level.INFO);

        System.out.println(code);

        String id = api.storeTokensUponLogin(code);
        currentID = id;

        synchronized (syncObject) {
            syncObject.notify();
        }

        HashMap<String, String> userInfo = api.currentUserAPI(id);

        if (mongoClient.getProfile(userInfo.get("id")).isEmpty()) {
            mongoClient.createNewProfile(userInfo);
        }

        // Profile
        System.out.println("Display Name: " + userInfo.get("display_name"));
        System.out.println();
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

        return new ResponseEntity<>("Custom header set", HttpStatus.OK);
    }

}

