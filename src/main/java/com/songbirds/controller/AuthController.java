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

    @RequestMapping(value = "/prime_login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity primeLogin(@RequestParam(name="state") String state) {
        LoginThreadLock.addToLoginLocks(new LoginThreadLock(state));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity login(@RequestParam(name="state") String state) {

        LoginThreadLock currentLoginThreadLock = LoginThreadLock.getLoginLock(state);

        synchronized (currentLoginThreadLock) {
            try {
                while(!currentLoginThreadLock.isRdyFlag()) {
                    currentLoginThreadLock.wait();
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        LoginThreadLock.removeFromLoginLocks(currentLoginThreadLock);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    public ResponseEntity logout(HttpSession session) {

        String user_id;
        try {
            user_id = session.getAttribute("user_id").toString();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LOGGER.log(Level.INFO, "User ID: " + user_id);
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

        LoginThreadLock currentLoginThreadLock = LoginThreadLock.getLoginLock(state);

        synchronized (currentLoginThreadLock) {
            session.setAttribute("user_id", id);
            if (mongoClient.getProfile(userInfo.get("id")) == null) {
                mongoClient.createNewProfile(userInfo);
            }

            currentLoginThreadLock.setRdyFlag();
            currentLoginThreadLock.notifyAll();
        }

        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        obj.put("email", userInfo.get("email"));

        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

}

