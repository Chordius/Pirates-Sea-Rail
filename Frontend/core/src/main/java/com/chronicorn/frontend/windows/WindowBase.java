package com.chronicorn.frontend.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.chronicorn.frontend.managers.assetManager.ImageManager;

public class WindowBase extends Window {
    private boolean isOpening;
    private boolean isClosing;

    // Constants
    protected int lineHeight = 36;
    protected int standardFontSize = 20;
    protected int standardPadding = 18;
    protected int textPadding = 9;
    protected int standardBackOpacity = 192;

    public WindowBase(String title, int x, int y, int width, int height) {
        super(title, ImageManager.skin);

        if (ImageManager.skin == null) {
            throw new IllegalStateException("GameSkin.load() must be called before creating windows!");
        }

        this.setModal(true);
        this.setMovable(false);
        this.setResizable(false);

        move(x, y, width, height);
        updatePadding();
        createContents();

        // Default windows use the standard window drawable
        Color bgColor = new Color(1, 1, 1, standardBackOpacity / 255f);
        this.setBackground(getSkin().newDrawable("window-drawable", bgColor));
    };

    public void setBackgroundDrawable(String drawableName) {
        Drawable drawable = getSkin().getDrawable(drawableName);
        this.setBackground(drawable);
    }

    public void move(int x, int y, int width, int height) {
        this.setPosition(x, y);
        this.setSize(width, height);
    }

    public void updatePadding() {
        this.pad(standardPadding);
    }

    public void createContents() {
        // TODO: Override In Child Window
        this.add("Ready").row();
    }

    public void open() {
        this.setVisible(true);
        this.isOpening = true;
        // You could add an Action here for animation
        this.getColor().a = 0f;
        this.addAction(Actions.fadeIn(0.5f));
    }

    public Label drawText(String text, int align) {
        Label label = new Label(text, getSkin());
        label.setAlignment(align);

        this.add(label).height(lineHeight).pad(textPadding);
        return label;
    }

    public void drawText(String text, Color color) {
        Label label = new Label(text, getSkin());
        label.setColor(color);
        this.add(label).height(lineHeight).pad(textPadding);
    }

    public void drawGauge(float current, float max, Color color) {
        ProgressBar bar = new ProgressBar(0, max, 1, false, getSkin());
        bar.setValue(current);
        bar.setColor(color);

        this.add(bar).height(lineHeight).fillX().pad(textPadding);
    }

    public void alignTitle(int alignment) {
        this.getTitleLabel().setAlignment(alignment);
    }

    public void alignContents(int alignment) {
        this.defaults().align(alignment);
    }

    public void newLine() {
        this.row();
    }
}
