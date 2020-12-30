package com.spotifywebapp.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import com.spotifywebapp.app.SpotifyWebAPI;
import com.spotifywebapp.app.SpotifyWebAPISingleton;

import java.net.http.HttpResponse;
import java.util.HashMap;

@Controller
public class DataController {

    public SpotifyWebAPI api = SpotifyWebAPISingleton.getInstance();

    @GetMapping("/datatransfer")
    public @ResponseBody String dataTransfer() {
        HttpClientHandler handler = new HttpClientHandler();
        handler.formRequest("http://localhost:8888/greeting");
        HttpResponse response = handler.sendRequest();
        System.out.println(response.toString());
        return response.toString();
    }

}
