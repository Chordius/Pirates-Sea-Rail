package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.eventManagers.GameMessage;

public class WindowMessage extends WindowBase {
    private Label textLabel;
    private int alignment;
    private static final int DEFAULT_ALIGNMENT = Align.topLeft;

    // Speaker
    private Table speakerBox;
    private Label speakerLabel;

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
        // Create window centered on screen, size 800x180
        super("", (Gdx.graphics.getWidth() - 1000) / 2, 36, 1000, 168);
        setBackgroundDrawable("window-speak-drawable");

        this.messageQueue = new Array<>();
        this.alignment = DEFAULT_ALIGNMENT;

        this.setClip(false);
        this.setVisible(false);
    }

    @Override
    public void createContents() {
        // 1. Create the Label
        textLabel = drawText("", DEFAULT_ALIGNMENT);
        textLabel.setWrap(true);

        textLabel.getStyle().font.getData().setLineHeight(standardPadding + 2 * textPadding);

        this.getCell(textLabel)
            .clearActor()        // Only needed if we were replacing the actor, safe to skip here usually
            .setActor(textLabel) // Re-set actor
            .expand()            // "Take up all empty space in the window"
            .fill()              // "Stretch the label to touch the edges"
            .top()
            .padTop(10)
            .padBottom(10)
            .padLeft(180)        // <-- (Optional but recommended) Keeps text out of the left fade
            .padRight(180)       // <-- (Optional but recommended) Keeps text out of the right fade
            .maxHeight(999);

        // 2. Create the Speaker Box
        speakerBox = new Table();
        speakerBox.setBackground(ImageManager.skin.getDrawable("speaker-box-drawable"));

        speakerLabel = new Label("", ImageManager.skin);
        speakerLabel.setAlignment(Align.center);

        // Add padding to push the text inward so it doesn't overlap the diamond borders
        speakerBox.add(speakerLabel).padLeft(50).padRight(50);

        // Add as a floating actor to the Window (not in the layout grid)
        this.addActor(speakerBox);
        speakerBox.setVisible(false); // Hidden by default
    }

    public void startConversation(Array<String> texts, Runnable onFinish) {
        this.messageQueue.clear();
        this.messageQueue.addAll(texts);
        this.onFinishCallback = onFinish;

        this.open();
        nextMessage();
    }

    public void showMessage(String speaker, String text) {
        this.messageQueue.clear();
        this.messageQueue.add(text);
        this.onFinishCallback = null;

        if (speaker != null && !speaker.isEmpty()) {
            speakerLabel.setText(speaker);
            speakerBox.pack(); // Automatically resize the background to fit the name

            // Calculate Top-Center position relative to the main window
            float xPos = (this.getWidth() - speakerBox.getWidth()) / 2f;
            float yPos = this.getHeight() - (speakerBox.getHeight() / 2f); // Floats halfway out the top

            speakerBox.setPosition(xPos, yPos);
            speakerBox.setVisible(true);

            this.getCell(textLabel).padTop(18);
        } else {
            speakerBox.setVisible(false);
            this.getCell(textLabel).padTop(10);
        }

        this.invalidate();

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
            int messageAlignment = GameMessage.getInstance().getAlignment();
            if (messageAlignment != this.alignment) {
                this.alignment = messageAlignment;
                if (textLabel != null) {
                    textLabel.setAlignment(this.alignment);
                }
            }
            String newSpeaker = GameMessage.getInstance().popSpeaker();
            String newText = GameMessage.getInstance().popText();

            // Pass both to the window
            this.showMessage(newSpeaker, newText);
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
