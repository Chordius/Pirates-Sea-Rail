package com.chronicorn.backend.dto;

public class BannerItem {
    private String id;
    private int rarity;
    private boolean isWeapon;

    public BannerItem(String id, int rarity, boolean isWeapon) {
        this.id = id;
        this.rarity = rarity;
        this.isWeapon = isWeapon;
    }

    public String getId() {
        return id;
    }

    public int getRarity() {
        return rarity;
    }

    public boolean isWeapon() {
        return isWeapon;
    }
}