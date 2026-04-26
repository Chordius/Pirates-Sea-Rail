package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align; // Penting
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.managers.ImageManager;
import com.chronicorn.frontend.managers.NetworkCallback;
import com.chronicorn.frontend.managers.NetworkManager;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.screens.MapScreen;

public class WindowLogin extends WindowBase {

    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;

    public WindowLogin() {
        super("", 0, 0, 400, 400); // Perbesar sedikit ukurannya
        this.center(); // Pastikan window di tengah stage
    }

    @Override
    public void createContents() {
        // Reset layout default window agar rapi
        this.defaults().pad(5).align(Align.center).width(300);

        // --- ROW 1: USERNAME ---
        Label lblUser = new Label("Username:", ImageManager.skin);
        lblUser.setColor(Color.YELLOW);
        this.add(lblUser).align(Align.left).row(); // Label rata kiri

        usernameField = new TextField("", ImageManager.skin);
        this.add(usernameField).height(35).row(); // Input box

        // --- ROW 2: PASSWORD ---
        Label lblPass = new Label("Password:", ImageManager.skin);
        lblPass.setColor(Color.YELLOW);
        this.add(lblPass).align(Align.left).padTop(10).row();

        passwordField = new TextField("", ImageManager.skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        this.add(passwordField).height(35).row();

        // --- ROW 3: STATUS ---
        statusLabel = new Label("", ImageManager.skin);
        statusLabel.setColor(Color.RED);
        statusLabel.setAlignment(Align.center);
        this.add(statusLabel).padTop(10).padBottom(10).row();

        // --- ROW 4: BUTTONS ---
        TextButton btnLogin = new TextButton("LOGIN", ImageManager.skin);
        this.add(btnLogin).height(45).padTop(5).row();

        TextButton btnRegister = new TextButton("REGISTER", ImageManager.skin);
        this.add(btnRegister).height(45).padTop(5).row();

        TextButton btnExit = new TextButton("EXIT GAME", ImageManager.skin);
        this.add(btnExit).height(45).padTop(15).row();

        // --- LISTENERS ---
        btnLogin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                performLogin();
            }
        });

        btnRegister.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                performRegister();
            }
        });

        btnExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    // ... method performLogin dan performRegister TETAP SAMA seperti sebelumnya ...
    private void performLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Username/Password kosong!");
            return;
        }

        statusLabel.setText("Logging in...");
        statusLabel.setColor(Color.YELLOW);

        NetworkManager.getInstance().login(user, pass, new NetworkCallback() {
            @Override
            public void onSuccess(String response) {
                // Sukses
                Main.currentUsername = user;
                GameSession.getInstance().resetSession();
                GameSession.getInstance().startTimer();
                SceneManager.getInstance().pushScreen(new MapScreen());
            }

            @Override
            public void onError(String errorMessage) {
                statusLabel.setText("Login Gagal! Cek Password.");
                statusLabel.setColor(Color.RED);
            }
        });
    }

    private void performRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Isi semua data!");
            return;
        }
        statusLabel.setText("Mendaftar...");

        NetworkManager.getInstance().register(user, pass, new NetworkCallback() {
            @Override
            public void onSuccess(String response) {
                statusLabel.setText("Register Sukses! Silakan Login.");
                statusLabel.setColor(Color.GREEN);
            }

            @Override
            public void onError(String errorMessage) {
                statusLabel.setText("Gagal: Username sudah ada?");
                statusLabel.setColor(Color.RED);
            }
        });
    }
}
