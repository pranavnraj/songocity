package com.spotifywebapp.app;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import com.spotifywebapp.app.SpotifyWebAPI;
import com.spotifywebapp.app.SpotifyWebAPISingleton;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;

@Controller
public class DataController {

    public SpotifyWebAPI api = SpotifyWebAPISingleton.getInstance();
    private MongoDBClient mongoClient = MongoDBSingleton.getInstance();

    @GetMapping("/datatransfer")
    public @ResponseBody String dataTransfer() {
        HttpClientHandler handler = new HttpClientHandler();
        handler.formRequest("http://localhost:8888/greeting");
        HttpResponse response = handler.sendRequest();
        System.out.println(response.toString());
        return response.toString();
    }

    @GetMapping("/query_friend")
    public @ResponseBody ResponseEntity<JSONObject> queryFriend(@RequestParam(name="id_query") String id_query) {

        ArrayList<String> matchedPattern = mongoClient.findMatchingFriends(id_query);

        JSONObject obj = new JSONObject();
        obj.put("queries", matchedPattern.toArray());
        return ResponseEntity.status(HttpStatus.OK).body(obj);
    }

    @PostMapping("/add_friend")
    public @ResponseBody
    ResponseEntity.BodyBuilder addFriend(@RequestBody Friend friend) {

        String flag = mongoClient.addFriend(friend.getUser(), friend.getFriend());

        return ResponseEntity.status(HttpStatus.OK).header("status", flag);

    }

    @DeleteMapping ("/remove_friend")
    public @ResponseBody
    ResponseEntity.BodyBuilder removeFriend(@RequestBody Friend friend) {

        mongoClient.deleteFriend(friend.getUser(), friend.getFriend());

        return ResponseEntity.status(HttpStatus.OK);

    }

    /*
    @GetMapping("/get_friend_list")
    public @ResponseBody ResponseEntity<JSONObject> getFriendList() {
        mongoClient.getFriendList();
    }*/

}
