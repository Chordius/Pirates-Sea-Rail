package com.chronicorn.frontend.managers.networkManager;

public interface NetworkCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}
