package com.songbirds.controller;

import com.songbirds.app.MongoDBClient;
import com.songbirds.app.SpotifyWebAPI;
import com.songbirds.objects.LoginThreadLock;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadRequestException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import org.json.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    private SpotifyWebAPI api = SpotifyWebAPI.getInstance();
    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @RequestMapping(value = "/{[path:[^\\.]*}")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
    }

    @RequestMapping(value = "/prime_login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    @ResponseBody
    public ResponseEntity primeLogin(@RequestParam(name="state") String state) {
        LoginThreadLock.addToLoginLocks(new LoginThreadLock(state));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    @ResponseBody
    public ResponseEntity<String> ping(HttpSession session) {
        LOGGER.log(Level.INFO, "PING: User " + session.getAttribute("user_id").toString());
        return ResponseEntity.status(HttpStatus.OK).body(session.getAttribute("user_id").toString());
    }

    @RequestMapping(value = "/authenticated", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    @ResponseBody
    public ResponseEntity<String> isAuthenticated(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        boolean authenticatedFlag = false;

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {

                if (cookie.getName().equalsIgnoreCase("SESSION")) {
                    byte[] decodedBytes = Base64.getDecoder().decode(cookie.getValue());
                    String sessionID = new String(decodedBytes);
                    if( mongoClient.sessionCookieExists(sessionID) != null){
                        authenticatedFlag = true;
                    }
                }
            }
        }

        JSONObject obj = new JSONObject();
        if (authenticatedFlag) {
            obj.put("authenticated", "true");
            return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
        } else {
            obj.put("authenticated", "false");
            return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
        }

    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins="http://localhost:3000", allowCredentials = "true")
    @ResponseBody
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
    @ResponseBody
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
    public String callback(@RequestParam(name="code") String code,
                                           @RequestParam(name="state") String state, HttpSession session) {
        LOGGER.setLevel(Level.INFO);

        //api.initializeAPI();
        String id;
        try {
            id = api.storeTokensUponLogin(code);
        } catch (BadRequestException e) {
            return "Invalid authorization";
        } catch (SpotifyWebApiException e) {
            LOGGER.log(Level.SEVERE, "Error: " + e.getMessage());
            e.printStackTrace();
            return "Unknown Internal Error";
        }
        HashMap<String, String> userInfo = api.currentUserAPI(id);

        LoginThreadLock currentLoginThreadLock = LoginThreadLock.getLoginLock(state);

        synchronized (currentLoginThreadLock) {
            if (mongoClient.getProfile(userInfo.get("id")) == null) {
                mongoClient.createNewProfile(userInfo);
            }
            session.setAttribute("user_id", id);
            session.setAttribute("name", userInfo.get("display_name"));

            currentLoginThreadLock.setRdyFlag();
            currentLoginThreadLock.notifyAll();
        }

        JSONObject obj = new JSONObject();
        obj.put("id", userInfo.get("id"));
        obj.put("display_name", userInfo.get("display_name"));
        obj.put("email", userInfo.get("email"));

        return "success";
    }

}

