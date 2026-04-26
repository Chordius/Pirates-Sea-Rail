package com.chronicorn.frontend.managers.combatManager;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.projectiles.SkeletonArrow;
import com.chronicorn.frontend.projectiles.BossProjectile;
import java.util.Iterator;

public class ProjectileManager {
    private static final ProjectileManager instance = new ProjectileManager();
    private Array<BossProjectile> projectiles = new Array<>();
    private Array<SkeletonArrow> skeletonArrows = new Array<>();

    private ProjectileManager() {}
    public static ProjectileManager getInstance() { return instance; }

    public void spawnBullet(float x, float y, float angle) {
        projectiles.add(new BossProjectile(x, y, angle));
    }

    public void spawnSkeletonArrow(float x, float y, float targetX, float targetY) {
        skeletonArrows.add(new SkeletonArrow(x, y, targetX, targetY));
    }

    public void update(float delta, Player player) {
        Iterator<BossProjectile> iter = projectiles.iterator();
        while(iter.hasNext()) {
            BossProjectile p = iter.next();
            p.update(delta);
            p.checkCollision(player);
            if(!p.isActive) iter.remove();
        }

        Iterator<SkeletonArrow> arrowIter = skeletonArrows.iterator();
        while(arrowIter.hasNext()) {
            SkeletonArrow arrow = arrowIter.next();
            arrow.update(delta);
            arrow.checkCollision(player);
            if(!arrow.isActive) arrowIter.remove();
        }
    }

    public void draw(SpriteBatch batch) {
        for(BossProjectile p : projectiles) p.draw(batch);
        for(SkeletonArrow arrow : skeletonArrows) arrow.draw(batch);
    }

    public void clear() { projectiles.clear();
        skeletonArrows.clear();}
}
