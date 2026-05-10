package com.chronicorn.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "palyers")

public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // plaintext.
    private int score;
    private int hp;
    private int atk;
    private int def;

    // Default Constructor
    public PlayerEntity() {}

    // Constructor untuk Register
    public PlayerEntity(String username, String password) {
        this.username = username;
        this.password = password;
        this.score = 0;
        this.hp = 35;
        this.atk = 35;
        this.def = 25;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }
    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }
}
