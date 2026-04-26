package com.chronicorn.frontend.states;

import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.contants.Direction;

public class DashingState implements PlayerState {
    private float timer;

    private final float DASH_DURATION = 0.065f;
    private final float DASH_SPEED = 2500f;

    // Variable untuk interval spawn ghost
    private float ghostSpawnTimer = 0;
    private final float GHOST_INTERVAL = 0.02f;

    public DashingState() {
        this.timer = DASH_DURATION;
    }

    @Override
    public void handleInput(Player player) {
        // Kosong agar Player tidak bisa belok saat sedang dash
    }

    @Override
    public void update(Player player, float delta) {
        timer -= delta;

        // Logika efek bayangan
        ghostSpawnTimer -= delta;
        if (ghostSpawnTimer <= 0) {
            player.spawnGhost();
            ghostSpawnTimer = GHOST_INTERVAL; // Reset timer
        }

        // 1. Terapkan kecepatan tinggi konstan
        applyDashVelocity(player);

        // 2. Jika durasi habis
        if (timer <= 0) {
            // Hentikan momentum seketika agar tidak slippery
            player.transitionToWalkSpeed();
            player.changeState(new NormalState());
        }
    }

    private void applyDashVelocity(Player player) {
        float dx = 0;
        float dy = 0;
        Direction dir = player.getCurrentDirection();

        switch (dir) {
            case LEFT:  dx = -1; break;
            case RIGHT: dx = 1; break;
            case UP:    dy = 1; break;
            case DOWN:  dy = -1; break;
        }
        // Memaksa kecepatan langsung ke angka maksimum (Instant burst)
        player.setExactVelocity(dx * DASH_SPEED, dy * DASH_SPEED);
    }

    @Override
    public void onDashCommand(Player player) {
        // Tidak bisa dash lagi saat sedang dash
    }
}
