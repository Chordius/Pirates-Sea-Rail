package com.chronicorn.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.windows.WindowLogin; // <--- Import WindowLogin
import com.chronicorn.frontend.windows.WindowSettings;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class TitleScreen implements Screen {
    private Stage stage;
    private Table mainTable;
    private Texture titleLogoTexture;
    private Texture userIndicatorTexture;
    private Texture backgroundPanoramaTexture;

    // Variabel Navigasi Keyboard
    private int focusedIndex = 0;
    private TextButton[] buttons;

    public TitleScreen() {
        stage = new Stage(new ScreenViewport());

        try {
            backgroundPanoramaTexture = new Texture(Gdx.files.internal("menu/TitlePanorama.png"));
            backgroundPanoramaTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("TitleScreen", "Background panorama image not found: menu/TitlePanorama.png");
        }

        try {
            titleLogoTexture = new Texture(Gdx.files.internal("titleSeaRail.png"));
            titleLogoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("TitleScreen", "Title text image not found: titleSeaRail.png");
        }

        try {
            userIndicatorTexture = new Texture(Gdx.files.internal("battlehud/userindicator.png"));
            userIndicatorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("TitleScreen", "User indicator image not found: battlehud/userindicator.png");
        }

        SoundManager.getInstance().playMusic("song_intro.mp3");
        setupUI();

        // Setup Input Listener untuk Keyboard
        setupKeyboardNavigation();
    }

    private void setupUI() {
        if (backgroundPanoramaTexture != null) {
            Image backgroundImg = new Image(backgroundPanoramaTexture);
            backgroundImg.setFillParent(true);
            stage.addActor(backgroundImg);
        }

        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // --- TITLE LOGO WITH SPINNING INDICATOR BEHIND IT ---
        Stack titleStack = new Stack();

        if (userIndicatorTexture != null) {
            Image indicatorImg = new Image(userIndicatorTexture);
            indicatorImg.setScaling(Scaling.fit);

            Table indicatorTable = new Table();
            // FIXED: Enabling transform calculations anchors rotations directly to this cell table center point
            indicatorTable.setTransform(true);
            indicatorTable.setOrigin(Align.center);

            indicatorTable.add(indicatorImg).size(320, 320).center();
            // FIXED: Run the looping rotation action on the matrix container table itself instead of the raw child image
            indicatorTable.addAction(Actions.forever(Actions.rotateBy(360f, 10f)));

            titleStack.add(indicatorTable);
        }

        if (titleLogoTexture != null) {
            Image titleImage = new Image(titleLogoTexture);
            titleImage.setScaling(Scaling.fit);

            Table titleTable = new Table();
            titleTable.add(titleImage).width(700).height(200).center();
            titleStack.add(titleTable);
        }

        // FIXED: Increased padBottom from 50 to 90 to lower the buttons underneath by exactly 40px
        mainTable.add(titleStack).padBottom(90).row();

        // --- BUTTONS ---
        boolean isLocal = com.chronicorn.frontend.managers.networkManager.NetworkConfigure.ACTIVE_ENV == com.chronicorn.frontend.managers.networkManager.NetworkConfigure.Environment.LOCAL
            || com.chronicorn.frontend.managers.networkManager.NetworkConfigure.ACTIVE_ENV == com.chronicorn.frontend.managers.networkManager.NetworkConfigure.Environment.OFFLINE;

        if (isLocal) {
            TextButton playButton = createTitleButton("START GAME", 0);
            playButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleStartGameNewGame();
                }
            });

            TextButton resumeButton = createTitleButton("RESUME", 1);
            resumeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleResumeGame();
                }
            });

            buttons = new TextButton[] { playButton, resumeButton };
        } else {
            TextButton playButton = createTitleButton("START GAME", 0);
            playButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleStartGameClick();
                }
            });
            buttons = new TextButton[] { playButton };
        }

        mainTable.add(buttons[0]).width(350).height(70).padBottom(20).row();
        if (isLocal) {
            mainTable.add(buttons[1]).width(350).height(70).row();
        }

        updateVisualFocus();
    }

    private void handleStartGameNewGame() {
        SoundManager.getInstance().playSound("gate.wav");

        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ChronicornSession");
        String savedId = prefs.getString("localUserId", null);

        if (savedId != null && !savedId.isEmpty()) {
            com.chronicorn.frontend.Main.currentLocalId = savedId;
            com.chronicorn.frontend.managers.eventManagers.GameSession.getInstance().resetSession();
            com.chronicorn.frontend.managers.SceneManager.getInstance().pushScreen(new MapScreen(true)); // isNewGame = true
        } else {
            openLoginWindow(false); // isContinue = false (starts new game)
        }
    }

    private void handleResumeGame() {
        SoundManager.getInstance().playSound("gate.wav");

        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ChronicornSession");
        String savedId = prefs.getString("localUserId", null);

        if (savedId != null && !savedId.isEmpty()) {
            com.chronicorn.frontend.Main.currentLocalId = savedId;
            final MapScreen mapScreen = new MapScreen(false); // isNewGame = false
            com.chronicorn.frontend.managers.SaveManager.getInstance().loadGame(new Runnable() {
                @Override
                public void run() {
                    com.chronicorn.frontend.managers.SceneManager.getInstance().pushScreen(mapScreen);
                }
            });
        } else {
            openLoginWindow(true); // isContinue = true (resumes game)
        }
    }

    private void handleStartGameClick() {
        SoundManager.getInstance().playSound("gate.wav");

        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ChronicornSession");
        String savedId = prefs.getString("localUserId", null);
        boolean hasSave = com.chronicorn.frontend.managers.SaveManager.getInstance().hasSaveFile();

        if (savedId != null && !savedId.isEmpty()) {
            com.chronicorn.frontend.Main.currentLocalId = savedId;
            if (hasSave) {
                // Continue saved game
                final MapScreen mapScreen = new MapScreen(false); // isNewGame = false
                com.chronicorn.frontend.managers.SaveManager.getInstance().loadGame(new Runnable() {
                    @Override
                    public void run() {
                        com.chronicorn.frontend.managers.SceneManager.getInstance().pushScreen(mapScreen);
                    }
                });
            } else {
                // Start new game
                com.chronicorn.frontend.managers.eventManagers.GameSession.getInstance().resetSession();
                com.chronicorn.frontend.managers.SceneManager.getInstance().pushScreen(new MapScreen(true)); // isNewGame = true
            }
        } else {
            openLoginWindow(hasSave);
        }
    }

    // --- METHOD BARU: BUKA LOGIN ---
    private void openLoginWindow(boolean isContinue) {
        // Sembunyikan menu utama agar tidak tumpang tindih
        mainTable.setVisible(false);

        // Buat Window Login
        WindowLogin loginWindow = new WindowLogin(isContinue);
        loginWindow.open();

        // Letakkan di tengah layar
        loginWindow.setPosition(
                (Gdx.graphics.getWidth() - loginWindow.getWidth()) / 2,
                (Gdx.graphics.getHeight() - loginWindow.getHeight()) / 2);

        // Tambahkan ke stage
        stage.addActor(loginWindow);
    }

    // --- METHOD: BUKA SETTINGS (Sudah ada tapi dirapikan) ---
    private void openSettings() {
        mainTable.setVisible(false);

        // Gunakan logika callback agar bisa kembali ke menu utama
        WindowSettings settingsWindow = new WindowSettings(new Runnable() {
            @Override
            public void run() {
                // Saat tombol Back ditekan di Settings:
                mainTable.setVisible(true);
                Gdx.input.setInputProcessor(stage);
                stage.setKeyboardFocus(null);
                updateVisualFocus();
            }
        });

        stage.addActor(settingsWindow);
        stage.setKeyboardFocus(settingsWindow);
    }

    // Helper untuk membuat tombol
    private TextButton createTitleButton(String text, final int index) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = ImageManager.skin.get("menu3", Label.LabelStyle.class).font;
        style.fontColor = Color.WHITE;

        TextButton button = new TextButton(text, style);

        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) {
                    focusedIndex = index;
                    updateVisualFocus();
                }
            }
        });
        return button;
    }

    private void setupKeyboardNavigation() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (mainTable.isVisible()) {
                    if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                        changeFocus(focusedIndex - 1);
                        return true;
                    }
                    if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                        changeFocus(focusedIndex + 1);
                        return true;
                    }
                    if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                        InputEvent click = new InputEvent();
                        click.setType(InputEvent.Type.touchDown);
                        buttons[focusedIndex].fire(click);
                        click.setType(InputEvent.Type.touchUp);
                        buttons[focusedIndex].fire(click);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void changeFocus(int newIndex) {
        if (newIndex < 0)
            newIndex = buttons.length - 1;
        if (newIndex >= buttons.length)
            newIndex = 0;
        focusedIndex = newIndex;
        updateVisualFocus();
    }

    private void updateVisualFocus() {
        for (int i = 0; i < buttons.length; i++) {
            if (i == focusedIndex) {
                buttons[i].getLabel().setColor(Color.YELLOW);
                buttons[i].clearActions();
                buttons[i].addAction(Actions.forever(
                    Actions.sequence(Actions.alpha(0.5f, 0.4f), Actions.alpha(1.0f, 0.4f))));
            } else {
                // FIXED: Replaces Color.WHITE with your custom fallback hexadecimal color value
                buttons[i].getLabel().setColor(Color.valueOf("CFC7B3"));
                buttons[i].clearActions();
                buttons[i].getColor().a = 1f;
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (titleLogoTexture != null)
            titleLogoTexture.dispose();
        if (userIndicatorTexture != null)
            userIndicatorTexture.dispose();
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
}
