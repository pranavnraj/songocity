package com.songbirds.app;

import com.songbirds.util.AppConstants;
import com.songbirds.util.LoginCredentialConstants;
import com.songbirds.util.MongoDBConstants;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import com.wrapper.spotify.model_objects.specification.*;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import com.wrapper.spotify.requests.data.browse.GetRecommendationsRequest;
import com.wrapper.spotify.requests.data.follow.UnfollowPlaylistRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import com.wrapper.spotify.requests.data.player.GetCurrentUsersRecentlyPlayedTracksRequest;
import com.wrapper.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.hc.core5.http.ParseException;


import java.util.*;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SpotifyWebAPI {

    private SpotifyApi spotifyApi;
    private final URI redirectURI;

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

    /*
    public synchronized void initializeAPI() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(LoginCredentialConstants.CLIENT_ID)
                .setClientSecret(LoginCredentialConstants.CLIENT_SECRET)
                .setRedirectUri(redirectURI)
                .build();
    }*/

    public synchronized HashMap<String, String> currentUserAPI(String id) {
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

    public synchronized ArrayList<String> currentUserTopArtists(String id){
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

    public synchronized ArrayList<String> currentUserTopTracks(String id){
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

    public synchronized ArrayList<String> currentUserRecentTracks(String id){
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

    public synchronized HashMap<String,String> currentUserPlaylists(String id){
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

    public HashMap<String,String> getTracks(String playlistId) throws SpotifyWebApiException {
        GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId).build();
        HashMap<String,String> playlistTracks = new HashMap<String,String>();

        while(true) {
            try {
                Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();

                if (playlistTrackPaging.getNext() == null) {
                    PlaylistTrack[] items = playlistTrackPaging.getItems();

                    for (PlaylistTrack track : items) {
                        if (track.getTrack() == null) {
                            continue;
                        }

                        // TODO Toggle between recency and all
                        Date currentDate = new Date();
                        Date trackDate = track.getAddedAt();
                        //LOGGER.log(Level.INFO, "Current Date: " + currentDate.getTime());
                        //LOGGER.log(Level.INFO, "Milliseconds in yr: " + AppConstants.MILLISECONDS_IN_YEAR);
                        //LOGGER.log(Level.INFO, "track Date: " + trackDate.getTime());
                        if (currentDate.getTime() - AppConstants.MILLISECONDS_IN_YEAR < trackDate.getTime()) {
                            playlistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                        }
                        //playlistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                    }
                } else {
                    int offset = 0;
                    while (playlistTrackPaging.getNext() != null) {
                        PlaylistTrack[] items = playlistTrackPaging.getItems();

                        for (PlaylistTrack track : items) {
                            if (track.getTrack() == null) {
                                continue;
                            }

                            playlistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                        }
                        offset += 100;
                        getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId).offset(offset).build();
                        playlistTrackPaging = getPlaylistsItemsRequest.execute();
                    }
                    PlaylistTrack[] items = playlistTrackPaging.getItems();

                    for (PlaylistTrack track : items) {
                        if (track.getTrack() == null) {
                            continue;
                        }

                        // TODO Toggle between recency and all
                        Date currentDate = new Date();
                        Date trackDate = track.getAddedAt();
                        LOGGER.log(Level.INFO, "Current Date: " + currentDate.getTime());
                        LOGGER.log(Level.INFO, "Milliseconds in yr: " + AppConstants.MILLISECONDS_IN_YEAR);
                        LOGGER.log(Level.INFO, "track Date: " + trackDate.getTime());
                        if (currentDate.getTime() - AppConstants.MILLISECONDS_IN_YEAR < trackDate.getTime()) {
                            playlistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                        }
                        //playlistTracks.put(track.getTrack().getId(), track.getTrack().getName());
                    }
                }
                break;

            } catch (TooManyRequestsException e) {
                LOGGER.log(Level.INFO, "Rate Limit: Pausing for a few seconds before trying again");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException a) {
                    a.printStackTrace();
                }
            } catch (IOException | ParseException e) {
                System.out.print("Error: " + e.getMessage());
            }
        }

        LOGGER.log(Level.INFO, playlistTracks.toString());

        System.out.println("Songs: " + playlistTracks.size());

        return playlistTracks;
    }

    public int getNumTracks(HashMap<String, HashMap<String, HashMap<String, Float>>> playlistsInfo){
        int numTracks = 0;
        for(String playlist: playlistsInfo.keySet()){
            numTracks += playlistsInfo.get(playlist).keySet().size();
        }
        return numTracks;
    }

    public synchronized List<String> getFilteredTracks(List<String> track_uris, String userID) {
        this.reprimeAPI(userID);

        HashMap<String, String> filteredList = new HashMap<String, String>();

        String[] ids = track_uris.toArray(new String[0]);

        LOGGER.log(Level.INFO, "Track URIS prefiltered: " + track_uris.toString());

        for(int i = 0; i < ids.length; i += 50) {
            int limit = Integer.min(ids.length, i + 50);

            String[] tracks = Arrays.copyOfRange(ids, i, limit);
            GetSeveralTracksRequest getSeveralTracksRequest = spotifyApi.getSeveralTracks(tracks).build();

            try {
                Track[] subTracks = getSeveralTracksRequest.execute();

                for (int j = 0; j < subTracks.length; j++) {
                    Track track = subTracks[j];

                    if (track != null) {
                        filteredList.put(track.getName(), track.getId());
                    }
                }
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                e.printStackTrace();
            }

        }

        List<String> list = new ArrayList(filteredList.values());
        LOGGER.log(Level.INFO, "Track URIS post-filtered: " + list.toString());

        return list;
    }

    public HashMap<String, HashMap<String, Float>> getTracksInfo(HashMap<String,String> playlistTracks) throws SpotifyWebApiException {

        String[] ids = playlistTracks.keySet().toArray(new String[0]);
        System.out.println(ids.length);
        HashMap<String, HashMap<String, Float>> tracksInfo = new HashMap<String, HashMap<String, Float>>();
        for(int i = 0; i < ids.length; i += 50) {
            String[] subset_ids = Arrays.copyOfRange(ids, i, i + 50);

            GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest = spotifyApi.getAudioFeaturesForSeveralTracks(subset_ids).build();
            GetSeveralTracksRequest getSeveralTracksRequest = spotifyApi.getSeveralTracks(subset_ids).build();

            while(true) {
                try {
                    AudioFeatures[] audioFeatures = getAudioFeaturesForSeveralTracksRequest.execute();
                    Track[] tracks = getSeveralTracksRequest.execute();

                    for (int j = 0; j < audioFeatures.length; j++) {
                        AudioFeatures trackInfo = audioFeatures[j];
                        Track track = tracks[j];
                        if (trackInfo != null) {

                            int explicitFlag;
                            if (track.getIsExplicit() == true) {
                                explicitFlag = 1;
                            } else {
                                explicitFlag = 0;
                            }

                            System.out.println(track.getId());
                            System.out.println(track.getName());
                            tracksInfo.put(trackInfo.getId(), new HashMap<String, Float>());
                            //tracksInfo.get(trackInfo.getId()).put("popularity", (float)track.getPopularity());
                            //tracksInfo.get(trackInfo.getId()).put("duration", (float)track.getDurationMs());
                            //tracksInfo.get(trackInfo.getId()).put("explicit", (float)explicitFlag);
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
                    break;
                } catch (TooManyRequestsException e) {
                    LOGGER.log(Level.INFO, "Rate Limit: Pausing for a few seconds before trying again");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException a) {
                        a.printStackTrace();
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return tracksInfo;
    }

    public ArrayList<String> getUniqueGenres(){
        ArrayList<String> uniqueGenres = new ArrayList<String>();

        return uniqueGenres;
    }

    public HashMap<String, String> getRecommendations(int limit) throws SpotifyWebApiException {
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

            while(true) {
                try {
                    Recommendations recommendations = getRecommendationsRequest.execute();
                    TrackSimplified[] tracks = recommendations.getTracks();

                    for (TrackSimplified track : tracks) {
                        recs.put(track.getId(), track.getName());
                    }
                    break;
                } catch (TooManyRequestsException e) {
                    LOGGER.log(Level.INFO, "Rate Limit: Pausing for a few seconds before trying again");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException a) {
                        a.printStackTrace();
                    }
                } catch (IOException |  ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return recs;
    }

    public HashMap<String, HashMap<String, HashMap<String, Float>>>  generateUserData(String user_id) throws SpotifyWebApiException{
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

    public synchronized String createPlaylist(String userId, String title){
        this.reprimeAPI(userId);

        final CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, title).description("Playlist created based on friends in title tastes").build();
        String playlistID = "";
        try {
            final Playlist playlist = createPlaylistRequest.execute();
            playlistID = playlist.getId();

            LOGGER.log(Level.INFO, "Created playlist: " + playlist.getName());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.log(Level.SEVERE, "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return playlistID;

    }

    public synchronized void addTracksToPlaylist(String playlistID, String[] recTracks, String userID) {
        this.reprimeAPI(userID);

        for(int i = 0; i < recTracks.length; i += 75) {

            int limit = Integer.min(recTracks.length, i + 75);

            String[] tracks = Arrays.copyOfRange(recTracks, i, limit);
            LOGGER.log(Level.INFO, "Tracks Length: " + tracks.length);

            AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistID, tracks).build();

            try {
                SnapshotResult snapshotResult = addItemsToPlaylistRequest.execute();
                if (snapshotResult != null)
                    LOGGER.log(Level.INFO, snapshotResult.getSnapshotId());
                else
                    LOGGER.log(Level.SEVERE, "Null Snapshot ID");

            } catch (IOException | SpotifyWebApiException | ParseException e) {
                LOGGER.log(Level.SEVERE,"Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String unfollowPlaylist(String playlistID) {

        String response;
        UnfollowPlaylistRequest unfollowPlaylistRequest = spotifyApi.unfollowPlaylist(playlistID).build();
        try {
            response = unfollowPlaylistRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.log(Level.SEVERE,"Error: " + e.getMessage());
            e.printStackTrace();
            return "Error deleting playlist";
        }

        return response;
    }

    public synchronized String storeTokensUponLogin(String authCode) {

        SpotifyApi tempSpotifyApi = new SpotifyApi.Builder()
                .setClientId(LoginCredentialConstants.CLIENT_ID)
                .setClientSecret(LoginCredentialConstants.CLIENT_SECRET)
                .setRedirectUri(redirectURI)
                .build();
        String id = "";

        AuthorizationCodeRequest authorizationCodeRequest = tempSpotifyApi.authorizationCode(authCode).build();
        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            tempSpotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            tempSpotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = tempSpotifyApi.getCurrentUsersProfile().build();
            User user = getCurrentUsersProfileRequest.execute();
            id = user.getId();

            mongoClient.storeAccessAndRefreshTokens(id, tempSpotifyApi.getAccessToken(), tempSpotifyApi.getRefreshToken(),
                    (long)authorizationCodeCredentials.getExpiresIn()*1000, System.currentTimeMillis());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("First time access token error");
            System.out.println("Error: " + e.getMessage());
        }

        return id;
    }

    public synchronized void reprimeAPI(String id) {
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
