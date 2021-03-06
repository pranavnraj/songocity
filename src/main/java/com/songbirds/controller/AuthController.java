package com.songbirds.controller;

import com.songbirds.app.MongoDBClient;
import com.songbirds.app.SpotifyWebAPI;
import com.songbirds.objects.LoginThreadLock;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import org.json.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

@RestController
public class AuthController {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins={"https://localhost:3000"}, allowCredentials = "true")
    public ResponseEntity login(@RequestParam(name="state") String state) {

        LoginThreadLock currentLoginThreadLock = LoginThreadLock.getLoginLock(state);
        if (currentLoginThreadLock == null) {
            currentLoginThreadLock = new LoginThreadLock(state);
            LoginThreadLock.addToLoginLocks(currentLoginThreadLock);
        }

        synchronized (currentLoginThreadLock) {
            try {
                currentLoginThreadLock.wait();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        LoginThreadLock.removeFromLoginLocks(currentLoginThreadLock);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="https://localhost:3000", allowCredentials = "true")
    public ResponseEntity logout(HttpSession session) {

        LOGGER.log(Level.INFO, "User ID: " + session.getAttribute("user_id"));
        session.invalidate();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/callback", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000")
    public ResponseEntity<String> callback(@RequestParam(name="code") String code,
                                           @RequestParam(name="state") String state, HttpSession session) {
        LOGGER.setLevel(Level.INFO);

        api.initializeAPI();
        String id = api.storeTokensUponLogin(code);
        HashMap<String, String> userInfo = api.currentUserAPI(id);

        LoginThreadLock currentLoginThreadLock;
        while(true) {
            //System.out.println("In forever loop: " + state + "-----" + LoginThreadLock.loginThreadLocks.get(0).getStateID() + "-----" + LoginThreadLock.loginThreadLocks.get(1).getStateID());
            currentLoginThreadLock = LoginThreadLock.getLoginLock(state);
            if(currentLoginThreadLock != null) {
                break;
            }
        }

        synchronized (currentLoginThreadLock) {
            session.setAttribute("user_id", id);
            if (mongoClient.getProfile(userInfo.get("id")) == null) {
                mongoClient.createNewProfile(userInfo);
            }
            currentLoginThreadLock.notify();
        }

        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        obj.put("email", userInfo.get("email"));

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

}

