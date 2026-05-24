package com.chronicorn.frontend.windows;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.managers.assetManager.ImageManager;

public class GabWindow extends WindowBase {

    public GabWindow() {
        super("", 0, 0, 100, 100);
        this.setModal(false);
        this.setMovable(false);
        this.setResizable(false);
        this.setVisible(false);

        // Background is @[Frontend/assets/menu/Label-DarkBrown.png], loaded in
        // ImageManager as "label-darkbrown-bg"
        Drawable bg = ImageManager.skin.getDrawable("label-darkbrown-bg");
        this.setBackground(bg);

        // Make the window somewhat transparent
        // this.getColor().a = 0.85f;
    }

    @Override
    public void createContents() {
        // empty by default
    }

    public void addTextContent(String text) {
        this.clearChildren();

        // Notation: "+x [Item_Name]"
        String display = text;
        if (text.startsWith("You obtained ")) {
            String temp = text.substring("You obtained ".length());
            if (temp.endsWith("!")) {
                temp = temp.substring(0, temp.length() - 1);
            }
            String[] parts = temp.split(" ", 2);
            if (parts.length == 2) {
                try {
                    int qty = Integer.parseInt(parts[0]);
                    display = "+" + qty + " " + parts[1];
                } catch (NumberFormatException e) {
                    display = "+ " + temp;
                }
            } else {
                display = "+ " + temp;
            }
        }

        Label label = new Label(display, ImageManager.skin, "default");
        label.setAlignment(Align.left);

        this.add(label).expand().fill().pad(5f, 45f, 5f, 15f);

        this.pack();

        float width = Math.max(360f, this.getWidth());
        float height = Math.max(55f, this.getHeight());
        this.setSize(width, height);
    }

    public void open(Stage stage) {
        this.setVisible(true);
        this.getColor().a = 0f;

        // FIXED: Set baseline Y to the bottom-left zone (e.g., 40px off the bottom
        // edge)
        float baseY = 40f;
        float targetY = baseY;

        // FIXED: Track the highest active window top-boundary to stack them upward
        // cleanly
        float maxY = -1;
        for (Actor actor : stage.getActors()) {
            if (actor instanceof GabWindow && actor != this && actor.isVisible()) {
                float topY = actor.getY() + actor.getHeight();
                if (topY > maxY) {
                    maxY = topY;
                }
            }
        }

        if (maxY != -1) {
            targetY = maxY + 8f; // Stacks 8px directly above the previous alert ribbon
        }

        // FIXED: Pushed X to -20f so the left ribbon tail cuts off cleanly past the
        // monitor edge
        this.setPosition(-20f, targetY);

        this.addAction(Actions.sequence(
                Actions.alpha(0f),
                Actions.fadeIn(0.25f),
                Actions.delay(2.5f),
                Actions.fadeOut(0.25f),
                Actions.removeActor()));
    }
}
