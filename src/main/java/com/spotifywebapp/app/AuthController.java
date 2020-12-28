package com.spotifywebapp.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import com.spotifywebapp.app.SpotifyWebAPI;

import java.util.HashMap;

@Controller
public class AuthController {

    public SpotifyWebAPI api;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping("/login")
    public @ResponseBody String login(Model model) {
        model.addAttribute("name", "World");
        api = new SpotifyWebAPI();
        api.authorizeAPI();
        return "Close Tab";
    }

    @GetMapping("/callback")
    public String callback(@RequestParam(name="code") String code) {
        api.setAuthCode(code);
        api.refreshTokenAPI();
        api.accessTokenAPI();
        HashMap<String, String> userInfo = api.currentUserAPI();

        System.out.println(userInfo.get("display_name"));
        return "homepage";
    }

}

