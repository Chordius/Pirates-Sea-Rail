package com.chronicorn.frontend.managers.combatManager;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.monsters.Monster;
import com.chronicorn.frontend.monsters.CultistBoss; // Jangan lupa import ini

public class CombatManager {

    public void handleCombat(float delta, Player player, Array<Monster> monsters) {
        Rectangle intersection = new Rectangle();

        for (int i = 0; i < monsters.size; i++) {
            Monster m = monsters.get(i);

            // --- LOGIKA GHOST MODE ---

            if (m instanceof CultistBoss) {
                if (((CultistBoss) m).isInvincible()) {
                    continue;
                }
            }

            // Cek Tabrakan Fisik
            if (Intersector.intersectRectangles(player.getBounds(), m.bounds, intersection)) {

                // --- LOGIKA DAMAGE (BUMP SYSTEM) ---
                boolean isRunning = player.isMoving();

                // Hitung Dot Product
                float dx = m.position.x - player.getPosition().x;
                float dy = m.position.y - player.getPosition().y;
                float dotProduct = (player.getVelocity().x * dx) + (player.getVelocity().y * dy);
                boolean isFacingMonster = dotProduct > 0;

                if (isRunning && isFacingMonster) {
                    int damage = player.calculateBumpDamage(m);
                    m.takeDamage(damage);
                }

                // --- LOGIKA FISIKA (KNOCKBACK) ---
                float pushDistMonsterX = 0;
                float pushDistMonsterY = 0;
                float pushDistPlayerX = 0;
                float pushDistPlayerY = 0;

                if (player.isDashing()) {
                    float knockbackForce = 10.0f;
                    if (intersection.width < intersection.height) {
                        pushDistMonsterX = knockbackForce;
                    } else {
                        pushDistMonsterY = knockbackForce;
                    }
                } else {
                    float monsterPushRatio = 1.0f - m.pushResistance;
                    float playerPushRatio = m.pushResistance;
                    if (playerPushRatio < 0.1f) playerPushRatio = 0.1f;

                    if (intersection.width < intersection.height) {
                        pushDistMonsterX = intersection.width * monsterPushRatio;
                        pushDistPlayerX = intersection.width * playerPushRatio;
                    } else {
                        pushDistMonsterY = intersection.height * monsterPushRatio;
                        pushDistPlayerY = intersection.height * playerPushRatio;
                    }
                }

                // Terapkan posisi baru setelah knockback
                if (intersection.width < intersection.height) {
                    if (player.getPosition().x < m.position.x) {
                        m.position.x += pushDistMonsterX;
                        player.getPosition().x -= pushDistPlayerX;
                    } else {
                        m.position.x -= pushDistMonsterX;
                        player.getPosition().x += pushDistPlayerX;
                    }
                } else {
                    if (player.getPosition().y < m.position.y) {
                        m.position.y += pushDistMonsterY;
                        player.getPosition().y -= pushDistPlayerY;
                    } else {
                        m.position.y -= pushDistMonsterY;
                        player.getPosition().y += pushDistPlayerY;
                    }
                }

                // Update bounds (hitbox) setelah posisi berubah
                m.setPosition(m.position.x, m.position.y);
                player.getBounds().setPosition(player.getPosition().x, player.getPosition().y);

                // --- HAPUS MONSTER MATI ---
                if (m.isDead) {
                    monsters.removeIndex(i);
                    i--;
                }
            }
        }
    }
}
