package com.spotifywebapp.app;

import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import com.wrapper.spotify.model_objects.specification.*;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.hc.core5.http.ParseException;


import java.util.HashMap;
import java.io.IOException;
import java.net.URI;

import com.spotifywebapp.app.LoginCredentialConstants;


public class SpotifyWebAPI {

    private SpotifyApi spotifyApi;
    private final URI redirectURI;
    private String authCode;
    private String refreshToken;
    private String accessToken;

    public SpotifyWebAPI() {
        redirectURI = SpotifyHttpManager.makeUri(LoginCredentialConstants.REDIRECT_URI);
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(LoginCredentialConstants.CLIENT_ID)
                .setClientSecret(LoginCredentialConstants.CLIENT_SECRET)
                .setRedirectUri(redirectURI)
                .build();
    }

    public void authorizeAPI() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri().build();
        URI authorizeURI = authorizationCodeUriRequest.execute();
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("open " + authorizeURI);
        } catch(IOException e) {
            System.out.println(e.toString());
        }
    }

    public HashMap<String, String> currentUserAPI() {
        this.setAccessSpotifyApi();

        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile().build();

        HashMap<String, String> userInfo = new HashMap<String, String>();

        try {
            User user = getCurrentUsersProfileRequest.execute();

            userInfo.put("id", user.getId());
            userInfo.put("display_name", user.getDisplayName());
            userInfo.put("email", user.getEmail());
            userInfo.put("profile_pic", user.getImages()[0].getUrl());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return userInfo;
    }

    public void refreshTokenAPI() {
        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(authCode).build();
        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }

        this.refreshToken = spotifyApi.getRefreshToken();
    }

    public void accessTokenAPI() {

        this.setRefreshSpotifyApi();

        AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                .build();

        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }

        this.accessToken = spotifyApi.getAccessToken();

    }

    private void setRefreshSpotifyApi() {
        this.spotifyApi = new SpotifyApi.Builder().
                setClientId(LoginCredentialConstants.CLIENT_ID).
                setClientSecret(LoginCredentialConstants.CLIENT_SECRET).
                setRefreshToken(this.refreshToken).build();
    }

    public void setAccessSpotifyApi() {
        this.spotifyApi = new SpotifyApi.Builder().setAccessToken(this.accessToken).build();
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
}
