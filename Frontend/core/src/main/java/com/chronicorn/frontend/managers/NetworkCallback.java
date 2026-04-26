package com.chronicorn.frontend.managers;

public interface NetworkCallback {
    void onSuccess(String response);
    void onError(String errorMessage);
}
