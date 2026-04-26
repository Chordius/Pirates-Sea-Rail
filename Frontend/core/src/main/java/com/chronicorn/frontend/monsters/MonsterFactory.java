package com.chronicorn.frontend.monsters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.managers.combatManager.MonsterManager;

import java.util.List;

public class MonsterFactory {

    private Animation<TextureRegion> animZombie;
    private Animation<TextureRegion> animSkeleton;
    private Texture sheetZombie;
    private Texture sheetSkeleton;

    public MonsterFactory() {
        loadAssets();
    }

    private void loadAssets() {
        // Load gambar
        sheetZombie = new Texture("zombie.png");
        sheetSkeleton = new Texture("skeleton.png");

        int frameWidthZombie = sheetZombie.getWidth() / 12;
        int frameHeightZombie = sheetZombie.getHeight() / 8;

        int frameWidthSkeleton = sheetSkeleton.getWidth() / 12;
        int frameHeightSkeleton = sheetSkeleton.getHeight() / 8;

        TextureRegion[][] tmpZ = TextureRegion.split(sheetZombie, frameWidthZombie, frameHeightZombie);
        TextureRegion[][] tmpS = TextureRegion.split(sheetSkeleton, frameWidthSkeleton, frameHeightSkeleton);

        TextureRegion[] zombieFrames = new TextureRegion[3];
        zombieFrames[0] = tmpZ[4][9];
        zombieFrames[1] = tmpZ[4][10];
        zombieFrames[2] = tmpZ[4][11];
        animZombie = new Animation<>(0.2f, zombieFrames);

        TextureRegion[] skeletonFrames = new TextureRegion[3];
        skeletonFrames[0] = tmpS[0][3];
        skeletonFrames[1] = tmpS[0][4];
        skeletonFrames[2] = tmpS[0][5];
        animSkeleton = new Animation<>(0.15f, skeletonFrames);
    }
    public Monster createZombie(float x, float y) {
        return new Zombie(animZombie, x, y);
    }

    public Monster createSkeleton(float x, float y) {
        return new Skeleton(animSkeleton, x, y);
    }

    public Monster createBoss(float x, float y, List<Vector2> tpPoints, MonsterManager manager) {
        return new CultistBoss(x, y, tpPoints, manager);
    }

    public Monster createRandomMonster(float x, float y) {
        if (Math.random() > 0.5) {
            return createZombie(x, y);
        } else {
            return createSkeleton(x, y);
        }
    }

    public void dispose() {
        sheetZombie.dispose();
        sheetSkeleton.dispose();
    }
}
