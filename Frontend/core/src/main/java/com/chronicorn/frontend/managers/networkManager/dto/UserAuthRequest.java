package com.chronicorn.frontend.managers.networkManager.dto;

public class UserAuthRequest {
    public String email;
    public String password;
    public String username; // Nullable for login, required for registration

    // LibGDX's JSON parser requires an empty constructor
    public UserAuthRequest() {}

    public UserAuthRequest(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
}
