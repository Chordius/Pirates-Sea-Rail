package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.custom.FadingPortrait;
import com.chronicorn.frontend.skills.Skill;

public class RightDetailsTable extends Table {

    // Listener
    public interface EquipWeaponsListener {
        void onEquipWeaponsClicked();
    }

    private EquipWeaponsListener equipListener;

    public void setEquipListener(EquipWeaponsListener listener) {
        this.equipListener = listener;
    }

    // Layout Groups for Animation
    private Table portraitColumn;
    private Table detailsColumn; // Promoted to class scope so we can force its layout later
    private Table headerTable;
    private Table attributesTable;
    private Table skillsTable;

    // Dynamic Visuals
    private FadingPortrait portraitImage;
    private Image elementIcon;
    private Image equipHintImage;

    // Dynamic Labels
    private Label nameLabel;
    private Label levelLabel;
    private Label hpLabel;
    private Label atkLabel;
    private Label defLabel;
    private Label magLabel;
    private Label spdLabel;

    public RightDetailsTable() {
        super();
        buildUI();
    }

    private void buildUI() {
        // --- LEFT COLUMN: PORTRAIT ---
        portraitColumn = new Table();

        Stack portraitStack = new Stack(); // Use a Stack to overlay the hint on the portrait

        portraitImage = new FadingPortrait();
        portraitImage.setScaling(Scaling.fill);
        portraitImage.setScale(1.1f);

        equipHintImage = new Image();
        equipHintImage.setScaling(Scaling.none);
        equipHintImage.setVisible(false); // Hidden by default

        // CRITICAL: Disable touch on the hint so it doesn't interrupt the hover state
        // when the mouse crosses it
        equipHintImage.setTouchable(Touchable.disabled);

        // A wrapper table to position the hint exactly where you want it
        // (bottom-leftish)
        Table hintLayer = new Table();
        hintLayer.setTouchable(Touchable.disabled);
        hintLayer.add(equipHintImage).bottom().padTop(120).padRight(30);

        portraitStack.add(portraitImage);
        portraitStack.add(hintLayer);

        // --- HOVER & CLICK LOGIC ---
        portraitStack.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                // pointer == -1 ensures this is a mouse hover, not a touch drag
                if (pointer == -1 && portraitImage.getDrawable() != null) {
                    equipHintImage.clearActions();
                    equipHintImage.setVisible(true);
                    equipHintImage.getColor().a = 0f;
                    // Slight pop-in animation
                    equipHintImage.addAction(Actions.parallel(
                            Actions.fadeIn(0.2f),
                            Actions.sequence(
                                    Actions.moveBy(0, -10f),
                                    Actions.moveBy(0, 10f, 0.3f, Interpolation.swingOut))));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    equipHintImage.clearActions();
                    equipHintImage.addAction(Actions.sequence(
                            Actions.fadeOut(0.15f),
                            Actions.visible(false)));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (equipListener != null && portraitImage.getDrawable() != null) {
                    equipListener.onEquipWeaponsClicked();
                }
            }
        });

        portraitColumn.add(portraitStack).expand().top().right().padTop(36).padRight(15);

        // --- RIGHT COLUMN: DETAILS ---
        detailsColumn = new Table();
        detailsColumn.top().left().padTop(20);

        // 1. Header Section
        headerTable = new Table();

        Stack nameStack = new Stack();
        Image splashImg = new Image(ImageManager.skin.getDrawable("name-splash"));
        splashImg.setScaling(com.badlogic.gdx.utils.Scaling.none);

        Table splashLayer = new Table();
        splashLayer.add(splashImg);

        nameLabel = new Label("", ImageManager.skin, "menu3");
        Table nameLayer = new Table();
        nameLayer.add(nameLabel).center();

        nameStack.add(splashLayer);
        nameStack.add(nameLayer);

        Table subHeaderTable = new Table();
        elementIcon = new Image();
        elementIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        levelLabel = new Label("", ImageManager.skin, "menu2");

        subHeaderTable.add(elementIcon).size(48, 48).left().padLeft(25).expandX();
        subHeaderTable.add(levelLabel).right().padRight(10).expandX();

        headerTable.add(nameStack).center().padBottom(-36).padLeft(15).row();
        headerTable.add(subHeaderTable).expandX().fillX();

        // 2. Attributes Section
        attributesTable = new Table();
        attributesTable.setBackground(ImageManager.skin.getDrawable("attributes-bg"));
        attributesTable.pad(42, 30, 55, 45);

        hpLabel = new Label("", ImageManager.skin, "menu");
        hpLabel.setFontScale(0.86f);
        atkLabel = new Label("", ImageManager.skin, "menu");
        atkLabel.setFontScale(0.86f);
        defLabel = new Label("", ImageManager.skin, "menu");
        defLabel.setFontScale(0.86f);
        magLabel = new Label("", ImageManager.skin, "menu");
        magLabel.setFontScale(0.86f);
        spdLabel = new Label("", ImageManager.skin, "menu");
        spdLabel.setFontScale(0.86f);

        attributesTable.add(buildStatRow("Max HP", hpLabel)).expandX().fillX().padBottom(2).row();
        attributesTable.add(buildStatRow("Attack", atkLabel)).expandX().fillX().padBottom(2).row();
        attributesTable.add(buildStatRow("Defence", defLabel)).expandX().fillX().padBottom(2).row();
        attributesTable.add(buildStatRow("Magic", magLabel)).expandX().fillX().padBottom(2).row();
        attributesTable.add(buildStatRow("Speed", spdLabel)).expandX().fillX().row();

        // 3. Skills Section
        skillsTable = new Table();
        skillsTable.left();

        // Assemble Details Column
        detailsColumn.add(headerTable).expandX().fillX().padBottom(15).row();
        detailsColumn.add(attributesTable).right().padBottom(10).row();
        detailsColumn.add(skillsTable).right().row();

        // --- ASSEMBLE MASTER LAYOUT ---
        this.add(portraitColumn).expand().fill();
        this.add(detailsColumn).expandY().fillY().top().padRight(50);
    }

    private Table buildStatRow(String statName, Label valueLabel) {
        Table row = new Table();
        Label keyLabel = new Label(statName, ImageManager.skin, "menu");
        keyLabel.setFontScale(0.86f);
        row.add(keyLabel).left().expandX();
        row.add(valueLabel).right();
        return row;
    }

    public void updateDetails(Actor actor) {
        if (actor == null) {
            this.setVisible(false);
            return;
        }

        this.setVisible(true);

        String portraitName = "portrait_" + actor.getName().toLowerCase();
        if (ImageManager.skin.has(portraitName, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            portraitImage.setDrawable(ImageManager.skin.getDrawable(portraitName));
        }

        nameLabel.setText(actor.getName());
        levelLabel.setText("Lv. " + actor.getLevel());

        String elementStr = (actor.getElement() != null) ? actor.getElement().name().toLowerCase() : "none";
        elementIcon.setDrawable(ImageManager.skin.getDrawable(elementStr));

        String hintName = "equiphint_" + elementStr;
        if (ImageManager.skin.has(hintName, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            equipHintImage.setDrawable(ImageManager.skin.getDrawable(hintName));
        }

        hpLabel.setText(String.valueOf(actor.getMaxHp()));
        atkLabel.setText(String.valueOf(actor.getEffectivePrimaryParam(0)));
        defLabel.setText(String.valueOf(actor.getEffectivePrimaryParam(1)));
        magLabel.setText(String.valueOf(actor.getEffectivePrimaryParam(2)));
        spdLabel.setText(String.valueOf(actor.getEffectivePrimaryParam(3)));

        skillsTable.clearChildren();
        String skillBgName = "skill_menu_squarae_" + elementStr;

        com.badlogic.gdx.utils.Array<Skill> visibleSkills = new com.badlogic.gdx.utils.Array<>();
        for (Skill s : actor.getSkills()) {
            if (s.isShowConditionMet(actor)) {
                visibleSkills.add(s);
            }
        }

        for (int i = 0; i < 4; i++) {
            Table skillBox = new Table();
            skillBox.setBackground(ImageManager.skin.getDrawable(skillBgName));

            if (i < visibleSkills.size) {
                Skill skill = visibleSkills.get(i);
                String iconName = skill.getIconId();
                Image iconImg = new Image(ImageManager.skin.getDrawable(iconName));
                iconImg.setScaling(Scaling.fit);
                skillBox.add(iconImg).size(48, 48).center();
            }
            skillsTable.add(skillBox).size(72, 72).padLeft(28);
        }
    }

    public void playEntranceAnimation() {
        // 1. Wipe the master queue
        this.clearActions();

        // 2. Stop all sub-elements and turn them invisible
        portraitColumn.clearActions();
        headerTable.clearActions();
        attributesTable.clearActions();
        skillsTable.clearActions();

        portraitColumn.getColor().a = 0f;
        headerTable.getColor().a = 0f;
        attributesTable.getColor().a = 0f;
        skillsTable.getColor().a = 0f;

        // 3. Defer coordinate capture
        this.addAction(Actions.sequence(
                Actions.delay(0.02f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        // FORCE LAYOUT OVERWRITE
                        // This forces LibGDX to re-snap the elements to their exact designated
                        // cell bounds, destroying any stray mid-animation coordinates.
                        RightDetailsTable.this.invalidate();
                        RightDetailsTable.this.layout();

                        if (detailsColumn != null) {
                            detailsColumn.invalidate();
                            detailsColumn.layout();
                        }

                        // Capture the stable coordinates
                        float portX = portraitColumn.getX();
                        float portY = portraitColumn.getY();

                        float headX = headerTable.getX();
                        float headY = headerTable.getY();

                        float attrX = attributesTable.getX();
                        float attrY = attributesTable.getY();

                        float skillX = skillsTable.getX();
                        float skillY = skillsTable.getY();

                        // Teleport away
                        portraitColumn.setPosition(portX, portY - 60f);
                        headerTable.setPosition(headX + 80f, headY);
                        attributesTable.setPosition(attrX + 80f, attrY);
                        skillsTable.setPosition(skillX + 80f, skillY);

                        // Animate back to the captured coordinates
                        portraitColumn.addAction(Actions.parallel(
                                Actions.fadeIn(0.4f),
                                Actions.moveTo(portX, portY, 0.45f, Interpolation.swingOut)));

                        float delayStep = 0.12f;

                        headerTable.addAction(Actions.sequence(
                                Actions.delay(delayStep * 0),
                                Actions.parallel(
                                        Actions.fadeIn(0.3f),
                                        Actions.moveTo(headX, headY, 0.55f, Interpolation.swingOut))));

                        attributesTable.addAction(Actions.sequence(
                                Actions.delay(delayStep * 1),
                                Actions.parallel(
                                        Actions.fadeIn(0.3f),
                                        Actions.moveTo(attrX, attrY, 0.55f, Interpolation.swingOut))));

                        skillsTable.addAction(Actions.sequence(
                                Actions.delay(delayStep * 2),
                                Actions.parallel(
                                        Actions.fadeIn(0.3f),
                                        Actions.moveTo(skillX, skillY, 0.55f, Interpolation.swingOut))));
                    }
                })));
    }
}
