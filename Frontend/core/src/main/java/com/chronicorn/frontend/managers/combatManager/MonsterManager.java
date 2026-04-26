package com.chronicorn.frontend.managers.combatManager;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.monsters.Monster;
import com.chronicorn.frontend.monsters.MonsterFactory;
import com.chronicorn.frontend.monsters.Zombie;
import com.chronicorn.frontend.monsters.Skeleton;

import java.util.ArrayList;
import java.util.List;

public class MonsterManager {
    private MonsterFactory factory;
    private Array<Monster> monsters;
    private List<Vector2> bossTeleportPoints;
    private List<Vector2> minionSpawnPoints;

    public MonsterManager() {
        this.factory = new MonsterFactory();
        this.monsters = new Array<>();
        this.bossTeleportPoints = new ArrayList<>();
        this.minionSpawnPoints = new ArrayList<>();
    }

    public void spawnFromMap(TiledMap map) {
        monsters.clear();
        bossTeleportPoints.clear();
        minionSpawnPoints.clear();

        Vector2 northPos = null;

        MapLayer logicalLayer = map.getLayers().get("Logic");
        if (logicalLayer == null) logicalLayer = map.getLayers().get("logic");

        if (logicalLayer != null) {
            for (MapObject object : logicalLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    RectangleMapObject rectObj = (RectangleMapObject) object;
                    float x = rectObj.getRectangle().x;
                    float y = rectObj.getRectangle().y;
                    String name = object.getName();

                    if (name != null) {
                        if (name.equalsIgnoreCase("North") || name.equalsIgnoreCase("South") ||
                            name.equalsIgnoreCase("East") || name.equalsIgnoreCase("West")) {
                            bossTeleportPoints.add(new Vector2(x, y));
                            if (name.equalsIgnoreCase("North")) northPos = new Vector2(x, y);
                        }
                    }
                }
            }
        }
        boolean hasBoss = (northPos != null);

        MapLayer entityLayer = map.getLayers().get("Entities");
        if (entityLayer != null) {
            for (MapObject object : entityLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    RectangleMapObject rectObj = (RectangleMapObject) object;
                    float x = rectObj.getRectangle().x;
                    float y = rectObj.getRectangle().y;
                    String name = object.getName();

                    if (name != null) {
                        // --- LOGIKA HYBRID ---

                        if (name.equalsIgnoreCase("Zombie")) {
                            if (hasBoss) {
                                // Level Boss
                                minionSpawnPoints.add(new Vector2(x, y));
                            } else {
                                // Level Biasa
                                spawnZombie(x, y);
                            }
                        }
                        else if (name.equalsIgnoreCase("Skeleton")) {
                            if (hasBoss) {
                                minionSpawnPoints.add(new Vector2(x, y));
                            } else {
                                spawnSkeleton(x, y);
                            }
                        }
                        else if (name.equalsIgnoreCase("MinionSpawn")) {
                            minionSpawnPoints.add(new Vector2(x, y));
                        }
                    }
                }
            }
        }

        if (hasBoss) {
            spawnBoss(northPos.x, northPos.y);
        }

    }

    public void spawnBossMinions(int amount) {
        if (minionSpawnPoints.isEmpty()) return;
        System.out.println("Boss Summoning " + amount + " Minions...");
        for (int i = 0; i < amount; i++) {
            int idx = MathUtils.random(0, minionSpawnPoints.size() - 1);
            Vector2 pos = minionSpawnPoints.get(idx);
            if (MathUtils.randomBoolean()) spawnZombie(pos.x, pos.y);
            else spawnSkeleton(pos.x, pos.y);
        }
    }

    public void update(float delta, Player player) {
        for (int i = 0; i < monsters.size; i++) {
            Monster m = monsters.get(i);
            m.update(delta, player);
            if (m.isDead) {
                monsters.removeIndex(i);
                i--;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (int i = 0; i < monsters.size; i++) {
            Monster m = monsters.get(i);
            if (m != null) m.draw(batch);
        }
    }

    public int getMinionCount() {
        int count = 0;
        for (int i = 0; i < monsters.size; i++) {
            Monster m = monsters.get(i);
            if (m instanceof Zombie || m instanceof Skeleton) count++;
        }
        return count;
    }

    public Array<Monster> getMonsters() { return monsters; }

    public void spawnBoss(float x, float y) {
        monsters.add(factory.createBoss(x, y, bossTeleportPoints, this));
    }
    public void spawnZombie(float x, float y){
        monsters.add(factory.createZombie(x,y));
    }
    public void spawnSkeleton(float x, float y){
        monsters.add(factory.createSkeleton(x,y));
    }
    public void dispose() { factory.dispose(); }
    public void clear() {
        SoundManager.getInstance().stopAllAudio();
        monsters.clear();
    }
}
