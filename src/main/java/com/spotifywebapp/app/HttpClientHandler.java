package com.spotifywebapp.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientHandler {

    private HttpClient client;
    private HttpRequest currRequest;

    public HttpClientHandler() {
        this.client = HttpClient.newHttpClient();
    }


    public void formRequest(String url) {
        try {
            currRequest = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
        } catch (URISyntaxException e) {
            System.out.println("Error" + e.toString());
        }
    }

    public void formRequest(String url, String body) {
        try {
            currRequest = HttpRequest.newBuilder().uri(new URI(url)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
        } catch (URISyntaxException e) {
            System.out.println("Error" + e.toString());
        }
    }

    public HttpResponse sendRequest() {

        HttpResponse response = null;

        try {
             response = this.client.send(currRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("Error" + e.toString());
        } catch (InterruptedException e) {
            System.out.println("Error" + e.toString());
        }

        return response;
    }

}
