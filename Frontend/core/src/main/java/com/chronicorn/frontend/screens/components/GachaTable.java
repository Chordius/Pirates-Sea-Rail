package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.battlers.ActorFactory;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.networkManager.NetworkManager;
import com.chronicorn.frontend.managers.networkManager.NetworkCallback;
import com.chronicorn.frontend.managers.networkManager.dto.GachaResult;
import com.chronicorn.frontend.managers.networkManager.dto.UserAuthResponse;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.screens.GachaAnimationScreen;
import com.chronicorn.frontend.screens.MenuScreen;

public class GachaTable extends Table {

    // Animation layout groups
    private Table leftRibbonColumn;
    private Table rightContentColumn;
    private Table bannerContainer;
    private Table purchasePanel;
    private Table bottomBar;
    private Table buttonWrapper;

    // Dynamic Elements
    private Image currentBannerImage;
    private Label currencyLabel;
    private Label statusLabel;
    private ButtonGroup<Button> ribbonGroup;

    // Hardcoded for the example: List of banner IDs
    private String[] activeBanners = { "deal", "ragnar", "money" };

    // Store currently selected banner for the API
    private String currentBannerSelection;

    public GachaTable() {
        super();
        buildUI();
    }

    private void buildUI() {
        this.setFillParent(true);
        this.top().left();

        // Calculate maximum ribbon width to dynamically size the left column
        float calculatedMaxRibbonWidth = 0;
        for (String bannerId : activeBanners) {
            String selectBannerName = "SelectBanner_" + bannerId;
            if (ImageManager.skin.has(selectBannerName, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                calculatedMaxRibbonWidth = Math.max(calculatedMaxRibbonWidth,
                        ImageManager.skin.getDrawable(selectBannerName).getMinWidth());
            }
        }
        final float maxRibbonWidth = calculatedMaxRibbonWidth > 0 ? calculatedMaxRibbonWidth : 85f;

        // ==========================================
        // 1. LEFT COLUMN: Ribbon Selectors
        // ==========================================
        leftRibbonColumn = new Table();
        leftRibbonColumn.left(); // Center vertically on the left of the screen

        ribbonGroup = new ButtonGroup<>();
        ribbonGroup.setMaxCheckCount(1);
        ribbonGroup.setMinCheckCount(1);
        ribbonGroup.setUncheckLast(true);

        for (int i = 0; i < activeBanners.length; i++) {
            final String bannerId = activeBanners[i];

            // Create the jagged ribbon button
            Button.ButtonStyle ribbonStyle = new Button.ButtonStyle();
            String selectBannerName = "SelectBanner_" + bannerId;
            float origWidth = 75f;
            float origHeight = 36f;

            if (ImageManager.skin.has(selectBannerName, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                com.badlogic.gdx.scenes.scene2d.utils.Drawable drawable = ImageManager.skin
                        .getDrawable(selectBannerName);
                origWidth = drawable.getMinWidth();
                origHeight = drawable.getMinHeight();
                ribbonStyle.up = ImageManager.skin.newDrawable(selectBannerName, new Color(0.75f, 0.75f, 0.75f, 1f));
                ribbonStyle.checked = ImageManager.skin.newDrawable(selectBannerName, Color.WHITE);
            } else {
                ribbonStyle.up = ImageManager.skin.newDrawable("white-pixel", new Color(0.85f, 0.8f, 0.7f, 1f));
                ribbonStyle.checked = ImageManager.skin.newDrawable("white-pixel", new Color(1f, 0.95f, 0.85f, 1f));
            }

            final Button ribbonBtn = new Button(ribbonStyle);
            ribbonBtn.setTransform(true);

            final boolean[] hovered = { false };
            ribbonBtn.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer,
                        com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) {
                        hovered[0] = true;
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer,
                        com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    if (pointer == -1) {
                        hovered[0] = false;
                    }
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    updateBanner(bannerId);
                }
            });

            // Smoothly scale the button by 1.1x when hovered or selected (checked)
            ribbonBtn.addAction(new com.badlogic.gdx.scenes.scene2d.Action() {
                @Override
                public boolean act(float delta) {
                    float targetScale = (hovered[0] || ribbonBtn.isChecked()) ? 1.1f : 1.0f;
                    float currentScale = ribbonBtn.getScaleX();
                    float newScale = com.badlogic.gdx.math.MathUtils.lerp(currentScale, targetScale, delta * 15f);
                    ribbonBtn.setScale(newScale);
                    ribbonBtn.setOrigin(0, ribbonBtn.getHeight() / 2f);
                    return false;
                }
            });

            ribbonGroup.add(ribbonBtn);

            // Negative pad left to attach it to the edge of the screen
            leftRibbonColumn.add(ribbonBtn).height(origHeight).width(origWidth).padBottom(15).left().padLeft(-5).row();
        }

        // ==========================================
        // 2. RIGHT COLUMN: Banner & Controls
        // ==========================================
        rightContentColumn = new Table();
        rightContentColumn.top().left().padTop(60).padRight(40).padLeft(20);

        // A. The Massive Banner Image
        bannerContainer = new Table();
        currentBannerImage = new Image();
        bannerContainer.add(currentBannerImage).padTop(24).center();

        // B. The Purchase Panel (Top-Up UI)
        purchasePanel = new Table();
        purchasePanel.center();

        Table card = new Table();
        card.setBackground(ImageManager.skin.getDrawable("window-drawable"));
        card.pad(30);

        Label titleLabel = new Label("CENTRAL WALLET TOP-UP", ImageManager.skin, "menu2");
        titleLabel.setAlignment(Align.center);
        card.add(titleLabel).padBottom(15).colspan(3).row();

        Label descLabel = new Label("Exchange Central Wallet balance for Premium Currency", ImageManager.skin,
                "default");
        descLabel.setAlignment(Align.center);
        descLabel.setColor(Color.LIGHT_GRAY);
        card.add(descLabel).padBottom(25).colspan(3).row();

        // Transaction Visuals
        Table costBlock = new Table();
        costBlock.setBackground(ImageManager.skin.getDrawable("label-bg"));
        costBlock.pad(10, 20, 10, 20);
        Label costVal = new Label("$10.00", ImageManager.skin, "menu2");
        Label costName = new Label("Wallet Balance", ImageManager.skin, "default");
        costName.setColor(Color.LIGHT_GRAY);
        costBlock.add(costVal).row();
        costBlock.add(costName);

        Label arrowLabel = new Label("-->", ImageManager.skin, "menu3");
        arrowLabel.setColor(Color.GOLD);

        Table rewardBlock = new Table();
        rewardBlock.setBackground(ImageManager.skin.getDrawable("label-bg"));
        rewardBlock.pad(10, 20, 10, 20);

        Table rewardRow = new Table();
        Image doubloonImage = new Image(
                ImageManager.skin.has("icon-doubloon", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)
                        ? ImageManager.skin.getDrawable("icon-doubloon")
                        : ImageManager.skin.newDrawable("white-pixel", Color.GOLD));
        Label rewardVal = new Label("160", ImageManager.skin, "menu2");
        rewardRow.add(doubloonImage).size(24, 24).padRight(5);
        rewardRow.add(rewardVal);

        Label rewardName = new Label("Clue", ImageManager.skin, "default");
        rewardName.setColor(Color.LIGHT_GRAY);
        rewardBlock.add(rewardRow).row();
        rewardBlock.add(rewardName);

        card.add(costBlock).width(150);
        card.add(arrowLabel).padLeft(20).padRight(20);
        card.add(rewardBlock).width(150).row();

        statusLabel = new Label("", ImageManager.skin, "default");
        statusLabel.setAlignment(Align.center);
        card.add(statusLabel).padTop(15).padBottom(10).colspan(3).row();

        TextButton buyBtn = new TextButton("Confirm Top-Up", ImageManager.skin, "boxed-button");
        buyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (Main.currentLocalId == null) {
                    statusLabel.setText("Error: User not logged in.");
                    statusLabel.setColor(Color.RED);
                    return;
                }

                statusLabel.setText("Connecting to Central Wallet...");
                statusLabel.setColor(Color.YELLOW);
                buyBtn.setDisabled(true);

                NetworkManager.buyCurrency(Main.currentLocalId, 10.0, 160, new NetworkCallback<String>() {
                    @Override
                    public void onSuccess(String msg) {
                        statusLabel.setText("Purchase Successful!");
                        statusLabel.setColor(Color.GREEN);
                        buyBtn.setDisabled(false);
                        refreshUserCurrency();
                    }

                    @Override
                    public void onError(String err) {
                        statusLabel.setText("Failed: " + err);
                        statusLabel.setColor(Color.RED);
                        buyBtn.setDisabled(false);
                    }
                });
            }
        });
        card.add(buyBtn).width(200).height(45).colspan(3).row();
        purchasePanel.add(card);

        // Put banner and purchase panels in a Stack
        Stack mainAreaStack = new Stack();
        mainAreaStack.add(bannerContainer);
        mainAreaStack.add(purchasePanel);

        // C. The Bottom Action Bar
        bottomBar = new Table();
        bottomBar.bottom().left().padTop(15);

        // --- Currency Display (Bottom Left) ---
        Table currencyWrapper = new Table();
        Image coinIcon = new Image(
                ImageManager.skin.has("icon-doubloon", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)
                        ? ImageManager.skin.getDrawable("icon-doubloon")
                        : ImageManager.skin.newDrawable("white-pixel", Color.GOLD));

        int currentCurrency = 4500;
        currencyLabel = new Label(String.valueOf(currentCurrency), ImageManager.skin, "menu2");

        currencyWrapper.add(coinIcon).size(32, 32).padRight(10);
        currencyWrapper.add(currencyLabel).left();

        // --- Gacha Buttons (Bottom Right) ---
        buttonWrapper = new Table();
        TextButton pull1Btn = new TextButton("1x Recruit", ImageManager.skin, "boxed-button");
        TextButton pull10Btn = new TextButton("10x Recruit", ImageManager.skin, "boxed-button");

        pull1Btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Triggered 1x Pull!");
                if (Main.currentLocalId != null) {
                    pull1Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                    pull10Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                    String dbBannerId = currentBannerSelection + "_1";
                    NetworkManager.getInstance().pullGacha(Main.currentLocalId, dbBannerId,
                            new NetworkCallback<GachaResult>() {
                                @Override
                                public void onSuccess(final GachaResult result) {
                                    System.out.println(
                                            "Gacha Pulled: " + result.pulledCharId + " (New: " + result.isNew + ")");
                                    if (result.pulledCharId != null && result.pulledCharId.startsWith("W")) {
                                        GameSession.getInstance().getInventory().addItem(result.pulledCharId, 1);
                                        System.out.println("Added Weapon/Equippable to inventory instances.");
                                    } else {
                                        GameSession.getInstance().getParty().unlockCharacter(result.pulledCharId,
                                                ActorFactory.createActor(result.pulledCharId));
                                    }

                                    pull1Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
                                    pull10Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

                                    GachaAnimationScreen animScreen = new GachaAnimationScreen(new Runnable() {
                                        @Override
                                        public void run() {
                                            SceneManager.getInstance().goBack();
                                            com.badlogic.gdx.Screen curr = SceneManager.getInstance().getCurrentScreen();
                                            if (curr instanceof MenuScreen) {
                                                ((MenuScreen) curr).showGachaResults(new String[]{result.pulledCharId});
                                            }
                                        }
                                    });

                                    SceneManager.getInstance().pushScreen(animScreen);
                                    refreshUserCurrency();
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    System.err.println("Gacha Pull Failed: " + errorMessage);
                                    pull1Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
                                    pull10Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
                                }
                            });
                } else {
                    System.err.println("Cannot pull gacha, user is not logged in/no local ID.");
                }
            }
        });

        pull10Btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Triggered 10x Pull!");
                if (Main.currentLocalId != null) {
                    pull1Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                    pull10Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                    String dbBannerId = currentBannerSelection + "_1";
                    NetworkManager.getInstance().pull10Gacha(Main.currentLocalId, dbBannerId,
                            new NetworkCallback<GachaResult[]>() {
                                @Override
                                public void onSuccess(final GachaResult[] results) {
                                    for (GachaResult result : results) {
                                        System.out.println("Gacha Pulled: " + result.pulledCharId + " (New: "
                                                + result.isNew + ")");
                                        GameSession.getInstance().getParty().unlockCharacter(result.pulledCharId,
                                                ActorFactory.createActor(result.pulledCharId));
                                    }

                                    pull1Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
                                    pull10Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

                                    final String[] pulledIds = new String[results.length];
                                    for (int i = 0; i < results.length; i++) {
                                        pulledIds[i] = results[i].pulledCharId;
                                    }

                                    GachaAnimationScreen animScreen = new GachaAnimationScreen(new Runnable() {
                                        @Override
                                        public void run() {
                                            SceneManager.getInstance().goBack();
                                            com.badlogic.gdx.Screen curr = SceneManager.getInstance().getCurrentScreen();
                                            if (curr instanceof MenuScreen) {
                                                ((MenuScreen) curr).showGachaResults(pulledIds);
                                            }
                                        }
                                    });

                                    SceneManager.getInstance().pushScreen(animScreen);
                                    refreshUserCurrency();
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    System.err.println("Gacha Pull Failed: " + errorMessage);
                                    pull1Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
                                    pull10Btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
                                }
                            });
                } else {
                    System.err.println("Cannot pull gacha, user is not logged in/no local ID.");
                }
            }
        });

        buttonWrapper.add(pull1Btn).size(160, 60).padRight(20);
        buttonWrapper.add(pull10Btn).size(160, 60);

        // Assemble bottom bar
        bottomBar.add(currencyWrapper).left().expandX();
        bottomBar.add(buttonWrapper).right();

        // Assemble Right Column
        rightContentColumn.add(mainAreaStack).expand().fill().row();
        rightContentColumn.add(bottomBar).expandX().fillX().padBottom(30).row();

        // ==========================================
        // 3. ASSEMBLE MASTER LAYOUT
        // ==========================================
        this.add(leftRibbonColumn).expandY().fillY().width(maxRibbonWidth);
        this.add(rightContentColumn).expand().fill();

        // Load initial currency
        refreshUserCurrency();

        // Initialize with the first banner
        updateBanner(activeBanners[0]);
    }

    private void refreshUserCurrency() {
        if (Main.currentLocalId != null) {
            NetworkManager.getUserInfo(Main.currentLocalId, new NetworkCallback<UserAuthResponse>() {
                @Override
                public void onSuccess(UserAuthResponse user) {
                    currencyLabel.setText(String.valueOf(user.premiumCurrency));
                }

                @Override
                public void onError(String error) {
                    System.err.println("Failed to fetch user currency: " + error);
                }
            });
        }
    }

    private void updateBanner(String bannerId) {
        currentBannerSelection = bannerId;

        if ("money".equals(bannerId)) {
            bannerContainer.setVisible(false);
            purchasePanel.setVisible(true);
            buttonWrapper.setVisible(false);
            if (statusLabel != null) {
                statusLabel.setText("");
            }
        } else {
            bannerContainer.setVisible(true);
            purchasePanel.setVisible(false);
            buttonWrapper.setVisible(true);

            String assetName = "banner_" + bannerId.toLowerCase();
            if (ImageManager.skin.has(assetName, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                currentBannerImage.setDrawable(ImageManager.skin.getDrawable(assetName));

                // Add a little pop animation whenever the banner is switched!
                currentBannerImage.clearActions();
                currentBannerImage.setScale(0.95f);
                currentBannerImage.setOrigin(Align.center);
                currentBannerImage.addAction(Actions.scaleTo(1f, 1f, 0.3f, Interpolation.swingOut));
            }
        }
    }

    // --- Entrance Animation ---
    public void playEntranceAnimation() {
        this.clearActions();

        leftRibbonColumn.clearActions();
        bannerContainer.clearActions();
        purchasePanel.clearActions();
        bottomBar.clearActions();
        if (rightContentColumn != null) {
            rightContentColumn.clearActions();
        }

        // --- THE TARGETED FIX: Clear and reset the shifting banner image scale state
        // ---
        if (currentBannerImage != null) {
            currentBannerImage.clearActions();
            currentBannerImage.setScale(1f);
        }
        // -------------------------------------------------------------------------------

        this.getColor().a = 1f;
        leftRibbonColumn.getColor().a = 0f;
        bannerContainer.getColor().a = 0f;
        purchasePanel.getColor().a = 0f;
        bottomBar.getColor().a = 0f;

        this.addAction(Actions.sequence(
                Actions.delay(0.02f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        GachaTable.this.invalidate();
                        GachaTable.this.layout();

                        if (rightContentColumn != null) {
                            rightContentColumn.invalidate();
                            rightContentColumn.layout();
                        }
                        if (leftRibbonColumn != null) {
                            leftRibbonColumn.invalidate();
                            leftRibbonColumn.layout();
                        }
                        if (bannerContainer != null) {
                            bannerContainer.invalidate();
                            bannerContainer.layout();
                        }
                        if (purchasePanel != null) {
                            purchasePanel.invalidate();
                            purchasePanel.layout();
                        }
                        if (bottomBar != null) {
                            bottomBar.invalidate();
                            bottomBar.layout();
                        }

                        bannerContainer.setPosition(0, 0);

                        float ribX = leftRibbonColumn.getX();
                        float ribY = leftRibbonColumn.getY();

                        float banX = bannerContainer.getX();
                        float banY = bannerContainer.getY();

                        float purchaseX = purchasePanel.getX();
                        float purchaseY = purchasePanel.getY();

                        float botX = bottomBar.getX();
                        float botY = bottomBar.getY();

                        leftRibbonColumn.setPosition(ribX - 60f, ribY);
                        bannerContainer.setPosition(banX, banY + 60f);
                        purchasePanel.setPosition(purchaseX, purchaseY + 60f);
                        bottomBar.setPosition(botX, botY - 60f);

                        leftRibbonColumn.addAction(Actions.parallel(
                                Actions.fadeIn(0.4f),
                                Actions.moveTo(ribX, ribY, 0.45f, Interpolation.swingOut)));

                        bannerContainer.addAction(Actions.sequence(
                                Actions.delay(0.1f),
                                Actions.parallel(
                                        Actions.fadeIn(0.3f),
                                        Actions.moveTo(banX, banY, 0.55f, Interpolation.swingOut))));

                        purchasePanel.addAction(Actions.sequence(
                                Actions.delay(0.1f),
                                Actions.parallel(
                                        Actions.fadeIn(0.3f),
                                        Actions.moveTo(purchaseX, purchaseY, 0.55f, Interpolation.swingOut))));

                        bottomBar.addAction(Actions.sequence(
                                Actions.delay(0.2f),
                                Actions.parallel(
                                        Actions.fadeIn(0.3f),
                                        Actions.moveTo(botX, botY, 0.55f, Interpolation.swingOut))));
                    }
                })));
    }
}
