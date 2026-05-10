package com.chronicorn.frontend.managers.battleManager.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

public class FloatingPopup extends Group {

    public FloatingPopup(String text, Skin skin, String stylename, Color color, float startX, float startY) {
        // 1. Create the text
        Label label = new Label(text, skin, stylename);
        label.setColor(color);
        label.setAlignment(Align.center);

        // 2. Wrap it in the Group for safe transformations
        this.addActor(label);
        this.setSize(label.getWidth(), label.getHeight());
        this.setOrigin(Align.center);
        this.setPosition(startX, startY);

        // 3. Determine if the text is a pure number (supports optional negative sign)
        boolean isNumber = text.matches("-?\\d+");

        if (isNumber) {
            // --- INTEGER ANIMATION: "The Slam" (Big to Normal) ---
            this.setScale(3.5f); // Start massive

            this.addAction(Actions.sequence(
                // Pop out and slam down quickly
                Actions.parallel(
                    Actions.scaleTo(1f, 1f, 0.15f, Interpolation.pow2Out),
                    Actions.moveBy(0, 40f, 0.15f, Interpolation.pow2Out)
                ),
                // Drift up slowly and fade out
                Actions.parallel(
                    Actions.moveBy(0, 60f, 0.7f, Interpolation.sineOut),
                    Actions.fadeOut(0.7f)
                ),
                Actions.removeActor()
            ));

        } else {
            // --- TEXT ANIMATION: "The Emerge" (Small to Big) ---
            this.setScale(0.5f); // Start small

            this.addAction(Actions.sequence(
                // Expand outward quickly and overshoot normal size slightly (1.2x)
                Actions.parallel(
                    Actions.scaleTo(1.2f, 1.2f, 0.15f, Interpolation.pow2Out),
                    Actions.moveBy(0, 40f, 0.15f, Interpolation.pow2Out)
                ),
                // Settle back to 1.0x while drifting and fading
                Actions.parallel(
                    Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.sine),
                    Actions.moveBy(0, 60f, 0.7f, Interpolation.sineOut),
                    Actions.fadeOut(0.7f)
                ),
                Actions.removeActor()
            ));
        }
    }
}
