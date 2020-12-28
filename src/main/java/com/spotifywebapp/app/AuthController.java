package com.spotifywebapp.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spotifywebapp.app.SpotifyWebAPI;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public void login() {
        api = new SpotifyWebAPI();
        api.authorizeAPI();
    }

    @GetMapping("/callback")
    public String callback(@RequestParam(name="code") String code) {
        api.setAuthCode(code);
        api.refreshTokenAPI();
        api.accessTokenAPI();
        HashMap<String, String> userInfo = api.currentUserAPI();

        userInfo.get("display_name");
        return "greeting";
    }

}

