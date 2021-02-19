package com.songbirds.app;

import com.songbirds.util.LoginCredentialConstants;
import com.songbirds.util.MongoDBConstants;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import com.wrapper.spotify.model_objects.specification.*;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import com.wrapper.spotify.requests.data.browse.GetRecommendationsRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import com.wrapper.spotify.requests.data.player.GetCurrentUsersRecentlyPlayedTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.hc.core5.http.ParseException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SpotifyWebAPI {

    private SpotifyApi spotifyApi;
    private final URI redirectURI;
    private String authCode;
    private String refreshToken;
    private String accessToken;

    private static MongoDBClient mongoClient = MongoDBClient.getInstance();
    private static final Logger LOGGER = Logger.getLogger(SpotifyWebAPI.class.getName());
    private static final SpotifyWebAPI api = new SpotifyWebAPI();

    private SpotifyWebAPI() {
        LOGGER.setLevel(Level.INFO);
        redirectURI = SpotifyHttpManager.makeUri(LoginCredentialConstants.REDIRECT_URI);
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(LoginCredentialConstants.CLIENT_ID)
                .setClientSecret(LoginCredentialConstants.CLIENT_SECRET)
                .setRedirectUri(redirectURI)
                .build();
    }

    public static SpotifyWebAPI getInstance() {
        return api;
    }

    public void authorizeAPI() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(LoginCredentialConstants.CLIENT_ID)
                .setClientSecret(LoginCredentialConstants.CLIENT_SECRET)
                .setRedirectUri(redirectURI)
                .build();

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri().scope("user-top-read,user-read-recently-played,user-read-email").build();
        URI authorizeURI = authorizationCodeUriRequest.execute();
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("open " + authorizeURI);
        } catch(IOException e) {
            System.out.println(e.toString());
        }
    }

    public HashMap<String, String> currentUserAPI(String id) {
        this.reprimeAPI(id);

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

    public ArrayList<String> currentUserTopArtists(String id){
        this.reprimeAPI(id);

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

    public ArrayList<String> currentUserTopTracks(String id){
        this.reprimeAPI(id);

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

    public ArrayList<String> currentUserRecentTracks(String id){
        this.reprimeAPI(id);

        GetCurrentUsersRecentlyPlayedTracksRequest getCurrentUsersRecentlyPlayedTracksRequest = spotifyApi.getCurrentUsersRecentlyPlayedTracks().after(new Date(1608940800)).build();
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

    public HashMap<String,String> currentUserPlaylists(String id){
        this.reprimeAPI(id);

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

            if(playlistTrackPaging.getNext() == null){
                PlaylistTrack[] items = playlistTrackPaging.getItems();

                for (PlaylistTrack track : items) {
                    playistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                }
            }else {
                int offset = 0;
                while (playlistTrackPaging.getNext() != null) {
                    PlaylistTrack[] items = playlistTrackPaging.getItems();

                    for (PlaylistTrack track : items) {
                        playistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                    }
                    offset += 100;
                    getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId).offset(offset).build();
                    playlistTrackPaging = getPlaylistsItemsRequest.execute();
                }
                PlaylistTrack[] items = playlistTrackPaging.getItems();

                for (PlaylistTrack track : items) {
                    playistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                }
            }

        }catch(IOException | SpotifyWebApiException | ParseException e) {
            System.out.print("Error: " + e.getMessage());
        }

        System.out.println("Songs: " + playistTracks.size());

        return playistTracks;
    }

    public int getNumTracks(HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo){
        int numTracks = 0;
        for(String playlist: playlistsInfo.keySet()){
            numTracks += playlistsInfo.get(playlist).keySet().size();
        }
        return numTracks;
    }

    public HashMap<String, HashMap<String, Float>> getTracksInfo(HashMap<String,String> playlistTracks) {
        //this.setAccessSpotifyApi();

        String[] ids = playlistTracks.keySet().toArray(new String[0]);
        System.out.println(ids.length);
        HashMap<String, HashMap<String, Float>> tracksInfo = new HashMap<String, HashMap<String, Float>>();
        for(int i = 0; i < ids.length; i +=101) {
            String[] subset_ids = Arrays.copyOfRange(ids, i, i + 100);

            GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest = spotifyApi.getAudioFeaturesForSeveralTracks(subset_ids).build();

            try {
                AudioFeatures[] audioFeatures = getAudioFeaturesForSeveralTracksRequest.execute();

                for (AudioFeatures trackInfo : audioFeatures) {
                    if (trackInfo != null) {
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
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.print("Error: " + e.getMessage());
            }
        }
        return tracksInfo;
    }

    public ArrayList<String> getUniqueGenres(){
        ArrayList<String> uniqueGenres = new ArrayList<String>();


        return uniqueGenres;
    }

    public HashMap<String, String> getRecommendations(int limit){
        //this.setAccessSpotifyApi();
        HashMap<String, String> recs = new HashMap<String,String>();
        String [] genres = {    "acoustic",
                "afrobeat",
                "alt-rock",
                "alternative",
                "ambient",
                "anime",
                "black-metal",
                "bluegrass",
                "blues",
                "bossanova",
                "brazil",
                "breakbeat",
                "british",
                "cantopop",
                "chicago-house",
                "children",
                "chill",
                "classical",
                "club",
                "comedy",
                "country",
                "dance",
                "dancehall",
                "death-metal",
                "deep-house",
                "detroit-techno",
                "disco",
                "disney",
                "drum-and-bass",
                "dub",
                "dubstep",
                "edm",
                "electro",
                "electronic",
                "emo",
                "folk",
                "forro",
                "french",
                "funk",
                "garage",
                "german",
                "gospel",
                "goth",
                "grindcore",
                "groove",
                "grunge",
                "guitar",
                "happy",
                "hard-rock",
                "hardcore",
                "hardstyle",
                "heavy-metal",
                "hip-hop",
                "holidays",
                "honky-tonk",
                "house",
                "idm",
                "indian",
                "indie",
                "indie-pop",
                "industrial",
                "iranian",
                "j-dance",
                "j-idol",
                "j-pop",
                "j-rock",
                "jazz",
                "k-pop",
                "kids",
                "latin",
                "latino",
                "malay",
                "mandopop",
                "metal",
                "metal-misc",
                "metalcore",
                "minimal-techno",
                "movies",
                "mpb",
                "new-age",
                "new-release",
                "opera",
                "pagode",
                "party",
                "philippines-opm",
                "piano",
                "pop",
                "pop-film",
                "post-dubstep",
                "power-pop",
                "progressive-house",
                "psych-rock",
                "punk",
                "punk-rock",
                "r-n-b",
                "rainy-day",
                "reggae",
                "reggaeton",
                "road-trip",
                "rock",
                "rock-n-roll",
                "rockabilly",
                "romance",
                "sad",
                "salsa",
                "samba",
                "sertanejo",
                "show-tunes",
                "singer-songwriter",
                "ska",
                "sleep",
                "songwriter",
                "soul",
                "soundtracks",
                "spanish",
                "study",
                "summer",
                "swedish",
                "synth-pop",
                "tango",
                "techno",
                "trance",
                "trip-hop",
                "turkish",
                "work-out",
                "world-music"};

        for(String genre: genres) {
            GetRecommendationsRequest getRecommendationsRequest = spotifyApi.getRecommendations().seed_genres(genre).limit(limit).build();

            try {
                Recommendations recommendations = getRecommendationsRequest.execute();
                TrackSimplified[] tracks = recommendations.getTracks();

                for (TrackSimplified track : tracks) {
                    recs.put(track.getId(), track.getName());
                }

            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return recs;
    }

    public HashMap<String, HashMap<String, HashMap<String, Float>>>  generateUserData(String user_id) {
        HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo = new HashMap<String, HashMap<String, HashMap<String, Float>>>();
        HashMap<String, String> playlistInfo = currentUserPlaylists(user_id);
        for (String playlistId : playlistInfo.keySet()) {
            String playlistName = playlistInfo.get(playlistId);
            playlistsInfo.put(playlistId, new HashMap<String, HashMap<String, Float>>());
            System.out.println(playlistName + ": " + playlistId);
            HashMap<String, String> playlistTracks = getTracks(playlistId);
            HashMap<String, HashMap<String, Float>> playlistTrackInfo = getTracksInfo(playlistTracks);
            for (String trackId : playlistTrackInfo.keySet()) {
                String trackName = playlistTracks.get(trackId);
                playlistsInfo.get(playlistId).put(trackId, new HashMap<String, Float>());
                for (String feature : playlistTrackInfo.get(trackId).keySet()) {
                    playlistsInfo.get(playlistId).get(trackId).put(feature, playlistTrackInfo.get(trackId).get(feature));
//                    System.out.println(feature + ": " + playlistTrackInfo.get(trackId).get(feature));
                }
            }
        }
        return playlistsInfo;
    }

    public String storeTokensUponLogin(String authCode) {
        String id = "";

        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(authCode).build();
        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile().build();
            User user = getCurrentUsersProfileRequest.execute();
            id = user.getId();

            mongoClient.storeAccessAndRefreshTokens(id, spotifyApi.getAccessToken(), spotifyApi.getRefreshToken(),
                    (long)authorizationCodeCredentials.getExpiresIn()*1000, System.currentTimeMillis());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("First time access token error");
            System.out.println("Error: " + e.getMessage());
        }

        return id;
    }

    public void reprimeAPI(String id) {
        String userRefreshToken = mongoClient.retrieveRefreshToken(id);
        String userAccessToken = mongoClient.retrieveAccessToken(id);
        long tokenLifetime = mongoClient.retrieveTokenLifetime(id);
        long storedTime = mongoClient.retrieveStoredTime(id);

        if (System.currentTimeMillis() - tokenLifetime >= storedTime) {
            this.spotifyApi = new SpotifyApi.Builder().
                    setClientId(LoginCredentialConstants.CLIENT_ID).
                    setClientSecret(LoginCredentialConstants.CLIENT_SECRET).
                    setRefreshToken(userRefreshToken).build();

            LOGGER.log(Level.INFO ,"REFRESHING ACCESS TOKEN");

            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                    .build();

            try {
                final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

                mongoClient.storeAccessAndRefreshTokens(id, authorizationCodeCredentials.getAccessToken(),
                        MongoDBConstants.IGNORE_REFRESH,
                        (long)authorizationCodeCredentials.getExpiresIn()*1000, System.currentTimeMillis());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("Refresh access token error");
                System.out.println("Error: " + e.getMessage());
            }
        }
        else {
            this.spotifyApi = new SpotifyApi.Builder().
                    setClientId(LoginCredentialConstants.CLIENT_ID).
                    setClientSecret(LoginCredentialConstants.CLIENT_SECRET).
                    setRefreshToken(userRefreshToken).
                    setAccessToken(userAccessToken).build();

            LOGGER.log(Level.INFO ,"NO REFRESHED ACCESS TOKEN NEEDED");
        }



    }

}
