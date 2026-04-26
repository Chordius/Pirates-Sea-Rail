package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.screens.MapScreen;
import com.badlogic.gdx.math.MathUtils;

public class CmdFade implements EventCommand {
    private MapScreen screen;
    private boolean fadeIn; // true = Black to Clear, false = Clear to Black
    private float speed;
    private boolean isDone = false;

    public CmdFade(MapScreen screen, boolean fadeIn, float speed) {
        this.screen = screen;
        this.fadeIn = fadeIn; // "Fade In" usually means screen becomes visible (Alpha 1 -> 0)
        this.speed = speed;
    }

    @Override
    public void start() {
        // Optional: Force start value to ensure smoothness
        // if (fadeIn) screen.fadeAlpha = 1f; else screen.fadeAlpha = 0f
    }

    @Override
    public void update(float delta) {
        if (fadeIn) {
            // Become Transparent (1.0 -> 0.0)
            screen.fadeAlpha -= speed * delta;
            if (screen.fadeAlpha <= 0f) {
                screen.fadeAlpha = 0f;
                isDone = true;
            }
        } else {
            // Become Black (0.0 -> 1.0)
            screen.fadeAlpha += speed * delta;
            if (screen.fadeAlpha >= 1f) {
                screen.fadeAlpha = 1f;
                isDone = true;
            }
        }
        // Clamp values just in case
        screen.fadeAlpha = MathUtils.clamp(screen.fadeAlpha, 0, 1);
    }

    @Override
    public boolean isFinished() {
        return isDone;
    }
}
