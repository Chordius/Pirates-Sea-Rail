package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.ResetManager;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.ImageManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.combatManager.ProjectileManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.screens.MapScreen;
import com.chronicorn.frontend.screens.TitleScreen;

public class WindowOptions extends WindowBase {

    private int focusedIndex = 0;
    private TextButton[] buttons;
    private Player player;

    public WindowOptions(Player player) {
        super(" ",
            (Gdx.graphics.getWidth() / 2) - 150,
            (Gdx.graphics.getHeight() / 2) - 150,
            300,
            300
        );
        this.player = player;
    }

    @Override
    public void createContents() {
        this.clearChildren();
        this.setDebug(false);

        this.padTop(30);
        this.padBottom(30);
        this.defaults().space(15).center();

        // Inisialisasi tombol dengan menyertakan index-nya
        TextButton btnBack = createRpgButton("Back To Game", 0);
        TextButton btnRestart = createRpgButton("Restart Level", 1);
        TextButton btnSettings = createRpgButton("Settings", 2);
        TextButton btnExit = createRpgButton("Exit To Main Menu", 3);

        buttons = new TextButton[]{btnBack, btnRestart, btnSettings, btnExit};

        for (TextButton btn : buttons) {
            this.add(btn).width(280).height(50).row();
        }

        // --- LOGIKA KLIK (MOUSE/ENTER) ---
        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playSound("lever.wav");
                SceneManager.getInstance().goBack();
            }
        });

        btnRestart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playSound("lever.wav");
                if (player != null) {
                    ResetManager.getInstance().restartLevel(player);

                    SceneManager.getInstance().goBack();
                }
            }
        });

        btnSettings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
                SoundManager.getInstance().playSound("lever.wav");
                WindowSettings settings = new WindowSettings(WindowOptions.this, player);
                settings.open();
                getStage().addActor(settings);

                getStage().setKeyboardFocus(settings);
            }
        });

        btnExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playSound("lever.wav");
                SoundManager.getInstance().stopAllAudio();
                SceneManager.getInstance().changeScreen(new TitleScreen());
            }
        });

        // --- LOGIKA NAVIGASI KEYBOARD ---
        this.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                    changeFocus(focusedIndex - 1);
                    return true;
                }
                if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                    changeFocus(focusedIndex + 1);
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    // Simulasikan klik pada tombol yang sedang dipilih
                    InputEvent click = new InputEvent();
                    click.setType(InputEvent.Type.touchDown);
                    buttons[focusedIndex].fire(click);
                    click.setType(InputEvent.Type.touchUp);
                    buttons[focusedIndex].fire(click);
                    return true;
                }
                return false;
            }
        });

        // Jalankan visual pertama kali
        updateVisualFocus();
    }

    private TextButton createRpgButton(String text, final int index) {
        TextButton.TextButtonStyle uniqueStyle = new TextButton.TextButtonStyle(ImageManager.skin.get(TextButton.TextButtonStyle.class));
        final TextButton button = new TextButton(text, uniqueStyle);

        // Listener Mouse Hover agar sinkron dengan keyboard
        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) { // Hanya saat mouse hover (bukan klik/sentuh)
                    focusedIndex = index;
                    updateVisualFocus();
                }
            }
        });
        return button;
    }

    private void changeFocus(int newIndex) {
        // Logika looping menu (dari bawah balik ke atas)
        if (newIndex < 0) newIndex = buttons.length - 1;
        if (newIndex >= buttons.length) newIndex = 0;

        focusedIndex = newIndex;
        updateVisualFocus();
    }

    private void updateVisualFocus() {
        final Drawable selectionBox = ImageManager.skin.getDrawable("selection-box");

        for (int i = 0; i < buttons.length; i++) {
            TextButton btn = buttons[i];
            if (i == focusedIndex) {
                // Aktifkan kotak putih dan kedap-kedip
                btn.getStyle().up = selectionBox;
                btn.clearActions();
                btn.addAction(Actions.forever(
                    Actions.sequence(Actions.alpha(0.5f, 0.4f), Actions.alpha(1.0f, 0.4f))
                ));
            } else {
                // Matikan kotak dan kedap-kedip
                btn.getStyle().up = null;
                btn.clearActions();
                btn.getColor().a = 1f;
            }
        }
    }
}
