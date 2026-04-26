package com.chronicorn.frontend.managers.animationManager;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class VFXActor extends Actor {
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private Runnable onFinishCallback;
    private boolean isFinished = false;

    public VFXActor(Animation<TextureRegion> animation, Runnable onFinishCallback) {
        this.animation = animation;
        this.onFinishCallback = onFinishCallback;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (isFinished) return;

        stateTime += delta;

        // Only auto-kill if it's a standard, non-JSON animation (callback is NOT null)
        if (animation.isAnimationFinished(stateTime) && onFinishCallback != null) {
            isFinished = true;
            onFinishCallback.run();
            this.remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isFinished) return;

        // Fetch the correct frame based on elapsed time
        TextureRegion currentFrame = animation.getKeyFrame(stateTime);

        float width = currentFrame.getRegionWidth();
        float height = currentFrame.getRegionHeight();

        // 1. Fetch the actor's current color (this contains the alpha modified by fadeOut/fadeIn)
        com.badlogic.gdx.graphics.Color color = getColor();

        // 2. Apply it to the batch, multiplying by parentAlpha for proper Scene2D hierarchy blending
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        // 3. Draw the frame
        batch.draw(
            currentFrame,
            getX() - width / 2f, getY() - height / 2f, // x, y (bottom left corner)
            width / 2f, height / 2f,                   // originX, originY (center for rotation/scaling)
            width, height,                             // width, height
            getScaleX(), getScaleY(),                  // scaleX, scaleY
            getRotation()                              // rotation
        );

        // 4. Reset the batch color back to pure white so other actors aren't affected
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
    }
}
