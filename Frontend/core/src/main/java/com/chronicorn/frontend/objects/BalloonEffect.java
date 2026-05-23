package com.chronicorn.frontend.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class BalloonEffect {
    private PhysicsObjects target;
    private Animation<TextureRegion> animation;
    private float stateTime;

    public BalloonEffect(PhysicsObjects target, Animation<TextureRegion> animation) {
        this.target = target;
        this.animation = animation;
        this.stateTime = 0f;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public boolean isFinished() {
        return animation.isAnimationFinished(stateTime);
    }

    public PhysicsObjects getTarget() {
        return target;
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame = animation.getKeyFrame(stateTime, false);
        if (frame != null) {
            Vector2 pos = target.getPosition();
            // Both Player and MapEvent are visually 64px tall.
            // Center the 48x48 balloon horizontally over the 48x64 character.
            // Since the character visual width is 48px, centering means drawing exactly at pos.x.
            batch.draw(frame, pos.x, pos.y + 64f, 48f, 48f);
        }
    }
}
