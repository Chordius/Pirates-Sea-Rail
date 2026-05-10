package com.chronicorn.frontend.windows;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public class WindowFlex extends WindowBase {
    private Drawable defaultBackground;

    public WindowFlex() {
        super("", 0, 0, 100, 100); // Default dummy values, will be resized later
        this.defaultBackground = this.getBackground();
        this.setVisible(false);
    }

    public void setTransparent(boolean isTransparent) {
        if (isTransparent) {
            this.setBackground((Drawable) null);
        } else {
            if (defaultBackground != null) {
                this.setBackground(defaultBackground);
            }
        }
    }

    public void resetContent() {
        this.clearChildren(); // Removes all labels/images
        this.pad(standardPadding); // Reset padding
        this.pack(); // Recalculate size
        setTransparent(false);
    }

    public void addImage(String internalPath) {
        Texture tex = new Texture(internalPath);
        Image img = new Image(tex);
        this.add(img).expand().fill().row();
    }

    /**
     * Adds static text (using your WindowBase helper).
     */
    public void addTextContent(String text) {
        drawText(text, Align.center);
        this.row();
    }

    // Override createContents to be empty, since we build it dynamically
    @Override
    public void createContents() { }
}
