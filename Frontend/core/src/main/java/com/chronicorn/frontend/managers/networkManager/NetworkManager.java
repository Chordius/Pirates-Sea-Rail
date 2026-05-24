package com.chronicorn.frontend.managers.networkManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
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

    private static int getOfflinePremiumCurrency() {
        if (Gdx.app == null) return 1600;
        Preferences prefs = Gdx.app.getPreferences("OfflineUserData");
        return prefs.getInteger("premiumCurrency", 1600);
    }

    private static void setOfflinePremiumCurrency(int amount) {
        if (Gdx.app != null) {
            Preferences prefs = Gdx.app.getPreferences("OfflineUserData");
            prefs.putInteger("premiumCurrency", amount);
            prefs.flush();
        }
    }

    private static final Json json = new Json();

    static {
        // Ensures LibGDX outputs standard JSON that Spring Boot's Jackson parser can read
        json.setOutputType(JsonWriter.OutputType.json);
        // Ignore fields that are not in the DTO (prevents crashes on unexpected backend properties)
        json.setIgnoreUnknownFields(true);
    }

    // ==========================================
    // SPECIFIC API ENDPOINTS
    // ==========================================

    public static void register(String email, String password, String username, NetworkCallback<UserAuthResponse> callback) {
        if (NetworkConfigure.isOffline()) {
            UserAuthResponse response = new UserAuthResponse();
            response.localUserId = "offline_user";
            response.username = username != null ? username : "Offline Sailor";
            response.premiumCurrency = getOfflinePremiumCurrency();
            Gdx.app.postRunnable(() -> callback.onSuccess(response));
            return;
        }
        UserAuthRequest request = new UserAuthRequest(email, password, username);
        sendPostRequest("/users/register", request, UserAuthResponse.class, callback);
    }

    public static void login(String email, String password, NetworkCallback<UserAuthResponse> callback) {
        if (NetworkConfigure.isOffline()) {
            UserAuthResponse response = new UserAuthResponse();
            response.localUserId = "offline_user";
            response.username = "Offline Sailor";
            response.premiumCurrency = getOfflinePremiumCurrency();
            Gdx.app.postRunnable(() -> callback.onSuccess(response));
            return;
        }
        UserAuthRequest request = new UserAuthRequest(email, password, null);
        sendPostRequest("/users/login", request, UserAuthResponse.class, callback);
    }

    public static void verifyParty(String userId, String[] partyCharIds, NetworkCallback<Boolean> callback) {
        if (NetworkConfigure.isOffline()) {
            Gdx.app.postRunnable(() -> callback.onSuccess(true));
            return;
        }
        PartyRequest request = new PartyRequest(userId, partyCharIds);
        sendPostRequest("/gacha/verify", request, Boolean.class, callback);
    }

    public static void pullGacha(String userId, String bannerId, NetworkCallback<GachaResult> callback) {
        if (NetworkConfigure.isOffline()) {
            int currency = getOfflinePremiumCurrency();
            if (currency < 160) {
                Gdx.app.postRunnable(() -> callback.onError("Insufficient premium currency"));
                return;
            }
            setOfflinePremiumCurrency(currency - 160);

            String[] pool = {"C001", "C002", "C003", "C004", "W001", "W002", "W003", "W004"};
            String rolled = pool[(int)(Math.random() * pool.length)];

            GachaResult res = new GachaResult();
            res.pulledCharId = rolled;
            boolean isNew = true;
            try {
                if (GameSession.getInstance().getParty() != null) {
                    isNew = !GameSession.getInstance().getParty().getOwnedCharacters().containsKey(rolled);
                }
            } catch (Exception ignored) {}
            res.isNew = isNew;

            Gdx.app.postRunnable(() -> callback.onSuccess(res));
            return;
        }
        sendPostRequest("/gacha/pull/" + userId + "?bannerId=" + bannerId, null, GachaResult.class, callback);
    }

    public static void pull10Gacha(String userId, String bannerId, NetworkCallback<GachaResult[]> callback) {
        if (NetworkConfigure.isOffline()) {
            int currency = getOfflinePremiumCurrency();
            if (currency < 1600) {
                Gdx.app.postRunnable(() -> callback.onError("Insufficient premium currency"));
                return;
            }
            setOfflinePremiumCurrency(currency - 1600);

            GachaResult[] results = new GachaResult[10];
            String[] pool = {"C001", "C002", "C003", "C004", "W001", "W002", "W003", "W004"};
            for (int i = 0; i < 10; i++) {
                String rolled = pool[(int)(Math.random() * pool.length)];
                GachaResult res = new GachaResult();
                res.pulledCharId = rolled;
                boolean isNew = true;
                try {
                    if (GameSession.getInstance().getParty() != null) {
                        isNew = !GameSession.getInstance().getParty().getOwnedCharacters().containsKey(rolled);
                    }
                } catch (Exception ignored) {}
                res.isNew = isNew;
                results[i] = res;
            }

            Gdx.app.postRunnable(() -> callback.onSuccess(results));
            return;
        }
        sendPostRequest("/gacha/pull10/" + userId + "?bannerId=" + bannerId, null, GachaResult[].class, callback);
    }

    public static void buyCurrency(String localUserId, double cost, int currencyAmount, NetworkCallback<String> callback) {
        if (NetworkConfigure.isOffline()) {
            int currency = getOfflinePremiumCurrency();
            setOfflinePremiumCurrency(currency + currencyAmount);
            Gdx.app.postRunnable(() -> callback.onSuccess("Purchase Successful (Offline)"));
            return;
        }
        PurchaseRequest request = new PurchaseRequest(localUserId, cost, currencyAmount);
        sendPostRequest("/payment/buy-currency", request, String.class, callback);
    }

    public static void grantCurrency(String userId, int amount, String key, NetworkCallback<UserAuthResponse> callback) {
        if (NetworkConfigure.isOffline()) {
            int currency = getOfflinePremiumCurrency();
            setOfflinePremiumCurrency(currency + amount);
            UserAuthResponse response = new UserAuthResponse();
            response.localUserId = "offline_user";
            response.username = "Offline Sailor";
            response.premiumCurrency = getOfflinePremiumCurrency();
            Gdx.app.postRunnable(() -> callback.onSuccess(response));
            return;
        }
        sendPostRequest("/users/" + userId + "/grant-currency?amount=" + amount + "&key=" + key, null, UserAuthResponse.class, callback);
    }

    public static void grantCharacter(String userId, String charId, NetworkCallback<GachaResult> callback) {
        if (NetworkConfigure.isOffline()) {
            GachaResult result = new GachaResult();
            result.pulledCharId = charId;
            boolean isNew = true;
            try {
                if (GameSession.getInstance().getParty() != null) {
                    isNew = !GameSession.getInstance().getParty().getOwnedCharacters().containsKey(charId);
                }
            } catch (Exception ignored) {}
            result.isNew = isNew;
            Gdx.app.postRunnable(() -> callback.onSuccess(result));
            return;
        }
        sendPostRequest("/gacha/grant/" + userId + "/" + charId, null, GachaResult.class, callback);
    }

    public static void getUserInfo(String localUserId, NetworkCallback<UserAuthResponse> callback) {
        if (NetworkConfigure.isOffline()) {
            UserAuthResponse response = new UserAuthResponse();
            response.localUserId = "offline_user";
            response.username = "Offline Sailor";
            response.premiumCurrency = getOfflinePremiumCurrency();
            Gdx.app.postRunnable(() -> callback.onSuccess(response));
            return;
        }
        sendGetRequest("/users/" + localUserId, UserAuthResponse.class, callback);
    }

    // HELPER METHOD
    private static <T> void sendPostRequest(String endpoint, Object payload, final Class<T> responseType, final NetworkCallback<T> callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(NetworkConfigure.getBaseUrl() + endpoint)
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

    private static <T> void sendGetRequest(String endpoint, final Class<T> responseType, final NetworkCallback<T> callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.GET)
            .url(NetworkConfigure.getBaseUrl() + endpoint)
            .header("Accept", "application/json")
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                final String responseString = httpResponse.getResultAsString();

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
