package com.spotifywebapp.app;

import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import com.spotifywebapp.app.SpotifyWebAPI;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.standard.Media;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/data")
public class DataController {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private static RestTemplate rest = new RestTemplate();
    private static HttpHeaders headers = new HttpHeaders();
    static {
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }
    private static final Logger LOGGER = Logger.getLogger(DataController.class.getName());

    @GetMapping("/datatransfer")
    public String dataTransfer() {
        HttpClientHandler handler = new HttpClientHandler();
        handler.formRequest("http://localhost:8888/greeting");
        HttpResponse response = handler.sendRequest();
        System.out.println(response.toString());
        return response.toString();
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> getProfileInfo(HttpServletRequest request, HttpSession session, @CookieValue(value = "SESSION",
            defaultValue = "session_cookie") String sessionCookie){

        request.changeSessionId();
        HashMap<String, String> userInfo = api.currentUserAPI(session.getAttribute("user_id").toString());
        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        obj.put("email", userInfo.get("email"));
        obj.put("profile_pic", userInfo.get("profile_pic"));
        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @RequestMapping(value = "/query_friend", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> queryFriend(@RequestParam(name="id_query") String id_query) {

        List<String> matchedPattern = mongoClient.findMatchingFriends(id_query);

        JSONObject obj = new JSONObject();
        obj.put("queries", matchedPattern.toArray());

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @RequestMapping(value = "/add_friend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> addFriend(@RequestBody Friend friend, HttpSession session) {

        String flag = mongoClient.addFriend(session.getAttribute("user_id").toString(), friend.getFriend());

        JSONObject obj = new JSONObject();
        obj.put("status", flag);

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());

    }

    @RequestMapping(value = "/remove_friend", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity removeFriend(@RequestParam(name="friend") String friend, HttpSession session) {

        mongoClient.deleteFriend(session.getAttribute("user_id").toString(), friend);

        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @RequestMapping(value = "/get_friend_list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> getFriendList(HttpSession session, @CookieValue(value = "SESSION",
            defaultValue = "session_cookie") String sessionCookie) {
        List<String> friendList = mongoClient.getFriendList(session.getAttribute("user_id").toString());

        JSONObject obj = new JSONObject();
        obj.put("friends", friendList.toArray());

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @RequestMapping(value = "/get_playlist_list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> getPlayListList(HttpSession session, @CookieValue(value = "SESSION",
            defaultValue = "session_cookie") String sessionCookie) {
        List<String> playlistList = mongoClient.getPlaylistList(session.getAttribute("user_id").toString());

        JSONObject obj = new JSONObject();
        obj.put("friends", playlistList.toArray());

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @RequestMapping(value = "/reccomender", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> getRecommendedPlaylist(@RequestBody Friends friends, HttpSession session, @CookieValue(value = "SESSION",
            defaultValue = "session_cookie") String sessionCookie) {

        LOGGER.log(Level.INFO, friends.getFriendIDs().toString());

        JSONObject friendIDJson = new JSONObject();
        friendIDJson.put("friends", friends.getFriendIDs());

        HttpEntity<String> requestEntity = new HttpEntity<String>(friendIDJson.toString(), headers);
        ResponseEntity<String> responseEntity = rest.exchange(AppConstants.FLASK_SERVER + "/recommend", HttpMethod.POST, requestEntity, String.class);

        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }

}
