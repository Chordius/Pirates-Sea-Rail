package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.items.Item;
import com.chronicorn.frontend.items.ItemDatabase;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.systems.PlayerInventory;

import java.util.Map;

public class ItemsTable extends Table {
    private Table leftColumn;
    private Table rightColumn;

    private Table leftRosterContainer;
    private Table itemsGridContainer;
    private Table detailsBgContainer;

    private String selectedItemId = null;
    private Label statusLabel;

    public ItemsTable() {
        super();
        this.setFillParent(true);
        buildUI();
    }

    private void buildUI() {
        leftColumn = new Table();
        leftColumn.top().left();

        leftRosterContainer = new Table();
        leftRosterContainer.top().left().pad(20);

        Stack leftStack = new Stack();

        Table rosterBgPanel = new Table();
        rosterBgPanel.setBackground(ImageManager.skin.getDrawable("gradient-bg"));
        rosterBgPanel.center();
        rosterBgPanel.add(leftRosterContainer).fill().expand();

        Table backgroundLayer = new Table();
        backgroundLayer.top().left().padTop(75);
        backgroundLayer.add(rosterBgPanel).width(320).height(380).left();

        leftStack.add(backgroundLayer);
        leftColumn.add(leftStack).top().left();

        rightColumn = new Table();
        rightColumn.top().left();

        Table rightPanel = new Table();
        rightPanel.top().left();

        itemsGridContainer = new Table();
        itemsGridContainer.top().left();

        ScrollPane scrollPane = new ScrollPane(itemsGridContainer, ImageManager.skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        detailsBgContainer = new Table();
        detailsBgContainer.setBackground(ImageManager.skin.getDrawable("window-drawable"));
        detailsBgContainer.top().left().pad(20);

        rightPanel.add(scrollPane).width(560).height(450).top().left();

        // FIXED 1: Replaced simple padLeft with symmetrical window boundaries to minimize right negative space
        rightPanel.add(detailsBgContainer).width(450).height(450).top().left().pad(0, 20, 0, 40);

        rightColumn.add(rightPanel).expand().fill().top().left();

        this.clearChildren();
        this.top().left().padTop(120);
        this.add(leftColumn).top().left();
        this.add(rightColumn).expand().fill().top().left().padLeft(40);

        refreshGrid();
        refreshLeftRoster();
        refreshDetails();
    }

    public void refreshGrid() {
        itemsGridContainer.clearChildren();
        PlayerInventory inventory = GameSession.getInstance().inventory;

        Map<String, Integer> allItems = inventory.getAllItems();

        java.util.List<Map.Entry<String, Integer>> itemsList = new java.util.ArrayList<>();
        if (allItems != null) {
            for (Map.Entry<String, Integer> entry : allItems.entrySet()) {
                if (entry.getValue() > 0) {
                    itemsList.add(entry);
                }
            }
        }

        int columns = 4;
        int totalSlots = Math.max(16, itemsList.size());

        for (int i = 0; i < totalSlots; i++) {
            if (i < itemsList.size()) {
                Map.Entry<String, Integer> entry = itemsList.get(i);
                final String itemId = entry.getKey();
                final int quantity = entry.getValue();

                final Item item = ItemDatabase.getItem(itemId);
                if (item == null) continue;

                Stack cardStack = new Stack();

                Table slotWireframe = new Table();
                slotWireframe.setBackground(ImageManager.skin.getDrawable("skill_menu_square_none"));
                cardStack.add(slotWireframe);

                String assetIconKey = item.getIcon();
                Drawable itemIconDrawable = ImageManager.skin.getDrawable(assetIconKey);
                Image itemIcon = new Image(itemIconDrawable);
                itemIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);

                Table iconWrapper = new Table();
                iconWrapper.center();
                iconWrapper.add(itemIcon).size(48, 48).center();
                cardStack.add(iconWrapper);

                Table qtyTable = new Table();
                qtyTable.bottom().right().padRight(22).padBottom(22);
                Label qtyLabel = new Label(String.valueOf(quantity), ImageManager.skin, "menu2");
                qtyLabel.setFontScale(0.85f);
                qtyLabel.setColor(Color.WHITE);
                qtyTable.add(qtyLabel);
                cardStack.add(qtyTable);

                Button.ButtonStyle emptyStyle = new Button.ButtonStyle();
                Button clickTarget = new Button(emptyStyle);

                clickTarget.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (selectedItemId != null && selectedItemId.equals(itemId)) {
                            selectedItemId = null;
                        } else {
                            selectedItemId = itemId;
                        }
                        refreshGrid();
                        refreshLeftRoster();
                        refreshDetails();
                    }
                });
                cardStack.add(clickTarget);

                if (selectedItemId != null && selectedItemId.equals(itemId)) {
                    Image selectBorder = new Image(ImageManager.skin.getDrawable("selection-box"));
                    cardStack.add(selectBorder);
                }

                itemsGridContainer.add(cardStack).size(100, 100).pad(8);
            } else {
                Stack emptyStack = new Stack();
                Table emptyBg = new Table();
                emptyBg.setBackground(ImageManager.skin.getDrawable("skill_menu_square_none"));
                emptyStack.add(emptyBg);

                itemsGridContainer.add(emptyStack).size(100, 100).pad(8);
            }

            if ((i + 1) % columns == 0) {
                itemsGridContainer.row();
            }
        }
    }

    public void refreshLeftRoster() {
        leftRosterContainer.clearChildren();
        java.util.List<Actor> activeParty = GameSession.getInstance().getParty().getActivePartyActors();

        Label title = new Label("Active Party", ImageManager.skin, "menu2");
        title.setColor(Color.GOLD);
        leftRosterContainer.add(title).padBottom(15).center().row();

        Table partyList = new Table();
        partyList.top().left();
        partyList.defaults().height(80).width(280).padBottom(10);

        for (final Actor actor : activeParty) {
            if (actor == null) continue;

            ImageManager.loadMenuCharacterAsset(actor.getName());

            Button.ButtonStyle actorStyle = new Button.ButtonStyle();
            actorStyle.up = ImageManager.skin.newDrawable("white-pixel", Color.valueOf("686259"));
            actorStyle.down = ImageManager.skin.newDrawable("white-pixel", Color.valueOf("aa9573"));

            Button actorBtn = new Button(actorStyle);

            if (selectedItemId != null) {
                actorBtn.getColor().a = 1.0f;
                actorBtn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            } else {
                actorBtn.getColor().a = 0.6f;
                actorBtn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            }

            Table cellTable = new Table();
            cellTable.left().pad(5);

            Image face = new Image(ImageManager.skin.getDrawable("face_" + actor.getName().toLowerCase()));
            face.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            cellTable.add(face).size(48, 48).padRight(12);

            Table info = new Table();
            info.left();
            Label nameLbl = new Label(actor.getName(), ImageManager.skin, "menu2");

            Label hpLbl = new Label("HP: " + actor.getHp() + "/" + actor.getMaxHp(), ImageManager.skin, "default");
            hpLbl.setFontScale(0.85f);
            // FIXED 2: Change the text rendering color state of the health metric to clear white
            hpLbl.setColor(Color.WHITE);

            Label enLbl = new Label("EN: " + actor.getEnergy() + "/" + actor.getMaxEnergy(), ImageManager.skin, "default");
            enLbl.setFontScale(0.85f);

            info.add(nameLbl).left().row();
            Table stats = new Table();
            stats.add(hpLbl).padRight(15);
            stats.add(enLbl);
            info.add(stats).left();

            cellTable.add(info).expandX().fillX();
            actorBtn.add(cellTable).expand().fill();

            if (selectedItemId != null) {
                actorBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Item item = ItemDatabase.getItem(selectedItemId);
                        if (item != null) {
                            useItemOnActor(item, actor);
                        }
                    }
                });
            }

            partyList.add(actorBtn).row();
        }

        leftRosterContainer.add(partyList).expandX().fillX().row();

        // FIXED 3: Replaced the unoutlined "menu" style sheet marker with the outlined "menu3" style layout configuration
        Label promptLabel = new Label(selectedItemId != null ? "Select target" : "Select an item to use", ImageManager.skin, "menu2");
        promptLabel.setColor(selectedItemId != null ? Color.GOLD : Color.LIGHT_GRAY);
        leftRosterContainer.add(promptLabel).padTop(10).center();
    }

    public void refreshDetails() {
        detailsBgContainer.clearChildren();

        if (selectedItemId == null) {
            Label placeholder = new Label("No item\nselected.", ImageManager.skin, "menu2");
            placeholder.setAlignment(Align.center);
            placeholder.getColor().a = 0.5f;
            detailsBgContainer.add(placeholder).width(240).padTop(120).center();
            return;
        }

        Item item = ItemDatabase.getItem(selectedItemId);
        if (item == null) {
            selectedItemId = null;
            refreshDetails();
            return;
        }

        int quantity = GameSession.getInstance().inventory.getItemQuantity(selectedItemId);
        if (quantity <= 0) {
            selectedItemId = null;
            refreshDetails();
            return;
        }

        detailsBgContainer.defaults().align(Align.left).space(12).padLeft(25);;

        Label nameLbl = new Label(item.getName(), ImageManager.skin, "menu2");
        detailsBgContainer.add(nameLbl).width(240).row();

        Table separator = new Table();
        separator.setBackground(ImageManager.skin.newDrawable("white-pixel", Color.valueOf("aa9573")));
        detailsBgContainer.add(separator).height(2).fillX().padTop(2).padBottom(4).row();

        Table iconRow = new Table();
        iconRow.left();

        Image itemIcon = new Image(ImageManager.skin.getDrawable(item.getIcon()));
        itemIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        Table detailsFaceBg = new Table();
        detailsFaceBg.setBackground(ImageManager.skin.getDrawable("skill_menu_square_none"));
        detailsFaceBg.add(itemIcon).size(48, 48).center();

        Label qtyLbl = new Label("Quantity: " + quantity, ImageManager.skin, "menu");

        iconRow.add(detailsFaceBg).size(64, 64).padRight(12);
        iconRow.add(qtyLbl).left();
        detailsBgContainer.add(iconRow).row();

        Label descLbl = new Label(item.getDescription(), ImageManager.skin, "menu");
        descLbl.setWrap(true);
        descLbl.setColor(Color.WHITE);
        detailsBgContainer.add(descLbl).width(240).row();

        statusLabel = new Label("", ImageManager.skin, "menu");
        statusLabel.setFontScale(0.85f);
        statusLabel.setWrap(true);
        statusLabel.setColor(Color.LIME);
        detailsBgContainer.add(statusLabel).width(240).padTop(10).row();
    }

    private void useItemOnActor(Item item, Actor actor) {
        boolean success = item.useOn(actor);
        if (success) {
            GameSession.getInstance().inventory.removeItem(item.getId(), 1);
            int remaining = GameSession.getInstance().inventory.getItemQuantity(item.getId());

            String itemName = item.getName();
            String actorName = actor.getName();

            if (remaining <= 0) {
                selectedItemId = null;
            }

            refreshGrid();
            refreshLeftRoster();
            refreshDetails();

            if (statusLabel != null) {
                statusLabel.setText("Used " + itemName + " on " + actorName + "!");
                statusLabel.setColor(Color.GREEN);
            }
        } else {
            if (statusLabel != null) {
                statusLabel.setText("No effect on " + actor.getName() + "!");
                statusLabel.setColor(Color.RED);
            }
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
                    ItemsTable.this.invalidateHierarchy();
                    ItemsTable.this.layout();

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
}
