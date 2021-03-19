package com.songbirds.concurrency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.songbirds.app.SpotifyWebAPI;
import com.songbirds.objects.Friends;
import com.songbirds.util.AppConstants;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public class TrainRunnable implements Callable {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private RestTemplate rest = new RestTemplate();

    private String userID;
    private HttpHeaders headers;

    public TrainRunnable(String userID, HttpHeaders headers) {
        this.userID = userID;
        this.headers = headers;
    }

    @Override
    public HttpStatus call() {
        //LOGGER.log(Level.INFO, "Generating training data");

        HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo = null;
        try {
            playlistsInfo = api.generateUserData(userID);
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        }
        int numTracks = api.getNumTracks(playlistsInfo);

        Gson gson = new GsonBuilder().create();
        JsonObject user = gson.toJsonTree(playlistsInfo).getAsJsonObject();
        PrintWriter out = null;
        try {
            out = new PrintWriter("src/main/jupyter_notebooks/" +
                    userID + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.println(user);
        out.flush();

        HashMap<String, String> genres = null;
        try {
            genres = api.getRecommendations(numTracks / 126);
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        }

        HashMap<String, HashMap<String, Float>> genreInfo = null;
        try {
            genreInfo = api.getTracksInfo(genres);
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        }

        Gson rec_gson = new GsonBuilder().create();
        JsonObject genreJson = rec_gson.toJsonTree(genreInfo).getAsJsonObject();
        PrintWriter outRec = null;
        try {
            outRec = new PrintWriter("src/main/jupyter_notebooks/" +
                    userID + "genres.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        outRec.println(genreJson);
        outRec.flush();

        JSONObject iDJson = new JSONObject();
        iDJson.put("user_id", userID);

        HttpEntity<String> requestEntity = new HttpEntity<String>(iDJson.toString(), headers);
        ResponseEntity<String> responseEntity = rest.exchange(AppConstants.FLASK_SERVER + "/train", HttpMethod.POST, requestEntity, String.class);

        return responseEntity.getStatusCode();
    }
}
