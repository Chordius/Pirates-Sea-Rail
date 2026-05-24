package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;

public class LeftRosterTable extends Table {
    private ButtonGroup<Button> rosterGroup;
    private CharacterSelectionListener listener;
    private Table listContainer;

    public interface CharacterSelectionListener {
        void onCharacterSelected(Actor selectedActor);
        void onPartyTriggerClicked();
    }

    public LeftRosterTable(CharacterSelectionListener listener) {
        super();
        this.listener = listener;
        buildUI();
    }

    private void buildUI() {
        this.left();

        final Table mainPanel = new Table();
        mainPanel.setBackground(ImageManager.skin.getDrawable("gradient-bg"));
        mainPanel.left();

        final ClickListener panelHoverTracker = new ClickListener();
        mainPanel.addListener(panelHoverTracker);

        final ClickListener buttonHoverTracker = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onPartyTriggerClicked();
                }
            }
        };

        Stack triggerStack = new Stack();

        final Image switchGraphic = new Image(ImageManager.skin.getDrawable("switch-text"));
        switchGraphic.setScaling(com.badlogic.gdx.utils.Scaling.none);
        switchGraphic.getColor().a = 0.0f;
        switchGraphic.setAlign(Align.left);

        Button.ButtonStyle triggerStyle = new Button.ButtonStyle();
        Button partyTriggerArea = new Button(triggerStyle);
        partyTriggerArea.addListener(buttonHoverTracker);

        triggerStack.add(switchGraphic);
        triggerStack.add(partyTriggerArea);

        listContainer = new Table();
        listContainer.top().left();
        listContainer.defaults().height(80).width(320).padBottom(10);

        rosterGroup = new ButtonGroup<>();
        rosterGroup.setMaxCheckCount(1);
        rosterGroup.setMinCheckCount(1);
        rosterGroup.setUncheckLast(true);

        refreshRoster();

        ScrollPane scrollPane = new ScrollPane(listContainer, ImageManager.skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        // --- DYNAMIC GAP ANIMATION ---
        // We capture the specific cell holding the trigger stack so we can modify its width live
        final Cell<Stack> triggerCell = mainPanel.add(triggerStack).width(15).expandY().fillY();
        mainPanel.add(scrollPane).width(320).expandY().fillY().padTop(33);

        // This action now handles BOTH the graphic opacity AND the physical layout gap
        mainPanel.addAction(new com.badlogic.gdx.scenes.scene2d.Action() {
            float currentWidth = 15f;

            @Override
            public boolean act(float delta) {
                float targetWidth = 15f;
                float targetAlpha = 0.0f;

                if (buttonHoverTracker.isOver()) {
                    targetAlpha = 1.0f;
                    targetWidth = 50f;
                } else if (panelHoverTracker.isOver()) {
                    targetAlpha = 0.3f;
                    targetWidth = 50f;
                }

                // 1. Interpolate visual opacity
                switchGraphic.getColor().a = MathUtils.lerp(switchGraphic.getColor().a, targetAlpha, delta * 12f);

                // 2. Interpolate physical gap width
                currentWidth = MathUtils.lerp(currentWidth, targetWidth, delta * 12f);

                // Only force a layout recalculation if the width is actively changing
                if (Math.abs(currentWidth - triggerCell.getMinWidth()) > 0.5f) {
                    triggerCell.width(currentWidth);
                    mainPanel.invalidate();
                }

                return false;
            }
        });

        this.add(mainPanel).height(420).left();
    }

    public void refreshRoster() {
        listContainer.clearChildren();
        rosterGroup.clear();

        java.util.List<Actor> activeParty = GameSession.getInstance().getParty().getActivePartyActors();

        if (activeParty.isEmpty()) {
            listContainer.add(new Label("Party is empty.", ImageManager.skin)).pad(20);
            return;
        }

        boolean isFirst = true;
        int itemIndex = 0; // Tracks position for staggered entrance animation

        for (final Actor actor : activeParty) {
            if (actor == null) continue;

            ImageManager.loadMenuCharacterAsset(actor.getName());

            Button.ButtonStyle emptyStyle = new Button.ButtonStyle();
            final Button charButton = new Button(emptyStyle);

            // ANIMATION: Set initial alpha to 0 for the entrance fade
            charButton.getColor().a = 0f;

            final Table contentTable = new Table();
            contentTable.left().pad(10);

            ProgressBar.ProgressBarStyle ultStyle = ImageManager.skin.get("ult-roster-bar-" + actor.getName().toLowerCase(), ProgressBar.ProgressBarStyle.class);
            float ringWidth = ultStyle.background.getMinWidth();
            float ringHeight = ultStyle.background.getMinHeight();

            ProgressBar.ProgressBarStyle hpStyle = ImageManager.skin.get("hp-roster-bar", ProgressBar.ProgressBarStyle.class);
            float hpWidth = hpStyle.background.getMinWidth();
            float hpHeight = hpStyle.background.getMinHeight();

            Stack portraitStack = new Stack();

            ProgressBar ultRing = new ProgressBar(0, actor.getMaxEnergy(), 1, true, ImageManager.skin, "ult-roster-bar-" + actor.getName().toLowerCase());
            ultRing.setValue(actor.getEnergy());
            ultRing.setAnimateDuration(0);

            Image faceImg = new Image(ImageManager.skin.getDrawable("face_" + actor.getName().toLowerCase()));
            faceImg.setScaling(com.badlogic.gdx.utils.Scaling.fit);

            Table faceWrapper = new Table();
            faceWrapper.add(faceImg).size(ringWidth - 14, ringHeight - 14);

            portraitStack.add(ultRing);
            portraitStack.add(faceWrapper);

            Table infoTable = new Table();
            infoTable.left();

            Label nameLabel = new Label(actor.getName(), ImageManager.skin, "menu2");

            ProgressBar hpBar = new ProgressBar(0, actor.getMaxHp(), 1, false, ImageManager.skin, "hp-roster-bar");
            hpBar.setValue(actor.getHp());
            hpBar.setAnimateDuration(0);

            infoTable.add(nameLabel).left().padBottom(5).row();
            infoTable.add(hpBar).size(hpWidth, hpHeight).left();

            contentTable.add(portraitStack).size(ringWidth, ringHeight).padRight(5);
            contentTable.add(infoTable).expandX().fillX();

            // ANIMATION: Smooth layout sliding and alpha adjustments
            contentTable.act(Gdx.graphics.getDeltaTime());
            contentTable.addAction(new com.badlogic.gdx.scenes.scene2d.Action() {
                float currentPad = 0f;

                @Override
                public boolean act(float delta) {
                    float targetAlpha = charButton.isChecked() ? 1.0f : 0.5f;
                    float targetPad = charButton.isChecked() ? 15f : 0f;

                    // Interpolate alpha
                    contentTable.getColor().a = MathUtils.lerp(contentTable.getColor().a, targetAlpha, delta * 15f);

                    // Interpolate padding and force layout calculation
                    currentPad = MathUtils.lerp(currentPad, targetPad, delta * 15f);
                    charButton.padLeft(currentPad);
                    charButton.invalidate();

                    return false;
                }
            });

            charButton.add(contentTable).expand().fill();

            charButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (listener != null) listener.onCharacterSelected(actor);
                }
            });

            // ANIMATION: Add a delayed fade-in action based on the item index
            charButton.addAction(Actions.sequence(
                Actions.delay(itemIndex * 0.08f),
                Actions.fadeIn(0.3f, Interpolation.fade)
            ));

            rosterGroup.add(charButton);
            listContainer.add(charButton).row();

            if (isFirst) {
                if (listener != null) listener.onCharacterSelected(actor);
                isFirst = false;
            }

            itemIndex++;
        }
    }
}
