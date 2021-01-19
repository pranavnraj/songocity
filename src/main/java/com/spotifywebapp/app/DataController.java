package com.spotifywebapp.app;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import com.spotifywebapp.app.SpotifyWebAPI;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

@Controller
public class DataController {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private static final Logger LOGGER = Logger.getLogger(DataController.class.getName());

    @GetMapping("/datatransfer")
    public @ResponseBody String dataTransfer() {
        HttpClientHandler handler = new HttpClientHandler();
        handler.formRequest("http://localhost:8888/greeting");
        HttpResponse response = handler.sendRequest();
        System.out.println(response.toString());
        return response.toString();
    }

    @GetMapping("/profile")
    @CrossOrigin(origins="http://localhost:3000")
    public @ResponseBody ResponseEntity<JSONObject> getProfileInfo(@CookieValue(value = "user_id",
            defaultValue = "user_id") String userId){
        HashMap<String, String> userInfo = api.currentUserAPI(userId);
        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        obj.put("email", userInfo.get("email"));
        obj.put("profile_pic", userInfo.get("profile_pic"));
        return ResponseEntity.status(HttpStatus.OK).body(obj);
    }

    @GetMapping("/query_friend")
    public @ResponseBody ResponseEntity<JSONObject> queryFriend(@RequestParam(name="id_query") String id_query) {

        List<String> matchedPattern = mongoClient.findMatchingFriends(id_query);

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

    @GetMapping("/get_friend_list")
    public @ResponseBody ResponseEntity<JSONObject> getFriendList(@CookieValue(value = "user_id",
            defaultValue = "user_id") String userId) {
        List<String> friendList = mongoClient.getFriendList(userId);

        JSONObject obj = new JSONObject();
        obj.put("friends", friendList.toArray());

        return ResponseEntity.status(HttpStatus.OK).body(obj);
    }

    @GetMapping("/get_playlist_list")
    public @ResponseBody ResponseEntity<JSONObject> getPlayListList(@CookieValue(value = "user_id",
            defaultValue = "user_id") String userId) {
        List<String> playlistList = mongoClient.getPlaylistList(userId);

        JSONObject obj = new JSONObject();
        obj.put("friends", playlistList.toArray());

        return ResponseEntity.status(HttpStatus.OK).body(obj);
    }

}
