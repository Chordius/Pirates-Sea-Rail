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

    private float defaultX;
    private float defaultY;

    // Speaker
    private Table speakerBox;
    private Label speakerLabel;

    // Typewriter Logic
    private String fullText = "";
    private float typeTimer = 0;
    private int charIndex = 0;
    private boolean isTyping = false;
    private final float TYPE_SPEED = 0.03f;

    public static class TextElement {
        public enum Type { CHAR, PAUSE }
        public Type type;
        public char character;
        public String color;

        public TextElement(Type type, char character, String color) {
            this.type = type;
            this.character = character;
            this.color = color;
        }
    }

    private Array<TextElement> elements = new Array<>();
    private int elementIndex = 0;
    private boolean isPaused = false;

    // Conversation Logic
    private Array<String> messageQueue;
    private Runnable onFinishCallback; // Code to run when conversation ends

    public WindowMessage() {
        // Create window centered on screen, size 800x180
        super("", (Gdx.graphics.getWidth() - 1000) / 2, 36, 1000, 168);
        setBackgroundDrawable("window-speak-drawable");

        this.defaultX = (Gdx.graphics.getWidth() - 1000) / 2f;
        this.defaultY = 36f;

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
        textLabel.getStyle().font.getData().markupEnabled = true;

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

        // Parse escape codes
        parseFullText(fullText);

        // Reset Typewriter
        textLabel.setText("");
        elementIndex = 0;
        typeTimer = 0;
        isTyping = true;
        isPaused = false;
    }

    private void closeConversation() {
        this.setVisible(false);

        GameMessage.getInstance().finish();
        if (onFinishCallback != null) {
            onFinishCallback.run();
            onFinishCallback = null;
        }
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
            } else {
                setPosition(defaultX, defaultY);
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
            boolean showBg = GameMessage.getInstance().popShowBackground();
            if (showBg) {
                setBackgroundDrawable("window-speak-drawable");
            } else {
                this.setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
            }

            // Pass both to the window
            this.showMessage(newSpeaker, newText);
        }

        // 1. Handle Input (J key or Enter to advance)
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (isPaused) {
                isPaused = false;
                elementIndex++;
                typeTimer = 0;
            } else if (isTyping) {
                // Skip to next pause or the end
                while (elementIndex < elements.size) {
                    TextElement el = elements.get(elementIndex);
                    if (el.type == TextElement.Type.PAUSE) {
                        isPaused = true;
                        break;
                    }
                    elementIndex++;
                }
                textLabel.setText(buildMarkupString(elementIndex));
                if (elementIndex >= elements.size) {
                    isTyping = false;
                }
            } else {
                // If done typing, Go to NEXT message
                nextMessage();
            }
        }

        // 2. Handle Typewriter Effect
        if (isTyping && !isPaused) {
            typeTimer += delta;
            while (typeTimer >= TYPE_SPEED) {
                typeTimer -= TYPE_SPEED;

                if (elementIndex >= elements.size) {
                    isTyping = false;
                    break;
                }

                TextElement el = elements.get(elementIndex);
                if (el.type == TextElement.Type.PAUSE) {
                    isPaused = true;
                    break;
                } else {
                    elementIndex++;
                    textLabel.setText(buildMarkupString(elementIndex));
                }
            }
        }
    }

    private void parseFullText(String text) {
        elements.clear();
        if (text == null) return;

        String activeColor = null;
        int i = 0;
        int len = text.length();
        while (i < len) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = text.charAt(i + 1);
                if (next == '!') {
                    elements.add(new TextElement(TextElement.Type.PAUSE, '\0', activeColor));
                    i += 2;
                } else if (next == 'c' || next == 'C') {
                    if (i + 2 < len && text.charAt(i + 2) == '[') {
                        int closeBracket = text.indexOf(']', i + 3);
                        if (closeBracket != -1) {
                            String colorVal = text.substring(i + 3, closeBracket).trim();
                            if (colorVal.isEmpty() || colorVal.equalsIgnoreCase("default") || colorVal.equalsIgnoreCase("normal")) {
                                activeColor = null;
                            } else {
                                // Normalize hex values (e.g. ff0000 -> #ff0000)
                                if (colorVal.matches("^[0-9a-fA-F]{6,8}$")) {
                                    activeColor = "#" + colorVal;
                                } else {
                                    activeColor = colorVal;
                                }
                            }
                            i = closeBracket + 1;
                        } else {
                            elements.add(new TextElement(TextElement.Type.CHAR, '\\', activeColor));
                            i++;
                        }
                    } else {
                        elements.add(new TextElement(TextElement.Type.CHAR, '\\', activeColor));
                        i++;
                    }
                } else if (next == 'n') {
                    elements.add(new TextElement(TextElement.Type.CHAR, '\n', activeColor));
                    i += 2;
                } else {
                    elements.add(new TextElement(TextElement.Type.CHAR, next, activeColor));
                    i += 2;
                }
            } else {
                elements.add(new TextElement(TextElement.Type.CHAR, c, activeColor));
                i++;
            }
        }
    }

    private String buildMarkupString(int upToLimit) {
        StringBuilder sb = new StringBuilder();
        String activeColor = null;
        for (int i = 0; i < upToLimit; i++) {
            TextElement el = elements.get(i);
            if (el.type == TextElement.Type.CHAR) {
                if (el.color != null && !el.color.equals(activeColor)) {
                    if (activeColor != null) {
                        sb.append("[]");
                    }
                    activeColor = el.color;
                    sb.append("[").append(activeColor).append("]");
                } else if (el.color == null && activeColor != null) {
                    sb.append("[]");
                    activeColor = null;
                }
                sb.append(el.character);
            }
        }
        if (activeColor != null) {
            sb.append("[]");
        }
        return sb.toString();
    }
}
