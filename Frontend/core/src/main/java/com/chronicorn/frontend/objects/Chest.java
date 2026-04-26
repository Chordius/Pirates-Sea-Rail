package com.chronicorn.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.eventcommands.CmdShowText;
import com.chronicorn.frontend.eventcommands.CmdWait;
import com.chronicorn.frontend.eventcommands.CmdWindowFlex;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;

public class Chest extends InteractiveObject {
    private boolean isOpen = false;

    // Hardcoded skill
    private final String SKILL_REWARD = "SKILL_DASH";
    private final String MESSAGE = "You obtained the Dash Skill!";

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

    // Constructor kembali sederhana (tanpa parameter flag/message)
    public Chest(String name, float x, float y) {
        super(name, x, y, 48, 48);
        isSolid = true;

        if (chestTexture == null) loadAssets();

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
        if (isOpen) return;

        // 1. Ubah status jadi terbuka
        isOpen = true;
        SoundManager.getInstance().playSound("chest.wav");
        updateVisuals();

        // 2. Simpan bahwa chest ini sudah dibuka
        GameSession.getInstance().set(this.name + "_OPENED");

        // 3. Dapet Dash
        if (!GameSession.getInstance().isSet(SKILL_REWARD)) {
            GameSession.getInstance().set(SKILL_REWARD);
            System.out.println("Skill Obtained: " + SKILL_REWARD);
        }

        // 4. Event Dialog
        events.queue(new CmdWait(0.2f));
        events.queue(new CmdWindowFlex()
            .setText(MESSAGE)
            .setBounds(
                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1 / 3,
                Gdx.graphics.getHeight() * 3 / 4,
                Gdx.graphics.getWidth() * 2 / 3,
                36 + 18
            )
            .setDuration(1f)
            .setBlocking(false)
        );
        events.queue(new CmdWait(1f));
        events.queue(new CmdWindowFlex()
            .setText("Press J to Dash!")
            .setBounds(
                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1 / 3,
                Gdx.graphics.getHeight() * 3 / 4,
                Gdx.graphics.getWidth() * 2 / 3,
                36 + 18
            )
            .setDuration(1f)
            .setBlocking(false)
        );
    }
}
