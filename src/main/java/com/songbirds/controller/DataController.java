package com.songbirds.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.songbirds.app.MongoDBClient;
import com.songbirds.app.S3AwsClient;
import com.songbirds.app.SpotifyWebAPI;
import com.songbirds.concurrency.SongbirdExecutorService;
import com.songbirds.concurrency.TrainRunnable;
import com.songbirds.objects.Friend;
import com.songbirds.objects.Friends;
import com.songbirds.util.AWSClientConstants;
import com.songbirds.util.AppConstants;
import com.songbirds.util.HttpClientHandler;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import org.apache.juli.logging.Log;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/data")
public class DataController {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private S3AwsClient s3AwsClient = S3AwsClient.getInstance();

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

        //request.changeSessionId();
        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }
        HashMap<String, String> userInfo = api.currentUserAPI(user_id);

        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        obj.put("email", userInfo.get("email"));
        obj.put("profile_pic", userInfo.get("profile_pic"));
        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @RequestMapping(value = "/query_friend", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> queryFriend(@RequestParam(name="id_query") String id_query, HttpSession session) {

        List<String> matchedPattern = mongoClient.findMatchingFriends(id_query);

        JSONObject obj = new JSONObject();
        obj.put("queries", matchedPattern.toArray());

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @RequestMapping(value = "/add_friend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> addFriend(@RequestBody Friend friend, HttpSession session) {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }

        String flag = mongoClient.addFriend(user_id, friend.getFriend());

        JSONObject obj = new JSONObject();
        obj.put("status", flag);

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());

    }

    @RequestMapping(value = "/remove_friend", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity removeFriend(@RequestParam(name="friend") String friend, HttpSession session) {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }

        mongoClient.deleteFriend(user_id, friend);

        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @RequestMapping(value = "/get_friend_list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> getFriendList(HttpSession session, @CookieValue(value = "SESSION",
            defaultValue = "session_cookie") String sessionCookie) {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }

        List<String> friendList = mongoClient.getFriendList(user_id);

        JSONObject obj = new JSONObject();
        obj.put("friends", friendList.toArray());

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @RequestMapping(value = "/get_playlist_list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<String> getPlayListList(HttpSession session) {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }

        List<Document> playlistList = mongoClient.getPlaylistList(user_id);

        JSONObject obj = new JSONObject();

        for (Document playlistInfo: playlistList) {
            HashMap<String, String> tracks;
            try {
                tracks = api.getTracks(playlistInfo.getString("playlist_id"));
            } catch(ServiceUnavailableException e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Spotify Web API unavailable");
            } catch(SpotifyWebApiException e) {
                System.out.println("Error");
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown Internal Error");
            }

            String playListTitle = playlistInfo.getString("title");

            if (!tracks.values().isEmpty()) {
                JSONObject subObj = new JSONObject();
                subObj.put("title", playListTitle);
                subObj.put("tracks", tracks.values());
                obj.put(playlistInfo.getString("playlist_id"), subObj);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    
    @RequestMapping(value = "/remove_playlist", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity removePlaylist(@RequestParam(name="playlistID") String playlistID, HttpSession session) {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }

        mongoClient.deletePlaylist(user_id, playlistID);

        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @RequestMapping(value = "/recommend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity generateRecommendedPlaylist(@RequestBody Friends friends, HttpSession session) {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }

        LOGGER.log(Level.INFO, friends.getFriendIDs().toString());

        JSONObject friendIDJson = new JSONObject();
        friendIDJson.put("user_id", user_id);
        friendIDJson.put("friends", friends.getFriendIDs());

        HttpEntity<String> requestEntity = new HttpEntity<String>(friendIDJson.toString(), headers);
        ResponseEntity<Friends> responseEntity = null;
        try {
            responseEntity = rest.exchange(AppConstants.FLASK_SERVER + "/recommend", HttpMethod.POST, requestEntity, Friends.class);
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Machine Learning Server Unavailable");
        }

        Friends friendRecs = responseEntity.getBody();

        List<String> track_URIs = friendRecs.getFriendIDs();
        String title = friends.getFriendIDs().toString() + "Playlist";
        String playlistID = api.createPlaylist(user_id, title);

        track_URIs = api.getFilteredTracks(track_URIs);

        String[] trackURIs = new String[track_URIs.size()];
        for(int i = 0; i < track_URIs.size(); i += 1) {
            trackURIs[i] = "spotify:track:" + track_URIs.get(i);
        }

        api.addTracksToPlaylist(playlistID, trackURIs);
        mongoClient.addNewPlaylist(user_id, playlistID, title);

        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }



    @RequestMapping(value = "/train", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public ResponseEntity generateTrainingDataAndTrainModel(HttpSession session) throws FileNotFoundException {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cannot find user for session ID");
        }

        LOGGER.log(Level.INFO, "Generating training data");

        HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo;
        // TODO Retry after wait period time
        try {
            playlistsInfo = api.generateUserData(user_id);
        } catch(ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Spotify Web API unavailable");
        } catch(SpotifyWebApiException e) {
            System.out.println("Error");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown Internal Error");
        }
        int numTracks = api.getNumTracks(playlistsInfo);

        Gson gson = new GsonBuilder().create();
        JsonObject user = gson.toJsonTree(playlistsInfo).getAsJsonObject();
        String fileName = "/tmp/" + user_id + ".txt";
        PrintWriter out = new PrintWriter(fileName);
        out.println(user);
        out.flush();

        s3AwsClient.putFileInS3(AWSClientConstants.TRAINING_DATA_KEY_PREFIX, fileName);

        File file = new File(fileName);
        file.delete();

        HashMap<String, String> genres;
        try {
            genres = api.getRecommendations(numTracks / AppConstants.NUM_GENRES);
        } catch(ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Spotify Web API unavailable");
        } catch(SpotifyWebApiException e) {
            System.out.println("Error");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown Internal Error");
        }

        HashMap<String,HashMap<String,Float>> genreInfo;
        try {
            genreInfo = api.getTracksInfo(genres);
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Spotify Web API unavailable");
        } catch(SpotifyWebApiException e) {
            System.out.println("Error");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown Internal Error");
        }

        Gson rec_gson = new GsonBuilder().create();
        JsonObject genreJson = rec_gson.toJsonTree(genreInfo).getAsJsonObject();
        fileName = "/tmp/" + user_id + "genres.txt";
        PrintWriter outRec = new PrintWriter(fileName);
        outRec.println(genreJson);
        outRec.flush();

        s3AwsClient.putFileInS3(AWSClientConstants.TRAINING_DATA_GENRE_KEY_PREFIX, fileName);

        file = new File(fileName);
        file.delete();

        LOGGER.log(Level.INFO, "Finished collecting data and writing to file");

        JSONObject iDJson = new JSONObject();
        iDJson.put("user_id", user_id);

        HttpEntity<String> requestEntity = new HttpEntity<String>(iDJson.toString(), headers);

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = rest.exchange(AppConstants.FLASK_SERVER + "/train", HttpMethod.POST, requestEntity, String.class);
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Machine Learning Server Unavailable");
        }

        return ResponseEntity.status(responseEntity.getStatusCode()).build();

    }

    @RequestMapping(value = "/train_threaded", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public ResponseEntity generateTrainingDataAndTrainModelThreaded(HttpSession session) throws FileNotFoundException {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ExecutorService service = SongbirdExecutorService.getExecutorService();
        TrainRunnable trainRunnable = new TrainRunnable(user_id, headers);

        Future<HttpStatus> status = service.submit(trainRunnable);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
