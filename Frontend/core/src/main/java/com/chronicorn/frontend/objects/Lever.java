package com.chronicorn.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.eventcommands.CmdWait; // Pastikan import ini
import com.chronicorn.frontend.eventcommands.CmdWindowFlex;
import com.chronicorn.frontend.eventcommands.EventCommand; // Import interface ini
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class Lever extends InteractiveObject {
    private String targetName;
    private boolean isPulled = false;

    private static Texture leverTexture;
    private static TextureRegion closedRegion;
    private static TextureRegion openRegion;

    public static void loadAssets() {
        if (leverTexture == null) {
            leverTexture = new Texture(Gdx.files.internal("fes_Chest.png"));
            TextureRegion[][] tmp = TextureRegion.split(leverTexture, 48, 48);
            closedRegion = tmp[4][1];
            openRegion = tmp[6][2];
        }
    }

    public Lever(String name, float x, float y, String targetName) {
        super(name, x, y, 48, 48);
        this.targetName = targetName;
        isSolid = true;
        if (leverTexture == null) loadAssets();
        updateVisuals();
    }

    private void updateVisuals() {
        if (isPulled) {
            currentFrame = openRegion;
        } else {
            currentFrame = closedRegion;
        }
    }

    @Override
    public void interact(Player p, EventManager events) {
        if (isPulled) return;

        isPulled = true;
        updateVisuals();

        // 1. Suara Lever (Langsung)
        SoundManager.getInstance().playSound("lever.wav");

        InteractiveObject target = LevelMapManager.getInstance().getObjectByName(targetName);

        if (target instanceof Gate) {
            final Gate targetGate = (Gate) target;

            events.queue(new CmdWait(0.2f));

            // 3. Buka Gate
            events.queue(new EventCommand() {
                @Override
                public void start() {
                    targetGate.open(); // Suara 'gate_open.wav' bunyi di sini nanti
                }

                @Override
                public void update(float delta) {} // Tidak butuh update

                @Override
                public boolean isFinished() {
                    return true; // Langsung selesai setelah start()
                }
            });

            // 4. Tampilkan Pesan
            events.queue(new CmdWindowFlex()
                .setText("A Gate has been opened!")
                .setBounds(
                    Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1/3,
                    Gdx.graphics.getHeight() * 3/4,
                    Gdx.graphics.getWidth() * 2/3,
                    54
                )
                .setDuration(3f)
                .setBlocking(false)
            );

        } else {
            System.err.println("Lever Error: Target is not a Gate!");
        }
    }
}
