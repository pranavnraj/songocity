package com.songbirds.concurrency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.songbirds.app.SpotifyWebAPI;
import com.songbirds.objects.Friends;
import com.songbirds.util.AppConstants;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;

public class TrainRunnable implements Runnable{

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private RestTemplate rest = new RestTemplate();

    private String userID;
    private HttpHeaders headers;

    public TrainRunnable(String userID, HttpHeaders headers) {
        this.userID = userID;
        this.headers = headers;
    }

    @Override
    public void run() {
        //LOGGER.log(Level.INFO, "Generating training data");

        HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo =
                api.generateUserData(userID);
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

        HashMap<String,String> genres = api.getRecommendations(numTracks/126);
        HashMap<String,HashMap<String,Float>> genreInfo = api.getTracksInfo(genres);

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

        //return ResponseEntity.status(responseEntity.getStatusCode()).build();
    }
}
