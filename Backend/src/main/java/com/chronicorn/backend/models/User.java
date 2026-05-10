package com.chronicorn.backend.models;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "global_user_id", updatable = false, nullable = false)
    private UUID globalUserId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "premium_currency", nullable = false)
    private int premiumCurrency = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getGlobalUserId() { return globalUserId; }
    public void setGlobalUserId(UUID globalUserId) { this.globalUserId = globalUserId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getPremiumCurrency() { return premiumCurrency; }
    public void setPremiumCurrency(int premiumCurrency) { this.premiumCurrency = premiumCurrency; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
}
