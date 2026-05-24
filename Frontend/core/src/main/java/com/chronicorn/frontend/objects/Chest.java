package com.chronicorn.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.eventcommands.CmdWait;
import com.chronicorn.frontend.eventcommands.CmdWindowFlex;
import com.chronicorn.frontend.eventcommands.CmdAddItem;
import com.chronicorn.frontend.items.Item;
import com.chronicorn.frontend.items.ItemDatabase;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.managers.networkManager.NetworkCallback;
import com.chronicorn.frontend.managers.networkManager.NetworkManager;
import com.chronicorn.frontend.managers.networkManager.dto.UserAuthResponse;
import com.chronicorn.frontend.scripts.MapScript;
import com.chronicorn.frontend.utils.SecurityUtils;

public class Chest extends InteractiveObject {
    private boolean isOpen = false;
    private int currencyAmount = 0;
    private String itemId;
    private int itemCount;

    private static Texture chestTexture;
    private static TextureRegion closedRegion;
    private static TextureRegion openRegion;

    public static void loadAssets() {
        if (chestTexture == null) {
            chestTexture = new Texture(Gdx.files.internal("fes_Chest.png"));
            TextureRegion[][] tmp = TextureRegion.split(chestTexture, 48, 48);
            closedRegion = tmp[0][0];
            openRegion = tmp[2][0];
        }
    }

    public Chest(String name, float x, float y, int currencyAmount) {
        this(name, x, y, currencyAmount, null, 0);
    }

    public Chest(String name, float x, float y, int currencyAmount, String itemId, int itemCount) {
        super(name, x, y, 48, 48);
        this.currencyAmount = currencyAmount;
        this.itemId = itemId;
        this.itemCount = itemCount;
        isSolid = true;

        if (chestTexture == null)
            loadAssets();

        // Cek apakah peti ini sudah pernah dibuka (load dari save data)
        if (GameSession.getInstance().isSet(name + "_OPENED")) {
            isOpen = true;
            isSolid = false;
        } else {
            isOpen = false;
            isSolid = true;
        }
        updateVisuals();
    }

    private void updateVisuals() {
        if (isOpen) {
            currentFrame = openRegion;
        } else {
            currentFrame = closedRegion;
        }
    }

    @Override
    public void interact(Player p, EventManager events) {
        if (isOpen)
            return;

        // 1. Ubah status jadi terbuka
        isOpen = true;
        SoundManager.getInstance().playSound("chest.wav");
        updateVisuals();

        // 2. Simpan bahwa chest ini sudah dibuka
        GameSession.getInstance().set(this.name + "_OPENED");

        // 3. Trigger map script if available to override or add behavior
        MapScript currentScript = LevelMapManager.getInstance().getCurrentScript();
        if (currentScript != null) {
            currentScript.onTrigger(this.name, events);
        }

        // 4. Default behavior: Grant currency from Tiled properties
        if (currencyAmount > 0) {
            if (Main.currentLocalId != null) {
                String key = SecurityUtils.generateVerificationKey(Main.currentLocalId, currencyAmount);
                NetworkManager.grantCurrency(Main.currentLocalId, currencyAmount, key,
                        new NetworkCallback<UserAuthResponse>() {
                            @Override
                            public void onSuccess(UserAuthResponse response) {
                                System.out.println("Chest currency reward granted: " + currencyAmount);
                            }

                            @Override
                            public void onError(String error) {
                                System.err.println("Failed to grant chest currency: " + error);
                            }
                        });

                // Show dynamic dialog message
                events.queue(new CmdWait(0.2f));
                events.queue(new CmdWindowFlex()
                        .setText("You obtained " + currencyAmount + " Doubloon!")
                        .setBounds(
                                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1 / 3,
                                Gdx.graphics.getHeight() * 3 / 4,
                                Gdx.graphics.getWidth() * 2 / 3,
                                36 + 18)
                        .setDuration(2f)
                        .setBlocking(true));
                events.queue(new CmdWait(0.5f));
            } else {
                System.err.println("Cannot grant chest currency: player is not logged in.");
            }
        }

        // 5. Default behavior: Grant item from Tiled properties
        if (itemId != null && !itemId.trim().isEmpty() && itemCount > 0) {
            Item item = ItemDatabase.getItem(itemId);
            if (item != null) {
                events.queue(new CmdAddItem(itemId, itemCount));

                events.queue(new CmdWait(0.2f));
                events.queue(new CmdWindowFlex()
                        .setText("You obtained " + itemCount + " " + item.getName() + "!")
                        .setBounds(
                                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1 / 3,
                                Gdx.graphics.getHeight() * 3 / 4,
                                Gdx.graphics.getWidth() * 2 / 3,
                                36 + 18)
                        .setDuration(2f)
                        .setBlocking(true));
                events.queue(new CmdWait(0.5f));
            } else {
                System.err.println("Chest specifies unknown item_id: " + itemId);
            }
        }
    }
}
