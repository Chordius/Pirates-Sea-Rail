package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.eventManagers.GameMessage;

public class WindowMessage extends WindowBase {
    private Label textLabel;
    private int alignment;

    // Typewriter Logic
    private String fullText = "";
    private float typeTimer = 0;
    private int charIndex = 0;
    private boolean isTyping = false;
    private final float TYPE_SPEED = 0.03f;

    // Conversation Logic
    private Array<String> messageQueue;
    private Runnable onFinishCallback; // Code to run when conversation ends

    public WindowMessage() {
        // Create window at bottom of screen, full width, height 180
        super("", 0, 0, Gdx.graphics.getWidth(), 180);

        this.messageQueue = new Array<>();
        this.alignment = Align.topLeft;

        // Hide by default
        this.setVisible(false);
    }

    @Override
    public void createContents() {
        // 1. Create the Label
        textLabel = drawText("", alignment);
        textLabel.setWrap(true);

        textLabel.getStyle().font.getData().setLineHeight(standardPadding + 2 * textPadding);

        this.getCell(textLabel)
            .clearActor()        // Only needed if we were replacing the actor, safe to skip here usually
            .setActor(textLabel) // Re-set actor
            .expand()            // "Take up all empty space in the window"
            .fill()              // "Stretch the label to touch the edges"
            .top()
            .maxHeight(999);
    }

    public void startConversation(Array<String> texts, Runnable onFinish) {
        this.messageQueue.clear();
        this.messageQueue.addAll(texts);
        this.onFinishCallback = onFinish;

        this.open();
        nextMessage();
    }

    public void showMessage(String text) {
        this.messageQueue.clear();
        this.messageQueue.add(text);
        this.onFinishCallback = null;

        this.open();
        nextMessage();
    }

    private void nextMessage() {
        if (messageQueue.size == 0) {
            closeConversation();
            return;
        }

        // Pop the next string
        fullText = messageQueue.removeIndex(0);

        // Reset Typewriter
        textLabel.setText("");
        charIndex = 0;
        typeTimer = 0;
        isTyping = true;
    }

    private void closeConversation() {
        this.setVisible(false);

        GameMessage.getInstance().finish();
    }

    public void setPosition(float x, float y) {
        this.setX(x);
        this.setY(y);
    };

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!this.isVisible() && GameMessage.getInstance().hasText()) {
            if (GameMessage.getInstance().getPosition()) {
                setPosition(GameMessage.getInstance().getX(), GameMessage.getInstance().getY());
            }
            if (GameMessage.getInstance().getAlignment() != this.alignment) {
                this.alignment = GameMessage.getInstance().getAlignment();
            }
            String newText = GameMessage.getInstance().popText();
            this.showMessage(newText); // Open window and start typing
        }

        // 1. Handle Input (Z key or Enter to advance)
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (isTyping) {
                // If typing, SKIP to the end
                charIndex = fullText.length();
                textLabel.setText(fullText);
                isTyping = false;
            } else {
                // If done typing, Go to NEXT message
                nextMessage();
            }
        }

        // 2. Handle Typewriter Effect
        if (isTyping) {
            typeTimer += delta;
            if (typeTimer >= TYPE_SPEED) {
                typeTimer = 0;
                charIndex++;

                // Append text safely
                textLabel.setText(fullText.substring(0, charIndex));

                // Check if done
                if (charIndex >= fullText.length()) {
                    isTyping = false;
                }
            }
        }
    }
}
