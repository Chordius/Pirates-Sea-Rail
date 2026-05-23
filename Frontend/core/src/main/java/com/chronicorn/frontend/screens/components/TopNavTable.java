package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.managers.assetManager.ImageManager;

public class TopNavTable extends Table {
    private ButtonGroup<Button> tabGroup; // Changed from TextButton to generic Button
    private TabSelectionListener listener;

    public interface TabSelectionListener {
        void onTabSelected(String tabName);
    }

    public TopNavTable(TabSelectionListener listener) {
        super();
        this.listener = listener;
        buildUI();
    }

    private void buildUI() {
        final Stack masterStack = new Stack();

        // ==========================================
        // LAYER 1: THE YELLOW BACKGROUND (SCROLLING)
        // ==========================================
        Table bgLayer = new Table();
        bgLayer.top();

        // 1. Get the raw drawable and extract its exact pixel dimensions
        final com.badlogic.gdx.scenes.scene2d.utils.Drawable headerDrawable = ImageManager.skin.getDrawable("yellow-nav-bg");
        final float headerWidth = headerDrawable.getMinWidth();
        final float headerHeight = headerDrawable.getMinHeight();

        // 2. Create the custom scrolling group
        WidgetGroup scrollingHeaderGroup = new WidgetGroup() {
            float scrollOffset = 0;
            float scrollSpeed = 40f; // PIXELS PER SECOND: Adjust this value to control the movement rate

            @Override
            public void act(float delta) {
                super.act(delta);
                // Increase the offset based on speed and frame time
                scrollOffset += scrollSpeed * delta;

                // Reset the offset once it reaches the exact width of a single image
                if (scrollOffset >= headerWidth) {
                    scrollOffset %= headerWidth;
                }

                // Position all child images horizontally, starting from the negative offset
                float currentX = -scrollOffset;
                for (com.badlogic.gdx.scenes.scene2d.Actor child : getChildren()) {
                    // Use Math.round to force whole-pixel rendering and prevent 1-pixel gap rendering artifacts
                    child.setPosition(Math.round(currentX), 0);
                    currentX += headerWidth;
                }
            }
        };

        // 3. Add multiple instances of the image to the group
        // Adding 5 instances ensures continuous graphic coverage across high-resolution monitors
        for (int i = 0; i < 5; i++) {
            Image hImg = new Image(headerDrawable);
            hImg.setScaling(com.badlogic.gdx.utils.Scaling.none);
            hImg.setSize(headerWidth, headerHeight);
            scrollingHeaderGroup.addActor(hImg);
        }

        // 4. Add the scrolling group to the background layer
        bgLayer.add(scrollingHeaderGroup).expandX().fillX().height(headerHeight).padTop(30);
        masterStack.add(bgLayer);

        // ==========================================
        // LAYER 2: THE SCROLLING CONTAINER
        // ==========================================
        Table tabContainer = new Table();
        tabContainer.align(Align.left | Align.top);

        tabGroup = new ButtonGroup<>();
        tabGroup.setMaxCheckCount(1);
        tabGroup.setMinCheckCount(1);
        tabGroup.setUncheckLast(true);

        String[] menus = {"Crew", "Items", "Settings", "Gacha"};

        for (String menuName : menus) {
            // 1. Define Visuals
            final Image ribbonImage = new Image(ImageManager.skin.getDrawable("ribbon-drawable"));
            ribbonImage.setSize(138, 240);
            ribbonImage.setVisible(false); // Hide by default

            final Image icon = new Image(ImageManager.skin.getDrawable(menuName.toLowerCase()));

            final Table textWrapper = new Table();
            textWrapper.setBackground(ImageManager.skin.getDrawable("label-bg"));
            Label textLabel = new Label(menuName, ImageManager.skin, "menu");
            textWrapper.add(textLabel).padLeft(15).padRight(15).padTop(2).padBottom(2);
            textWrapper.pack();
            textWrapper.setVisible(false); // Hide by default

            // CRITICAL FOR ANIMATION: You must enable transform to scale a Table in LibGDX
            textWrapper.setTransform(true);
            textWrapper.setOrigin(Align.center); // Forces the scale animation to expand from the middle

            // 2. The Invisible Button Hitbox
            Button.ButtonStyle emptyStyle = new Button.ButtonStyle();
            final Button tabButton = new Button(emptyStyle);
            // REMOVED: tabButton.setFillParent(true); We will size this manually in layout()

            // 3. The Unconstrained Canvas & State Machine
            final WidgetGroup localCanvas = new WidgetGroup() {
                boolean wasChecked = false;
                boolean initialized = false;

                @Override
                public void act(float delta) {
                    super.act(delta);
                    boolean isChecked = tabButton.isChecked();

                    // Detect a state change to trigger animations ONLY once per click
                    if (isChecked != wasChecked || !initialized) {
                        wasChecked = isChecked;
                        initialized = true;

                        float w = getWidth();
                        float baseH = -20 - (getHeight() - 64) / 2f;

                        if (isChecked) {
                            // --- SELECTION ANIMATIONS ---

                            // 1. Ribbon falls down
                            ribbonImage.setVisible(true);
                            ribbonImage.clearActions();
                            float ribbonTargetY = 140 - 240;
                            ribbonImage.setPosition((w - ribbonImage.getWidth()) / 2f, ribbonTargetY + 100); // Start 40px higher
                            ribbonImage.getColor().a = 0f; // Start transparent
                            ribbonImage.addAction(Actions.parallel(
                                Actions.fadeIn(0.01f),
                                Actions.moveTo((w - ribbonImage.getWidth()) / 2f, ribbonTargetY, 0.15f, Interpolation.pow2Out)
                            ));

                            // 2. Text stretches out
                            textWrapper.setVisible(true);
                            textWrapper.clearActions();
                            textWrapper.setPosition((w - textWrapper.getWidth()) / 2f, 60 - 120);
                            textWrapper.setScale(0f, 1f); // Start squished horizontally
                            textWrapper.getColor().a = 0f;
                            textWrapper.addAction(Actions.parallel(
                                Actions.fadeIn(0.2f),
                                Actions.scaleTo(1f, 1f, 0.3f, Interpolation.circleOut)
                            ));

                            // 3. Icon pops up
                            icon.clearActions();
                            icon.addAction(Actions.parallel(
                                Actions.sizeTo(112, 112, 0.25f, Interpolation.circleOut),
                                Actions.moveTo((w - 112) / 2f, baseH + 8, 0.25f, Interpolation.circleOut)
                            ));

                        } else {
                            // --- DESELECTION ANIMATIONS ---
                            ribbonImage.setVisible(false);
                            textWrapper.setVisible(false);

                            // Icon shrinks down into the yellow band
                            icon.clearActions();
                            icon.addAction(Actions.parallel(
                                Actions.sizeTo(84, 84, 0.25f, Interpolation.circleOut),
                                Actions.moveTo((w - 84) / 2f, baseH, 0.25f, Interpolation.circleOut)
                            ));
                        }
                    }
                }

                @Override
                public void layout() {
                    super.layout();
                    float w = getWidth();
                    float baseH = -20 - (getHeight() - 64) / 2f;

                    // HITBOX FIX: The visual icon sits roughly between Y = -33 and Y = 87.
                    // We manually set the invisible button bounds to cover EXACTLY this area.
                    // This kills the unclickable bottom and prevents clicking empty sky above 90!
                    tabButton.setBounds(0, -40, w, 130);

                    // If the window resizes, snap components to correct positions (only if not currently animating)
                    if (!icon.hasActions()) {
                        if (tabButton.isChecked()) {
                            ribbonImage.setPosition((w - ribbonImage.getWidth()) / 2f, 140 - 240);
                            textWrapper.setPosition((w - textWrapper.getWidth()) / 2f, 60 - 120);
                            icon.setSize(112, 112);
                            icon.setPosition((w - 112) / 2f + 2, baseH + 8);
                        } else {
                            icon.setSize(84, 84);
                            icon.setPosition((w - 84) / 2f + 2, baseH);
                        }
                    }
                }
            };

            // Add to the local canvas. Top to bottom order dictates Z-index.
            localCanvas.addActor(ribbonImage);
            localCanvas.addActor(icon);
            localCanvas.addActor(textWrapper);
            localCanvas.addActor(tabButton);

            tabButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (listener != null) listener.onTabSelected(menuName);
                }
            });

            tabGroup.add(tabButton);

            // Add to the Table Layout
            tabContainer.add(localCanvas).height(90).padBottom(120).width(new com.badlogic.gdx.scenes.scene2d.ui.Value() {
                @Override
                public float get(com.badlogic.gdx.scenes.scene2d.Actor context) {
                    float width = masterStack.getWidth();
                    return (width > 0 ? width : Gdx.graphics.getWidth()) / 4f;
                }
            });
        }

        ScrollPane scrollPane = new ScrollPane(tabContainer, ImageManager.skin);
        scrollPane.setScrollingDisabled(false, true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);

        Table scrollLayer = new Table();
        scrollLayer.top();
        scrollLayer.add(scrollPane).expand().fill().padTop(0);

        masterStack.add(scrollLayer);

        this.add(masterStack).expandX().fillX().height(200).top();
    }
}
