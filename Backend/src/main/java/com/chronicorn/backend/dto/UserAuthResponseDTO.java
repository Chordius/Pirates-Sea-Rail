package com.chronicorn.backend.dto;

import java.util.UUID;

// 2. What Spring Boot sends back to LibGDX on successful login
public class UserAuthResponseDTO {
    private UUID localUserId;
    private UUID globalUserId;
    private String username;
    private int premiumCurrency;

    public UserAuthResponseDTO(UUID localUserId, UUID globalUserId, String username, int premiumCurrency) {
        this.localUserId = localUserId;
        this.globalUserId = globalUserId;
        this.username = username;
        this.premiumCurrency = premiumCurrency;
    }
    // Getters
    public UUID getLocalUserId() { return localUserId; }
    public UUID getGlobalUserId() { return globalUserId; }
    public String getUsername() { return username; }
    public int getPremiumCurrency() { return premiumCurrency; }
}