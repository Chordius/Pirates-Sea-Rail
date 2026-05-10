package com.chronicorn.backend.models;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_characters")
public class PlayerCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "instance_id", updatable = false, nullable = false)
    private UUID instanceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "char_id", nullable = false, length = 50)
    private String charId;

    @Column(nullable = false)
    private int level = 1;

    @Column(name = "dupes_count", nullable = false)
    private int dupesCount = 0;

    @Column(name = "acquired_at", insertable = false, updatable = false)
    private ZonedDateTime acquiredAt;

    public PlayerCharacter() {}

    public PlayerCharacter(UUID userId, String charId) {
        this.userId = userId;
        this.charId = charId;
    }

    // Getters and Setters
    public UUID getInstanceId() { return instanceId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getCharId() { return charId; }
    public void setCharId(String charId) { this.charId = charId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getDupesCount() { return dupesCount; }
    public void setDupesCount(int dupesCount) { this.dupesCount = dupesCount; }
    public ZonedDateTime getAcquiredAt() { return acquiredAt; }
}