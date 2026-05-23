package com.chronicorn.backend.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_pity", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "banner_type" })
})
public class UserPity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "pity_id")
    private UUID pityId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "banner_type", nullable = false, length = 50)
    private String bannerType;

    @Column(name = "pity_count", nullable = false)
    private int pityCount;

    public UserPity() {
    }

    public UserPity(UUID userId, String bannerType, int pityCount) {
        this.userId = userId;
        this.bannerType = bannerType;
        this.pityCount = pityCount;
    }

    public void incrementPity() {
        this.pityCount++;
    }

    public void resetPity() {
        this.pityCount = 0;
    }

    public UUID getPityId() {
        return pityId;
    }

    public void setPityId(UUID pityId) {
        this.pityId = pityId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getBannerType() {
        return bannerType;
    }

    public void setBannerType(String bannerType) {
        this.bannerType = bannerType;
    }

    public int getPityCount() {
        return pityCount;
    }

    public void setPityCount(int pityCount) {
        this.pityCount = pityCount;
    }
}