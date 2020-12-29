package com.spotifywebapp.app;

import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import com.wrapper.spotify.model_objects.specification.*;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import com.wrapper.spotify.requests.data.player.GetCurrentUsersRecentlyPlayedTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.hc.core5.http.ParseException;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

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
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri().scope("user-top-read,user-read-recently-played").build();
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

    public ArrayList<String> currentUserTopArtists(){
        this.setAccessSpotifyApi();

        GetUsersTopArtistsRequest getUsersTopArtistsRequest = spotifyApi.getUsersTopArtists().build();
        ArrayList<String> artists = new ArrayList<String>();
        try{
            Paging<Artist> artistPaging = getUsersTopArtistsRequest.execute();

            Artist[] items = artistPaging.getItems();

            for(Artist artist: items) {
                artists.add(artist.getName());
            }
        }catch(IOException | SpotifyWebApiException | ParseException e)
        {
            System.out.println("Error: " + e.getMessage());
        }

        return artists;
    }

    public ArrayList<String> currentUserTopTracks(){
        this.setAccessSpotifyApi();

        GetUsersTopTracksRequest getUsersTopTracksRequest = spotifyApi.getUsersTopTracks().build();
        ArrayList<String> tracks = new ArrayList<String>();
        try{
            Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            Track[] items = trackPaging.getItems();

            for(Track track : items){
                tracks.add(track.getName());
            }

        }catch(IOException | SpotifyWebApiException | ParseException e){
            System.out.print("Error: " + e.getMessage());
        }

        return tracks;
    }

    public ArrayList<String> currentUserRecentTracks(){
        this.setAccessSpotifyApi();

        GetCurrentUsersRecentlyPlayedTracksRequest getCurrentUsersRecentlyPlayedTracksRequest = spotifyApi.getCurrentUsersRecentlyPlayedTracks().build();
        ArrayList<String> recentTracks = new ArrayList<String>();
        try{
            PagingCursorbased<PlayHistory> playHistoryPagingCursorbased = getCurrentUsersRecentlyPlayedTracksRequest.execute();

            PlayHistory[] recentItems = playHistoryPagingCursorbased.getItems();

            for(PlayHistory track : recentItems){
                recentTracks.add(track.getTrack().getName());
            }

        }catch(IOException | SpotifyWebApiException | ParseException e){
            System.out.print("Error: " + e.getMessage());
        }

        return recentTracks;
    }

    public HashMap<String,String> currentUserPlaylists(){
        this.setAccessSpotifyApi();

        GetListOfCurrentUsersPlaylistsRequest getListOfCurrentUsersPlaylistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        HashMap<String, String> playlistInfo = new HashMap<String, String>();
        try{
            Paging <PlaylistSimplified> playlistSimplifiedPaging = getListOfCurrentUsersPlaylistsRequest.execute();

            PlaylistSimplified[] items = playlistSimplifiedPaging.getItems();

            for(PlaylistSimplified playlist: items){
                playlistInfo.put(playlist.getId(),playlist.getName());
            }
        }catch(IOException | SpotifyWebApiException | ParseException e) {
            System.out.print("Error: " + e.getMessage());
        }

        return playlistInfo;
    }

    public HashMap<String,String> getTracks(String playlistId){
        GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId).build();
        HashMap<String,String> playistTracks = new HashMap<String,String>();
        try{
            Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();

            PlaylistTrack[] items = playlistTrackPaging.getItems();

            for(PlaylistTrack track : items){
                playistTracks.put(track.getTrack().getId(),track.getTrack().getName());
            }

        }catch(IOException | SpotifyWebApiException | ParseException e) {
            System.out.print("Error: " + e.getMessage());
        }

        return playistTracks;
    }

    public HashMap<String, HashMap<String, Float>> getTracksInfo(HashMap<String,String> playlistTracks) {
        //this.setAccessSpotifyApi();

        String[] ids = playlistTracks.keySet().toArray(new String[0]);
        GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest = spotifyApi.getAudioFeaturesForSeveralTracks(ids).build();
        HashMap<String,HashMap<String,Float>> tracksInfo = new HashMap<String,HashMap<String,Float>>();
        try {
            AudioFeatures[] audioFeatures = getAudioFeaturesForSeveralTracksRequest.execute();

            for (AudioFeatures trackInfo : audioFeatures) {
                if(trackInfo != null) {
                    tracksInfo.put(trackInfo.getId(), new HashMap<String, Float>());
                    tracksInfo.get(trackInfo.getId()).put("acousticness", trackInfo.getAcousticness());
                    tracksInfo.get(trackInfo.getId()).put("danceability", trackInfo.getDanceability());
                    tracksInfo.get(trackInfo.getId()).put("duration", trackInfo.getDanceability());
                    tracksInfo.get(trackInfo.getId()).put("energy", trackInfo.getEnergy());
                    tracksInfo.get(trackInfo.getId()).put("instrumentalness", trackInfo.getInstrumentalness());
                    tracksInfo.get(trackInfo.getId()).put("mainKey", trackInfo.getKey().floatValue());
                    tracksInfo.get(trackInfo.getId()).put("liveness", trackInfo.getLiveness());
                    tracksInfo.get(trackInfo.getId()).put("loudness", trackInfo.getLoudness());
                    tracksInfo.get(trackInfo.getId()).put("tempo", trackInfo.getTempo());
                    tracksInfo.get(trackInfo.getId()).put("timeSignature", trackInfo.getTimeSignature().floatValue());
                    tracksInfo.get(trackInfo.getId()).put("valence", trackInfo.getValence());
                }
            }
        }catch(IOException | SpotifyWebApiException | ParseException e) {
            System.out.print("Error: " + e.getMessage());
        }
        return tracksInfo;
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
