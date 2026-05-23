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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.screens.components.*;

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
    private Table contentContainer;
    private Table equipMenuTable;
    private Actor currentSelectedActor;

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
        Stack rootStack = new Stack();

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
                                    com.chronicorn.frontend.items.Equippable[] selected = ((EquipMenuTable) equipMenuTable)
                                            .getEquippedWeapons();
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
        } else if (tabName.equals("Inventory")) {
            // E.g., contentContainer.add(new InventoryTable()).fill().expand();
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
            SceneManager.getInstance().goBack();
            return;
        }

        stage.act(delta);
        stage.draw();
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
