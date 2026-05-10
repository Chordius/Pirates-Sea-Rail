package com.chronicorn.frontend.managers.networkManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.chronicorn.frontend.managers.networkManager.dto.*;

public class NetworkManager {
    private static NetworkManager instance;
    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    // Local host
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final Json json = new Json();

    static {
        // Ensures LibGDX outputs standard JSON that Spring Boot's Jackson parser can read
        json.setOutputType(JsonWriter.OutputType.json);
    }

    // ==========================================
    // SPECIFIC API ENDPOINTS
    // ==========================================

    public static void register(String email, String password, String username, NetworkCallback<UserAuthResponse> callback) {
        UserAuthRequest request = new UserAuthRequest(email, password, username);
        sendPostRequest("/users/register", request, UserAuthResponse.class, callback);
    }

    public static void login(String email, String password, NetworkCallback<UserAuthResponse> callback) {
        // Assuming you made a simple LibGDX class for UserAuthRequest
        UserAuthRequest request = new UserAuthRequest(email, password, null);
        sendPostRequest("/users/login", request, UserAuthResponse.class, callback);
    }

    public static void verifyParty(String userId, String[] partyCharIds, NetworkCallback<Boolean> callback) {
        PartyRequest request = new PartyRequest(userId, partyCharIds);
        sendPostRequest("/gacha/verify", request, Boolean.class, callback);
    }

    public static void pullGacha(String userId, NetworkCallback<GachaResult> callback) {
        // Notice payload is null here because the UUID is passed in the URL path
        sendPostRequest("/gacha/pull/" + userId, null, GachaResult.class, callback);
    }

    public static void buyCurrency(String localUserId, double cost, int currencyAmount, NetworkCallback<String> callback) {
        PurchaseRequest request = new PurchaseRequest(localUserId, cost, currencyAmount);
        sendPostRequest("/payment/buy-currency", request, String.class, callback);
    }

    // HELPER METHOD
    private static <T> void sendPostRequest(String endpoint, Object payload, final Class<T> responseType, final NetworkCallback<T> callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(BASE_URL + endpoint)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .content(payload != null ? json.toJson(payload) : "")
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                final String responseString = httpResponse.getResultAsString();

                // Route back to the main LibGDX thread so UI updates don't crash the game
                Gdx.app.postRunnable(() -> {
                    if (statusCode >= 200 && statusCode < 300) {
                        if (responseType == String.class) {
                            callback.onSuccess((T) responseString);
                        } else if (responseType == Boolean.class) {
                            callback.onSuccess((T) Boolean.valueOf(responseString));
                        } else {
                            callback.onSuccess(json.fromJson(responseType, responseString));
                        }
                    } else {
                        // Extract the error message Spring Boot sent back
                        callback.onError("Error " + statusCode + ": " + responseString);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> callback.onError("Network Failure: " + t.getMessage()));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onError("Request Cancelled"));
            }
        });
    }
}
