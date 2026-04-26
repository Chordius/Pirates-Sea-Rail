package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.managers.ImageManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.observers.PlayerObserver;

public class GameHUD extends Table implements PlayerObserver {

    private ProgressBar healthBar;
    private Label healthLabel;
    private DashWidget dashWidget;

    // Komponen Atas
    private Image dividerLine;
    private Label countdownLabel;
    private boolean isCutsceneHidden = false;
    private boolean isPlayerDead = false;

    public GameHUD() {
        this.setFillParent(true);
        createContents();
    }

    private void createContents() {
        // --- 1. BAGIAN ATAS (Tetap Sama) ---
        Table topTable = new Table();
        countdownLabel = new Label("6", ImageManager.skin);
        countdownLabel.setFontScale(1.7f);
        countdownLabel.setColor(Color.WHITE);
        Texture dividerTex = new Texture(Gdx.files.internal("Divider_Line.png"));
        dividerLine = new Image(dividerTex);

        topTable.add(countdownLabel).padBottom(5).row();
        topTable.add(dividerLine).width(380).height(36);

        this.top();
        this.add(topTable).padTop(30).row();

        // --- 2. SPACER ---
        this.add().expand().fill().row();

        // --- 3. BAGIAN BAWAH KIRI (MENGGUNAKAN STACK) ---
        Stack bottomStack = new Stack();

        // A. Setup HP Group (Label + Bar)
        Table hpGroup = new Table();
        healthLabel = new Label("HP 35/35", ImageManager.skin);
        healthLabel.setFontScale(1.25f);
        healthLabel.setColor(Color.WHITE);
        healthLabel.setAlignment(Align.left);

        healthBar = new ProgressBar(0, 35, 1, false, ImageManager.skin, "hp-bar");
        healthBar.setValue(35);
        healthBar.setAnimateDuration(0.25f);

        hpGroup.add(healthLabel).left().padLeft(20).padBottom(2).row();
        hpGroup.add(healthBar).width(396).height(20).left();

        // B. Setup Dash Icon
        dashWidget = new DashWidget();

        // --- PENYUSUNAN LAYER ---

        // LAYER 1 (Belakang): HP Group
        Container<Table> hpContainer = new Container<>(hpGroup);
        hpContainer.align(Align.bottomLeft); // Rata kiri bawah
        hpContainer.padLeft(75).padBottom(12);
        bottomStack.add(hpContainer);

        // LAYER 2 (Depan): Dash Icon
        Container<DashWidget> iconContainer = new Container<>(dashWidget);
        iconContainer.align(Align.bottomLeft);
        iconContainer.size(90, 88);
        bottomStack.add(iconContainer);

        this.add(bottomStack).bottom().left().pad(20);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        int count = GameSession.getInstance().getInt("ROOM_COUNTDOWN");
        countdownLabel.setText(String.valueOf(count));
        if (count <= 3) countdownLabel.setColor(Color.RED);
        else countdownLabel.setColor(Color.WHITE);
    }

    @Override
    public void onHealthChanged(int currentHp, int maxHp) {
        healthBar.setRange(0, maxHp);
        healthBar.setValue(currentHp);
        healthLabel.setText("HP " + currentHp + "/" + maxHp);
    }

    @Override
    public void onDashCooldownChanged(float currentTimer, float maxTime) {
        dashWidget.updateCooldown(currentTimer, maxTime);
    }

    @Override
    public void onPlayerStatusChanged(boolean isDead) {
        this.isPlayerDead = isDead;
        updateVisibility();
    }

    public void setCutsceneHidden(boolean hidden) {
        this.isCutsceneHidden = hidden;
        updateVisibility();
    }

    private void updateVisibility() {
        // Show HUD only if: Player is Alive AND Cutscene is NOT hiding it
        boolean shouldShow = !isPlayerDead && !isCutsceneHidden;
        System.out.println(shouldShow);

        super.setVisible(shouldShow);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
}
