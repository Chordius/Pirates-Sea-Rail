package com.chronicorn.frontend.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;

public class NetworkManager {

    // Local host
    private static final String BASE_URL = "https://finpro-oop-kelompok-5-production.up.railway.app/api";

    // Singleton Instance
    private static NetworkManager instance;

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    // LOGIN
    public void login(String username, String password, final NetworkCallback callback) {
        // Format JSON
        String requestBody = "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }";

        // Mengirim ke endpoint /login
        sendPostRequest("/login", requestBody, callback);
    }

    // REGISTER
    public void register(String username, String password, final NetworkCallback callback) {
        String requestBody = "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }";
        sendPostRequest("/register", requestBody, callback);
    }

    // UPDATE / SAVE GAME
    // Score berisi Waktu (detik)
    public void saveGame(String username, int score, int hp, int atk, int def, final NetworkCallback callback) {
        String requestBody = "{" +
            "\"username\": \"" + username + "\"," +
            "\"score\": " + score + "," +
            "\"hp\": " + hp + "," +
            "\"atk\": " + atk + "," +
            "\"def\": " + def +
            "}";

        sendPostRequest("/update", requestBody, callback);
    }

    // LEADERBOARD
    public void getLeaderboard(final NetworkCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.GET)
            .url(BASE_URL + "/leaderboard")
            .header("Content-Type", "application/json")
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                if (httpResponse.getStatus().getStatusCode() == 200) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Error: " + httpResponse.getStatus().getStatusCode());
                }
            }

            @Override
            public void failed(Throwable t) {
                callback.onError("Connection Failed: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                callback.onError("Cancelled");
            }
        });
    }

    // HELPER METHOD
    private void sendPostRequest(String endpoint, String jsonBody, final NetworkCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(BASE_URL + endpoint)
            .header("Content-Type", "application/json")
            .content(jsonBody)
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String result = httpResponse.getResultAsString();

                if (statusCode == 200) {
                    Gdx.app.postRunnable(() -> callback.onSuccess(result));
                } else {
                    Gdx.app.postRunnable(() -> callback.onError("Error " + statusCode + ": " + result));
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> callback.onError("Network Error: " + t.getMessage()));
            }

            @Override
            public void cancelled() { }
        });
    }
}
