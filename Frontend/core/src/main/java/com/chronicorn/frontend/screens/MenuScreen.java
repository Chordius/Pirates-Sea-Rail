package com.chronicorn.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.items.Equippable;
import com.chronicorn.frontend.items.Item;
import com.chronicorn.frontend.items.ItemDatabase;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.screens.components.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.battlers.ActorFactory;
import com.chronicorn.frontend.managers.assetManager.ImageManager;

public class MenuScreen implements Screen {
    private Stage stage;
    private Image backgroundImage;
    private Player player;

    private Stack rootStack;

    private Table topNavTable;
    private Table leftRosterTable;
    private Table partySelectionTable;
    private Table rightDetailsTable;
    private Table gachaTable;
    private Table settingsTable;
    private Table contentContainer;
    private Table equipMenuTable;
    private Table itemsTable;
    private Actor currentSelectedActor;

    // Gacha aftermath overlay fields
    private Table gachaResultsOverlay;
    private java.util.List<Table> gachaResultsCards;
    private boolean gachaResultsAnimationDone;
    private Label gachaResultsPromptLabel;

    public MenuScreen(Image bgImage, Player player) {
        this.backgroundImage = bgImage;
        this.player = player;
        this.stage = new Stage(new ScreenViewport());

        if (this.backgroundImage != null) {
            this.backgroundImage.setFillParent(true);
            stage.addActor(this.backgroundImage);
        }

        buildUI();
    }

    private void buildUI() {
        rootStack = new Stack();

        Table contentLayer = new Table();
        contentLayer.top().left();

        topNavTable = new TopNavTable(new TopNavTable.TabSelectionListener() {
            @Override
            public void onTabSelected(String tabName) {
                switchMenuContent(tabName);
            }
        });

        contentContainer = new Table();

        contentLayer.add(contentContainer).expand().fill().padTop(70);

        Table navLayer = new Table();
        navLayer.top().left();
        navLayer.add(topNavTable).expandX().fillX();

        navLayer.setTouchable(Touchable.childrenOnly);
        topNavTable.setTouchable(Touchable.childrenOnly);

        rootStack.add(contentLayer);
        rootStack.add(navLayer);

        rootStack.setFillParent(true);
        stage.addActor(rootStack);

        switchMenuContent("Crew");
    }

    private void switchMenuContent(String tabName) {
        contentContainer.clearChildren();

        if (tabName.equals("Crew")) {
            Stack masterCrewStack = new Stack();

            rightDetailsTable = new RightDetailsTable();
            equipMenuTable = new EquipMenuTable();
            equipMenuTable.setVisible(false); // Hide it initially

            // Equip Listener Transition
            ((RightDetailsTable) rightDetailsTable).setEquipListener(new RightDetailsTable.EquipWeaponsListener() {
                @Override
                public void onEquipWeaponsClicked() {
                    if (leftRosterTable != null && leftRosterTable.isVisible()) {

                        // 1. Fade out the base crew layout
                        leftRosterTable.clearActions();
                        leftRosterTable.addAction(Actions.sequence(
                                Actions.fadeOut(0.2f, Interpolation.fade),
                                Actions.visible(false)));

                        rightDetailsTable.clearActions();
                        rightDetailsTable.addAction(Actions.sequence(
                                Actions.fadeOut(0.2f, Interpolation.fade),
                                Actions.visible(false)));

                        // 2. Prep and Animate the Equip Menu
                        ((EquipMenuTable) equipMenuTable).updateMenu(currentSelectedActor,
                                GameSession.getInstance().inventory);
                        equipMenuTable.setVisible(true);
                        ((EquipMenuTable) equipMenuTable).playEntranceAnimation();
                        ((EquipMenuTable) equipMenuTable).setListener(new EquipMenuTable.EquipInteractionListener() {
                            @Override
                            public void onConfirmClicked() {
                                if (currentSelectedActor != null) {
                                    Equippable[] selected = ((EquipMenuTable) equipMenuTable)
                                            .getEquippedWeapons();

                                    // Remove selected weapons from any other character's equipment list
                                    if (GameSession.getInstance().getParty() != null && GameSession.getInstance().getParty().getOwnedCharacters() != null) {
                                        for (java.util.Map.Entry<String, Actor> entry : GameSession.getInstance().getParty().getOwnedCharacters().entrySet()) {
                                            Actor other = entry.getValue();
                                            if (other != currentSelectedActor) {
                                                boolean changed = false;
                                                if (selected[0] != null && other.getEquipments().removeValue(selected[0], true)) {
                                                    changed = true;
                                                }
                                                if (selected[1] != null && other.getEquipments().removeValue(selected[1], true)) {
                                                    changed = true;
                                                }
                                                if (changed) {
                                                    other.calculateParams(other.getLevel());
                                                }
                                            }
                                        }
                                    }

                                    currentSelectedActor.getEquipments().clear();
                                    if (selected[0] != null)
                                        currentSelectedActor.getEquipments().add(selected[0]);
                                    if (selected[1] != null)
                                        currentSelectedActor.getEquipments().add(selected[1]);
                                    currentSelectedActor.calculateParams(currentSelectedActor.getLevel());
                                }

                                // Fade out Equip Menu
                                equipMenuTable.clearActions();
                                equipMenuTable.addAction(Actions.sequence(
                                        Actions.fadeOut(0.2f, Interpolation.fade),
                                        Actions.visible(false)));

                                // Bring back base Crew Menu and update details
                                leftRosterTable.setVisible(true);
                                rightDetailsTable.setVisible(true);

                                leftRosterTable.getColor().a = 1f;
                                rightDetailsTable.getColor().a = 1f;

                                ((LeftRosterTable) leftRosterTable).refreshRoster();
                                if (currentSelectedActor != null) {
                                    ((RightDetailsTable) rightDetailsTable).updateDetails(currentSelectedActor);
                                }
                                ((RightDetailsTable) rightDetailsTable).playEntranceAnimation();
                            }

                            @Override
                            public void onCloseClicked() {
                                // 1. Fade out Equip Menu
                                equipMenuTable.clearActions();
                                equipMenuTable.addAction(Actions.sequence(
                                        Actions.fadeOut(0.2f, Interpolation.fade),
                                        Actions.visible(false)));

                                // 2. Bring back base Crew Menu
                                leftRosterTable.setVisible(true);
                                rightDetailsTable.setVisible(true);

                                // Reset alpha before playing entrance animations
                                leftRosterTable.getColor().a = 1f;
                                rightDetailsTable.getColor().a = 1f;

                                // Trigger the sliding entrances again
                                ((LeftRosterTable) leftRosterTable).refreshRoster();
                                ((RightDetailsTable) rightDetailsTable).playEntranceAnimation();
                            }
                        });
                    }
                }
            });

            leftRosterTable = new LeftRosterTable(new LeftRosterTable.CharacterSelectionListener() {
                @Override
                public void onCharacterSelected(Actor selectedActor) {
                    // Cache the selection so the equip menu knows who to load!
                    currentSelectedActor = selectedActor;
                    ((RightDetailsTable) rightDetailsTable).updateDetails(selectedActor);
                }

                @Override
                public void onPartyTriggerClicked() {
                    boolean isCurrentlyVisible = partySelectionTable.isVisible();

                    if (!isCurrentlyVisible) {
                        // 1. Opening Party Screen
                        ((PartySelectionTable) partySelectionTable).refreshToCurrentParty();

                        // Fade out details
                        rightDetailsTable.clearActions();
                        rightDetailsTable.addAction(Actions.sequence(
                                Actions.fadeOut(0.2f, Interpolation.fade),
                                Actions.visible(false)));

                        // Prep and animate in Party Grid
                        partySelectionTable.setVisible(true);
                        ((PartySelectionTable) partySelectionTable).playEntranceAnimation();

                    } else {
                        // 2. Closing Party Screen (via trigger)
                        partySelectionTable.clearActions();
                        partySelectionTable.addAction(Actions.sequence(
                                Actions.fadeOut(0.2f, Interpolation.fade),
                                Actions.visible(false)));

                        // Bring back Details
                        rightDetailsTable.setVisible(true);
                        rightDetailsTable.getColor().a = 1f; // Must reset master alpha after a fadeOut!
                        ((RightDetailsTable) rightDetailsTable).playEntranceAnimation();
                    }
                }
            });

            // Call it initially upon loading the menu
            ((RightDetailsTable) rightDetailsTable).playEntranceAnimation();

            partySelectionTable = new PartySelectionTable(new PartySelectionTable.PartyConfirmListener() {
                @Override
                public void onPartyConfirmed() {
                    // Fade out Party Screen
                    partySelectionTable.clearActions();
                    partySelectionTable.addAction(Actions.sequence(
                            Actions.fadeOut(0.2f, Interpolation.fade),
                            Actions.visible(false)));

                    ((LeftRosterTable) leftRosterTable).refreshRoster();

                    // Bring back Details
                    rightDetailsTable.setVisible(true);
                    rightDetailsTable.getColor().a = 1f; // Must reset master alpha after a fadeOut!
                    ((RightDetailsTable) rightDetailsTable).playEntranceAnimation();
                }
            });
            partySelectionTable.setVisible(false);

            Stack rightSideStack = new Stack();
            rightSideStack.add(rightDetailsTable);
            rightSideStack.add(partySelectionTable);

            Table crewLayout = new Table();
            crewLayout.add(leftRosterTable).width(350).fillY().expandY();
            crewLayout.add(rightSideStack).expand().fill().padTop(70);

            masterCrewStack.add(crewLayout);
            masterCrewStack.add(equipMenuTable);

            contentContainer.add(masterCrewStack).fill().expand();
        } else if (tabName.equals("Gacha")) {

            if (gachaTable == null) {
                gachaTable = new GachaTable();
            }

            // FIXED: Only add to container if it isn't already a child.
            // This completely mitigates layout breakage from spam clicking!
            if (!contentContainer.getChildren().contains(gachaTable, true)) {
                contentContainer.add(gachaTable).fill().expand();
            }

            // Fire the slam-in animations safely!
            ((GachaTable) gachaTable).playEntranceAnimation();
        } else if (tabName.equals("Settings")) {
            if (settingsTable == null) {
                settingsTable = new SettingsTable();
            }
            if (!contentContainer.getChildren().contains(settingsTable, true)) {
                contentContainer.add(settingsTable).fill().expand();
            }
            ((SettingsTable) settingsTable).playEntranceAnimation();
        } else if (tabName.equals("Items")) {
            if (itemsTable == null) {
                itemsTable = new ItemsTable();
            }
            if (!contentContainer.getChildren().contains(itemsTable, true)) {
                contentContainer.add(itemsTable).fill().expand();
            }
            ((ItemsTable) itemsTable).refreshGrid();
            ((ItemsTable) itemsTable).refreshLeftRoster();
            ((ItemsTable) itemsTable).refreshDetails();
            ((ItemsTable) itemsTable).playEntranceAnimation();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gachaResultsOverlay != null && gachaResultsOverlay.getParent() != null) {
                handleGachaOverlayInteraction();
            } else {
                SceneManager.getInstance().goBack();
            }
            return;
        }

        stage.act(delta);
        stage.draw();
    }

    public void showGachaResults(String[] pulledIds) {
        if (rootStack == null) return;

        gachaResultsCards = new java.util.ArrayList<>();
        gachaResultsAnimationDone = false;

        gachaResultsOverlay = new Table();
        gachaResultsOverlay.setFillParent(true);
        gachaResultsOverlay.setTouchable(Touchable.enabled);
        gachaResultsOverlay.setBackground(ImageManager.skin.newDrawable("white-pixel", new Color(0, 0, 0, 0.75f)));

        Table gridContainer = new Table();
        gridContainer.center();

        int columns = pulledIds.length > 5 ? 5 : 1;
        int currentCol = 0;

        for (String id : pulledIds) {
            Stack cardStack = new Stack();

            String nameText;
            Drawable iconDrawable;

            if (id.startsWith("W")) {
                Item item = ItemDatabase.getItem(id);
                nameText = item != null ? item.getName() : id;
                iconDrawable = item != null ? ImageManager.skin.getDrawable(item.getIcon()) : ImageManager.skin.getDrawable("white-pixel");
            } else {
                Actor actor = ActorFactory.createActor(id);
                nameText = actor.getName();
                iconDrawable = ImageManager.skin.getDrawable("face_" + actor.getName().toLowerCase());
            }

            Image faceImg = new Image(iconDrawable);
            faceImg.setScaling(Scaling.fit);

            Table faceBg = new Table();
            faceBg.setBackground(ImageManager.skin.newDrawable("white-pixel", Color.valueOf("686259")));
            faceBg.add(faceImg).expand().fill();

            Table nameBanner = new Table();
            nameBanner.bottom();

            Table bannerBg = new Table();
            bannerBg.setBackground(ImageManager.skin.newDrawable("dark-pixel", new Color(0, 0, 0, 0.8f)));
            Label nameLabel = new Label(nameText, ImageManager.skin, "default");
            nameLabel.setFontScale(0.8f);
            nameLabel.setAlignment(Align.center);
            bannerBg.add(nameLabel).pad(2, 5, 2, 5).expandX().fillX();

            nameBanner.add(bannerBg).expandX().fillX();

            cardStack.add(faceBg);
            cardStack.add(nameBanner);

            Table cardWrapper = new Table();
            cardWrapper.add(cardStack).size(120, 160);
            cardWrapper.setOrigin(Align.center);
            cardWrapper.setTransform(true);
            cardWrapper.getColor().a = 0f;

            gridContainer.add(cardWrapper).pad(10);
            gachaResultsCards.add(cardWrapper);

            currentCol++;
            if (currentCol >= columns) {
                gridContainer.row();
                currentCol = 0;
            }
        }

        gachaResultsPromptLabel = new Label("Click to skip", ImageManager.skin, "default");
        gachaResultsPromptLabel.setFontScale(0.9f);
        gachaResultsPromptLabel.getColor().a = 0.5f;

        gachaResultsOverlay.add(gridContainer).padBottom(40).row();
        gachaResultsOverlay.add(gachaResultsPromptLabel).center();

        float delayPerCard = 0.4f;
        float fadeDuration = 0.25f;

        for (int i = 0; i < gachaResultsCards.size(); i++) {
            Table card = gachaResultsCards.get(i);
            card.getColor().a = 0f;
            card.addAction(Actions.sequence(
                Actions.delay(i * delayPerCard),
                Actions.fadeIn(fadeDuration)
            ));
        }

        float totalAnimTime = gachaResultsCards.size() * delayPerCard + fadeDuration;
        gachaResultsOverlay.addAction(Actions.sequence(
            Actions.delay(totalAnimTime),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    gachaResultsAnimationDone = true;
                    gachaResultsPromptLabel.setText("Click anywhere to close");
                }
            })
        ));

        gachaResultsOverlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleGachaOverlayInteraction();
            }
        });

        rootStack.add(gachaResultsOverlay);
    }

    private void handleGachaOverlayInteraction() {
        if (!gachaResultsAnimationDone) {
            gachaResultsOverlay.clearActions();
            for (Table card : gachaResultsCards) {
                card.clearActions();
                card.getColor().a = 1f;
            }
            gachaResultsAnimationDone = true;
            gachaResultsPromptLabel.setText("Click anywhere to close");
        } else {
            gachaResultsOverlay.remove();
            gachaResultsOverlay = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
