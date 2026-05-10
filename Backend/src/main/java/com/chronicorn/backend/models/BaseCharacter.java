package com.chronicorn.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "base_characters")
public class BaseCharacter {

    @Id
    @Column(name = "char_id", length = 50, nullable = false)
    private String charId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int rarity;

    // Getters and Setters
    public String getCharId() { return charId; }
    public void setCharId(String charId) { this.charId = charId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getRarity() { return rarity; }
    public void setRarity(int rarity) { this.rarity = rarity; }
}
