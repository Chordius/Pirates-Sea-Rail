package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.networkManager.NetworkCallback;
import com.chronicorn.frontend.managers.networkManager.NetworkManager;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.networkManager.dto.UserAuthResponse;
import com.chronicorn.frontend.screens.MapScreen;

public class WindowLogin extends WindowBase {
    private TextField emailField;
    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;

    public WindowLogin() {
        // Adjusted size to better fit the compact, label-less design
        super("", 0, 0, 400, 450);
        this.center();
    }

    @Override
    public void createContents() {
        this.defaults().pad(5).align(Align.center).width(320);

        // --- HEADER (Optional: Mimics the 'Kuro Games' logo area) ---
        Label headerLabel = new Label("ACCOUNT LOGIN", ImageManager.skin);
        headerLabel.setAlignment(Align.center);
        this.add(headerLabel).padBottom(20).row();

        // --- INPUT FIELDS (Using placeholder text instead of external labels) ---
        emailField = new TextField("", ImageManager.skin);
        emailField.setMessageText("Enter email");
        emailField.setAlignment(Align.center); // Optional: Align.left looks more like the image
        this.add(emailField).height(45).padBottom(10).row();

        usernameField = new TextField("", ImageManager.skin);
        usernameField.setMessageText("Enter username");
        usernameField.setAlignment(Align.center);
        this.add(usernameField).height(45).padBottom(10).row();

        passwordField = new TextField("", ImageManager.skin);
        passwordField.setMessageText("Enter password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        passwordField.setAlignment(Align.center);
        this.add(passwordField).height(45).padBottom(15).row();

        // --- SUB-MENU ROW (Exit Left, Register Right) ---
        // A nested table allows two elements to share the same horizontal row
        Table subMenuTable = new Table();

        Label lblExit = new Label("Exit Game", ImageManager.skin);
        lblExit.setColor(Color.LIGHT_GRAY);
        lblExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Label lblRegister = new Label("Register Now", ImageManager.skin);
        lblRegister.setColor(Color.GOLD);
        lblRegister.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                performRegister();
            }
        });

        subMenuTable.add(lblExit).align(Align.left).expandX();
        subMenuTable.add(lblRegister).align(Align.right).expandX();

        this.add(subMenuTable).width(320).padBottom(15).row();

        // --- MAIN LOGIN BUTTON ---
        TextButton btnLogin = new TextButton("Log in", ImageManager.skin);
        btnLogin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                performLogin();
            }
        });
        this.add(btnLogin).width(320).height(50).row();

        // --- STATUS INDICATOR ---
        statusLabel = new Label("", ImageManager.skin);
        statusLabel.setColor(Color.RED);
        statusLabel.setAlignment(Align.center);
        this.add(statusLabel).padTop(15).row();
    }

    private void performLogin() {
        String email = emailField.getText();
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Username/Password kosong!");
            return;
        }

        statusLabel.setText("Logging in...");
        statusLabel.setColor(Color.YELLOW);

        NetworkManager.getInstance().login(email, pass, new NetworkCallback<UserAuthResponse>() {
            @Override
            public void onSuccess(UserAuthResponse result) {
                Main.currentLocalId = result.localUserId;
                GameSession.getInstance().resetSession();
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
        String email = emailField.getText();
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Isi semua data!");
            return;
        }
        statusLabel.setText("Mendaftar...");

        NetworkManager.getInstance().register(email, pass, user, new NetworkCallback<UserAuthResponse>() {
            @Override
            public void onSuccess(UserAuthResponse result) {
                statusLabel.setText("Register Sukses! Silakan Login.");
                Main.currentLocalId = result.localUserId;
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
