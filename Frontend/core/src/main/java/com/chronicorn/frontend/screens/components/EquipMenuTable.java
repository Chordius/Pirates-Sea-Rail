package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.items.Equippable;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.systems.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class EquipMenuTable extends Table {

    public interface EquipInteractionListener {
        void onConfirmClicked();

        void onCloseClicked();
    }

    private EquipInteractionListener listener;

    public void setListener(EquipInteractionListener listener) {
        this.listener = listener;
    }

    public Equippable[] getEquippedWeapons() {
        return equippedWeapons;
    }

    private Table leftColumn;
    private Table rightColumn;

    private Image equipHeader;
    private Table equipBgContainer;
    private Table slotsTable;
    private Table attributesTable;
    private Table weaponsListContainer;

    private ButtonGroup<Button> slotGroup;
    private Image[] slotIcons = new Image[2];
    private Equippable[] equippedWeapons = new Equippable[2];

    // Track dynamic visibility states for cards using unique IDs
    private Map<String, Table> statusBadgeMap = new HashMap<>();

    public EquipMenuTable() {
        super();
        this.setFillParent(true);
        buildUI();
    }

    private void buildUI() {
        leftColumn = new Table();
        leftColumn.top().left();

        equipHeader = new Image();
        equipHeader.setScaling(com.badlogic.gdx.utils.Scaling.none);

        equipBgContainer = new Table();
        equipBgContainer.setBackground(ImageManager.skin.getDrawable("equip_menu"));
        equipBgContainer.top().left();

        slotsTable = new Table();
        slotsTable.left();

        slotGroup = new ButtonGroup<>();
        slotGroup.setMaxCheckCount(1);
        slotGroup.setMinCheckCount(1);

        Button.ButtonStyle slotStyle = new Button.ButtonStyle();
        slotStyle.up = ImageManager.skin.getDrawable("skill_menu_square_none");
        slotStyle.checked = ImageManager.skin.newDrawable("skill_menu_square_none", Color.valueOf("#EFEBE0"));

        for (int i = 0; i < 2; i++) {
            Table slotBox = new Table();
            slotIcons[i] = new Image();
            slotIcons[i].setScaling(com.badlogic.gdx.utils.Scaling.fit);
            slotBox.add(slotIcons[i]).size(32, 32);

            Button slotBtn = new Button(slotStyle);
            slotBtn.add(slotBox).expand().fill();
            slotGroup.add(slotBtn);

            slotsTable.add(slotBtn).size(72, 72).padRight(64);
        }

        attributesTable = new Table();
        attributesTable.pad(15, 30, 45, 60);

        equipBgContainer.add(slotsTable).left().padTop(22).padLeft(30).row();
        equipBgContainer.add(attributesTable).left().padTop(75).padLeft(10);

        // Assemble the Left Column by layering the header on top of the parchment
        // background
        Stack leftStack = new Stack();

        // Layer 1: The parchment background asset (shifted up to secure a tight
        // alignment vector)
        Table backgroundLayer = new Table();
        backgroundLayer.top().left().padTop(75);
        backgroundLayer.add(equipBgContainer).left();

        // Layer 2: The foreground brush stroke text header overlay
        Table headerLayer = new Table();
        headerLayer.top().left();
        headerLayer.add(equipHeader).left().padLeft(-20);

        // Combine the layers inside the stack container
        leftStack.add(backgroundLayer);
        leftStack.add(headerLayer);

        // Attach the composite layout stack directly to your column pipeline
        leftColumn.add(leftStack).top().left();

        rightColumn = new Table();
        rightColumn.top().left(); // Secures the container structure on the top edge

        final Table rightPanel = new Table();
        rightPanel.top().left(); // Aligns internal components to match the header's top edge

        weaponsListContainer = new Table();
        weaponsListContainer.top().left();

        ScrollPane scrollPane = new ScrollPane(weaponsListContainer, ImageManager.skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        // Aligned layout boundaries close the visual dead space completely
        rightPanel.add(scrollPane).width(480).height(380).expandY().fillY().top().left().row();

        // BUTTON CONTROL ROW (Placed directly under the right-side weapon inventory
        // grid)
        Table actionButtonRow = new Table();
        actionButtonRow.left().padTop(15);

        TextButton confirmBtn = new TextButton("Confirm Equip", ImageManager.skin, "boxed-button");
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onConfirmClicked();
            }
        });

        TextButton closeBtn = new TextButton("Close Equipment", ImageManager.skin, "boxed-button");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onCloseClicked();
            }
        });

        // FIXED: Using cell dimensions instead of .setSize() ensures the text
        // background scales perfectly
        actionButtonRow.add(confirmBtn).width(250).height(65).padRight(15);
        actionButtonRow.add(closeBtn).width(250).height(65);
        rightPanel.add(actionButtonRow).left();

        rightColumn.add(rightPanel).expand().fill().top().left(); // Ensures matching top alignment across vectors

        // ROOT CONTAINER ASSEMBLY
        this.clearChildren();
        this.top().left().padTop(120);
        this.add(leftColumn).top().left();
        this.add(rightColumn).expand().fill().top().left().padLeft(40);
    }

    private Table buildStatRow(String statName, Label valueLabel, Label unitLabel) {
        Table row = new Table();
        Label keyLabel = new Label(statName, ImageManager.skin, "menu");
        row.add(keyLabel).width(160).left();
        row.add(valueLabel).right().expandX();
        row.add(unitLabel).width(18).left().padLeft(2);
        return row;
    }

    public void updateMenu(Actor actor, PlayerInventory inventory) {
        String elementStr = (actor.getElement() != null) ? actor.getElement().name().toLowerCase() : "none";

        String headerName = "equipweapon_" + elementStr;
        if (ImageManager.skin.has(headerName, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            equipHeader.setDrawable(ImageManager.skin.getDrawable(headerName));
        }

        equippedWeapons[0] = null;
        equippedWeapons[1] = null;
        slotIcons[0].setDrawable(null);
        slotIcons[1].setDrawable(null);

        for (int i = 0; i < 2; i++) {
            if (i < actor.getEquipments().size) {
                Equippable eq = actor.getEquipments().get(i);
                equippedWeapons[i] = eq;
                if (eq != null) {
                    String assetIconKey = eq.getIcon();
                    slotIcons[i].setDrawable(ImageManager.skin.getDrawable(assetIconKey));
                }
            }
        }

        if (slotGroup.getButtons().size > 0) {
            slotGroup.getButtons().get(0).setChecked(true);
        }

        weaponsListContainer.clearChildren();
        statusBadgeMap.clear();

        if (inventory.getOwnedEquipments().isEmpty()) {
            weaponsListContainer.add(new Label("No equipment available.", ImageManager.skin)).pad(20);
            updateBonusLabels();
            return;
        }

        int columns = 4;
        int currentCol = 0;

        for (final Equippable equip : inventory.getOwnedEquipments()) {
            Stack cardStack = new Stack();

            String assetIconKey = equip.getIcon();
            final Drawable weaponIconDrawable = ImageManager.skin.getDrawable(assetIconKey);

            Image wpnIcon = new Image(weaponIconDrawable);
            wpnIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);

            Table faceBg = new Table();
            faceBg.setBackground(ImageManager.skin.newDrawable("white-pixel", Color.valueOf("686259")));
            faceBg.add(wpnIcon).size(64, 64).center();

            // Replicated Badge Architecture
            Table badgeContainer = new Table();
            badgeContainer.top();

            Table badgeBg = new Table();
            badgeBg.setBackground(ImageManager.skin.newDrawable("white-pixel", Color.valueOf("EBE7DC")));
            Label badgeLabel = new Label("", ImageManager.skin, "menu");
            badgeBg.add(badgeLabel).pad(0, 0, 0, 0);
            badgeContainer.add(badgeBg).fillX().expandX();
            badgeContainer.setVisible(false);

            statusBadgeMap.put(equip.getId(), badgeContainer);

            Button.ButtonStyle emptyStyle = new Button.ButtonStyle();
            Button clickTarget = new Button(emptyStyle);

            clickTarget.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int activeSlot = slotGroup.getCheckedIndex();
                    if (activeSlot != -1) {
                        // TOGGLE MECHANIC: If clicked item is already assigned to this exact slot,
                        // unequip it
                        if (equippedWeapons[activeSlot] == equip) {
                            equippedWeapons[activeSlot] = null;
                            slotIcons[activeSlot].setDrawable(null);
                        } else {
                            int otherSlot = (activeSlot == 0) ? 1 : 0;
                            // Block movement if mapped to the opposite slot channel
                            if (equippedWeapons[otherSlot] == equip)
                                return;

                            equippedWeapons[activeSlot] = equip;
                            slotIcons[activeSlot].setDrawable(weaponIconDrawable);
                        }

                        updateBonusLabels();
                        refreshAllStatusLabels();
                    }
                }
            });

            // Reassembled Card Elements (Pure 1:1 Square - No name label)
            cardStack.add(faceBg);
            cardStack.add(badgeContainer);
            cardStack.add(clickTarget);

            // Reconfigured card box constraints to standard uniform 1:1 dimensions
            weaponsListContainer.add(cardStack).size(100, 100).pad(8);

            currentCol++;
            if (currentCol >= columns) {
                weaponsListContainer.row();
                currentCol = 0;
            }
        }

        updateBonusLabels();
        refreshAllStatusLabels();
    }

    private void refreshAllStatusLabels() {
        // Reset all badges on screen
        for (Table badge : statusBadgeMap.values()) {
            badge.setVisible(false);
        }

        // Toggles display tracking metrics matching slot layout configs directly
        for (int i = 0; i < 2; i++) {
            Equippable activeWpn = equippedWeapons[i];
            if (activeWpn != null) {
                Table badgeContainer = statusBadgeMap.get(activeWpn.getId());
                if (badgeContainer != null) {
                    badgeContainer.setVisible(true);

                    Table badgeBg = (Table) badgeContainer.getChildren().get(0);
                    Label badgeLabel = (Label) badgeBg.getChildren().get(0);

                    badgeLabel.setText("Slot " + (i + 1));
                }
            }
        }
    }

    private void updateBonusLabels() {
        attributesTable.clearChildren();

        // Use a map to accumulate bonuses dynamically for any stat type present
        java.util.Map<Equippable.StatType, Float> accumulatedBonuses = new java.util.EnumMap<>(
                Equippable.StatType.class);

        // Accumulate stats from both equipped slots
        for (int i = 0; i < 2; i++) {
            Equippable wpn = equippedWeapons[i];
            if (wpn != null) {
                // Handle the fixed baseline stat
                if (wpn.getFixedStat() != null) {
                    accumulatedBonuses.put(wpn.getFixedStat(),
                            accumulatedBonuses.getOrDefault(wpn.getFixedStat(), 0f) + wpn.getFixedValue());
                }
                // Handle the randomized secondary stat roll
                if (wpn.getRandomStat() != null) {
                    accumulatedBonuses.put(wpn.getRandomStat(),
                            accumulatedBonuses.getOrDefault(wpn.getRandomStat(), 0f) + wpn.getRandomPercentBonus());
                }
            }
        }

        // Loop through the accumulated stats to build layout cells on demand
        for (java.util.Map.Entry<Equippable.StatType, Float> entry : accumulatedBonuses.entrySet()) {
            Equippable.StatType stat = entry.getKey();
            float totalValue = entry.getValue();

            if (totalValue <= 0)
                continue;

            // Format formatting criteria depending on the stat type categorization rules
            String valueStr;
            String unitStr;
            if (stat == Equippable.StatType.ATTACK || stat == Equippable.StatType.DEFENSE ||
                    stat == Equippable.StatType.MAGIC || stat == Equippable.StatType.SPEED ||
                    stat == Equippable.StatType.MAX_HP) {
                // Render flat attributes as discrete integer allocations
                valueStr = "+" + (int) totalValue;
                unitStr = "";
            } else {
                // Render combat rate allocations as percentage distributions scaled by 100
                valueStr = "+" + (int) (totalValue * 100);
                unitStr = "%";
            }

            // Convert Enum names to clean title case labels (e.g., CRIT_RATE -> Crit Rate)
            String cleanName = stat.name().replace("_", " ").toLowerCase();
            cleanName = Character.toUpperCase(cleanName.charAt(0)) + cleanName.substring(1);

            Label valLabel = new Label(valueStr, ImageManager.skin, "menu");
            valLabel.setColor(Color.LIME);

            Label uniLabel = new Label(unitStr, ImageManager.skin, "menu");
            uniLabel.setColor(Color.LIME);

            attributesTable.add(buildStatRow(cleanName, valLabel, uniLabel)).expandX().fillX().padBottom(10).row();
        }

        // Render clean layout updates if no weapon instances are currently active
        if (accumulatedBonuses.isEmpty()) {
            Label emptyLabel = new Label("No Attribute Bonuses", ImageManager.skin, "menu");
            emptyLabel.getColor().a = 0.5f;
            attributesTable.add(emptyLabel).left().padLeft(10);
        }
    }

    public void playEntranceAnimation() {
        this.clearActions();
        leftColumn.clearActions();
        rightColumn.clearActions();

        this.getColor().a = 1f;
        leftColumn.getColor().a = 0f;
        rightColumn.getColor().a = 0f;

        this.addAction(Actions.sequence(
                Actions.delay(0.05f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        EquipMenuTable.this.invalidateHierarchy();
                        EquipMenuTable.this.layout();

                        float leftX = leftColumn.getX();
                        float leftY = leftColumn.getY();
                        float rightX = rightColumn.getX();
                        float rightY = rightColumn.getY();

                        leftColumn.setPosition(leftX - 100f, leftY);
                        rightColumn.setPosition(rightX + 100f, rightY);

                        leftColumn.addAction(Actions.parallel(
                                Actions.fadeIn(0.4f),
                                Actions.moveTo(leftX, leftY, 0.5f, Interpolation.swingOut)));

                        rightColumn.addAction(Actions.sequence(
                                Actions.delay(0.1f),
                                Actions.parallel(
                                        Actions.fadeIn(0.4f),
                                        Actions.moveTo(rightX, rightY, 0.5f, Interpolation.swingOut))));
                    }
                })));
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Loop through your slots and apply a breathing alpha wave to the selected one
        for (int i = 0; i < slotGroup.getButtons().size; i++) {
            Button btn = slotGroup.getButtons().get(i);
            if (btn.isChecked()) {
                // Calculate a smooth sin wave oscillation over engine runtimes
                float alphaBreathing = 0.4f + (MathUtils.sin(Gdx.graphics.getFrameId() * 0.1f) + 1f) * 0.3f;
                btn.getColor().a = alphaBreathing;
            } else {
                btn.getColor().a = 1.0f; // Return unselected slots to stable visibility states
            }
        }
    }
}
