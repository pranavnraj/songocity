package com.spotifywebapp.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import com.spotifywebapp.app.SpotifyWebAPI;
import com.spotifywebapp.app.SpotifyWebAPISingleton;

import java.lang.reflect.Array;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class AuthController {

    private SpotifyWebAPI api = SpotifyWebAPISingleton.getInstance();

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        System.out.println("Greeting done");
        return "greeting";
    }

    @GetMapping("/login")
    public @ResponseBody String login(Model model) {
        model.addAttribute("name", "World");
        api.authorizeAPI();
        return "Close Tab";
    }

    @GetMapping("/callback")
    public @ResponseBody String callback(@RequestParam(name="code") String code) {
        api.setAuthCode(code);
        api.refreshTokenAPI();
        api.accessTokenAPI();

        HashMap<String, String> userInfo = api.currentUserAPI();

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
        System.out.println("Analyzing Playlists... ");
        HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo = new HashMap<String, HashMap<String, HashMap<String, Float>>>();
        HashMap<String, String> playlistInfo = api.currentUserPlaylists();
        for(String playlistId: playlistInfo.keySet() ){
            String playlistName = playlistInfo.get(playlistId);
            playlistsInfo.put(playlistName, new HashMap<String, HashMap<String, Float>>());

            System.out.println(playlistName + ": " + playlistId);
            HashMap<String,String> playlistTracks = api.getTracks(playlistId);
            HashMap<String,HashMap<String,Float>> playlistTrackInfo = api.getTracksInfo(playlistTracks);
            for(String trackId: playlistTrackInfo.keySet()){
                String trackName = playlistTracks.get(trackId);
//                System.out.println("Analyzing " +  trackName + " ...");
                playlistsInfo.get(playlistName).put(trackName, new HashMap<String, Float>());
                for(String feature : playlistTrackInfo.get(trackId).keySet()){
                    playlistsInfo.get(playlistName).get(trackName).put(feature, playlistTrackInfo.get(trackId).get(feature));
//                    System.out.println(feature + ": " + playlistTrackInfo.get(trackId).get(feature));
                }
            }

        }

        System.out.println("The Hashmap to be passed to Python");

        for(String playlistName : playlistsInfo.keySet()){
            System.out.println("playlist: " + playlistName);
            for(String trackName : playlistsInfo.get(playlistName).keySet()){
                System.out.println("track: " + trackName);
                for(String feature: playlistsInfo.get(playlistName).get(trackName).keySet()){
                    System.out.println(feature + ": " + playlistsInfo.get(playlistName).get(trackName).get(feature));
                }
            }
        }

        HttpClientHandler handler = new HttpClientHandler();
        handler.formRequest("http://localhost:8888/greeting");
        HttpResponse response = handler.sendRequest();
        System.out.println(response.toString());
        return response.toString();
    }

}

