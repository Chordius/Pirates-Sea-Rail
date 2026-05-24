package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;

import java.util.HashMap;
import java.util.Map;

public class PartySelectionTable extends Table {
    private Actor[] activeSlots = new Actor[4];
    private Map<String, Table> badgeMap = new HashMap<>();

    // Promoted to class scope for the animation method
    private Table gridContainer;
    private TextButton confirmButton;

    public interface PartyConfirmListener {
        void onPartyConfirmed();
    }

    private PartyConfirmListener listener;

    public PartySelectionTable(PartyConfirmListener listener) {
        super();
        this.listener = listener;
        initActiveSlots();
        buildUI();
    }

    public void initActiveSlots() {
        for (int i = 0; i < 4; i++) {
            activeSlots[i] = null;
        }

        java.util.List<Actor> currentParty = GameSession.getInstance().getParty().getActivePartyActors();
        for (int i = 0; i < Math.min(4, currentParty.size()); i++) {
            activeSlots[i] = currentParty.get(i);
        }
    }

    public void refreshToCurrentParty() {
        initActiveSlots();
        updateAllBadges();
    }

    private void buildUI() {
        gridContainer = new Table();
        gridContainer.top().left();

        Map<String, Actor> ownedCharacters = GameSession.getInstance().getParty().getOwnedCharacters();

        int columns = 5;
        int currentCol = 0;

        for (Map.Entry<String, Actor> entry : ownedCharacters.entrySet()) {
            final Actor actor = entry.getValue();

            Stack cardStack = new Stack();

            Image faceImg = new Image(ImageManager.skin.getDrawable("face_" + actor.getName().toLowerCase()));
            faceImg.setScaling(com.badlogic.gdx.utils.Scaling.fit);

            Table faceBg = new Table();
            faceBg.setBackground(ImageManager.skin.newDrawable("white-pixel", Color.valueOf("686259")));
            faceBg.add(faceImg).expand().fill();

            Table nameBanner = new Table();
            nameBanner.bottom();

            Table bannerBg = new Table();
            bannerBg.setBackground(ImageManager.skin.newDrawable("dark-pixel", new Color(0, 0, 0, 0.8f)));
            Label nameLabel = new Label(actor.getName() + " Lv." + actor.getLevel(), ImageManager.skin, "default");
            nameLabel.setFontScale(0.8f);
            bannerBg.add(nameLabel).pad(2, 5, 2, 5);

            nameBanner.add(bannerBg).expandX().fillX();

            Table badgeContainer = new Table();
            badgeContainer.top().right();

            Table badgeBg = new Table();
            badgeBg.setBackground(ImageManager.skin.newDrawable("dark-pixel", new Color(0.1f, 0.1f, 0.1f, 0.9f)));
            Label badgeLabel = new Label("", ImageManager.skin, "number-style");
            badgeBg.add(badgeLabel).pad(4, 10, 4, 10);

            badgeContainer.add(badgeBg);
            badgeMap.put(actor.getId(), badgeContainer);

            Button.ButtonStyle emptyStyle = new Button.ButtonStyle();
            Button clickTarget = new Button(emptyStyle);
            clickTarget.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleCharacterClick(actor);
                }
            });

            cardStack.add(faceBg);
            cardStack.add(nameBanner);
            cardStack.add(badgeContainer);
            cardStack.add(clickTarget);

            gridContainer.add(cardStack).size(120, 160).pad(10);

            currentCol++;
            if (currentCol >= columns) {
                gridContainer.row();
                currentCol = 0;
            }
        }

        updateAllBadges();

        ScrollPane scrollPane = new ScrollPane(gridContainer, ImageManager.skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        Table layoutWrapper = new Table();
        layoutWrapper.add(scrollPane).expand().fill().pad(20).row();

        confirmButton = new TextButton("Confirm Party", ImageManager.skin, "boxed-button");

        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean hasAtLeastOneMember = false;
                for (int i = 0; i < 4; i++) {
                    if (activeSlots[i] != null) {
                        hasAtLeastOneMember = true;
                        break;
                    }
                }

                if (!hasAtLeastOneMember) {
                    confirmButton.setText("Party Cannot Be Empty!");
                    confirmButton.setColor(Color.SCARLET);

                    confirmButton.addAction(Actions.sequence(
                        Actions.delay(1.5f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                confirmButton.setText("Confirm Party");
                                confirmButton.setColor(Color.WHITE);
                            }
                        })
                    ));
                    return;
                }

                for (int i = 0; i < 4; i++) {
                    if (activeSlots[i] != null) {
                        GameSession.getInstance().getParty().setActivePartyMember(i, activeSlots[i].getId());
                    } else {
                        GameSession.getInstance().getParty().setActivePartyMember(i, null);
                    }
                }

                if (listener != null) listener.onPartyConfirmed();
            }
        });

        layoutWrapper.add(confirmButton).size(200, 60).padBottom(20);

        this.add(layoutWrapper).expand().fill();
    }

    private void handleCharacterClick(Actor clickedActor) {
        int existingIndex = -1;

        for (int i = 0; i < 4; i++) {
            if (activeSlots[i] != null && activeSlots[i].getId().equals(clickedActor.getId())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex != -1) {
            activeSlots[existingIndex] = null;
        } else {
            for (int i = 0; i < 4; i++) {
                if (activeSlots[i] == null) {
                    activeSlots[i] = clickedActor;
                    break;
                }
            }
        }

        updateAllBadges();
    }

    private void updateAllBadges() {
        for (Table badge : badgeMap.values()) {
            badge.setVisible(false);
        }

        for (int i = 0; i < 4; i++) {
            Actor activeActor = activeSlots[i];
            if (activeActor != null) {
                Table badgeContainer = badgeMap.get(activeActor.getId());
                if (badgeContainer != null) {
                    badgeContainer.setVisible(true);

                    Table badgeBg = (Table) badgeContainer.getChildren().get(0);
                    Label badgeLabel = (Label) badgeBg.getChildren().get(0);

                    badgeLabel.setText(String.valueOf(i + 1));
                }
            }
        }
    }

    // --- UPDATED: "Tavern Dealer" Entrance Animation ---
    public void playEntranceAnimation() {
        this.getColor().a = 1f;

        // Force the layout engine to calculate the final grid positions immediately
        gridContainer.validate();

        int index = 0;

        for (com.badlogic.gdx.scenes.scene2d.Actor child : gridContainer.getChildren()) {

            // 1. CRITICAL: Allow nested UI elements (Stack) to be scaled and rotated!
            if (child instanceof Group) {
                ((Group) child).setTransform(true);
            }

            // 2. Capture the card's final resting position in the grid
            float targetX = child.getX();
            float targetY = child.getY();

            // 3. Yank the card away to the "Dealer's Hand" (Top-left off-screen)
            child.setPosition(-100f, gridContainer.getHeight() + 100f);

            // 4. Set starting state: shrunk, invisible, and spun wildly
            child.getColor().a = 0f;
            child.setOrigin(Align.center);
            child.setScale(0.3f);
            child.setRotation(180f);

            // 5. Fling the cards sequentially!
            child.clearActions();
            child.addAction(Actions.sequence(
                Actions.delay(index * 0.03f),
                Actions.parallel(
                    Actions.fadeIn(0.15f),
                    Actions.moveTo(targetX, targetY, 0.3f, Interpolation.swingOut),
                    Actions.scaleTo(1f, 1f, 0.3f, Interpolation.pow3Out),
                    Actions.rotateTo(0f, 0.3f, Interpolation.swingOut)
                )
            ));
            index++;
        }

        // Wait for all cards to finish dealing before showing the Confirm button
        confirmButton.getColor().a = 0f;
        confirmButton.clearActions();
        confirmButton.addAction(Actions.sequence(
            Actions.delay(index * 0.06f),
            Actions.fadeIn(0.4f)
        ));
    }
}
