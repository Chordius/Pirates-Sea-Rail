package com.chronicorn.frontend.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.contants.Direction;
import com.chronicorn.frontend.eventcommands.CmdBattle;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class EnemyMapEvent extends MapEvent {

    private String enemyGroupId;

    // Position/Defeated tracking
    private Vector2 startPosition;
    private boolean isDefeated = false;
    private float respawnTimer = 0f;

    // Chase AI constants
    private float chaseTimer = 0f;
    private static final float CHASE_INTERVAL = 0.2f;
    private static final float CHASE_RANGE = 240f; // 5 tiles range (5 * 48)

    public EnemyMapEvent(String name, float x, float y, float width, float height, String enemyGroupId) {
        super(name, x, y, width, height);
        this.enemyGroupId = enemyGroupId;
        this.isSolid = true; // They block the player
        this.startPosition = new Vector2(x, y);
    }

    public void setDefeated(boolean defeated) {
        this.isDefeated = defeated;
        if (defeated) {
            this.isSolid = false;
            this.respawnTimer = 180f; // 3 minutes
        } else {
            this.isSolid = true;
            this.x = startPosition.x;
            this.y = startPosition.y;
            // MapEvent uses targetPosition for smooth grid movement interpolation
            if (this.targetPosition != null) {
                this.targetPosition.set(startPosition);
            }
            if (this.bounds != null) {
                this.bounds.setPosition(startPosition.x, startPosition.y);
            }
            this.isMoving = false;
        }
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (isDefeated)
            return;
        super.render(batch);
    }

    @Override
    public void update(float delta) {
        if (isDefeated) {
            respawnTimer -= delta;
            if (respawnTimer <= 0) {
                setDefeated(false);
            }
            return;
        }

        // If we are currently moving, let the standard MapEvent update handle the
        // smooth sliding movement.
        if (isMoving) {
            super.update(delta);
            return;
        }

        // If the game/event manager is busy, don't update/move.
        boolean isGameBusy = LevelMapManager.getInstance().getEventManager() != null
                && LevelMapManager.getInstance().getEventManager().isBusy();
        if (isGameBusy) {
            super.update(delta); // Let standard idle animation tick, but no movement.
            return;
        }

        Player player = LevelMapManager.getInstance().getPlayer();
        if (player == null) {
            super.update(delta);
            return;
        }

        float dx = player.getPosition().x - this.x;
        float dy = player.getPosition().y - this.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= CHASE_RANGE) {
            // Chase mode!
            chaseTimer -= delta;
            if (chaseTimer <= 0) {
                chaseTimer = CHASE_INTERVAL;

                // Let's decide which way to step to get closer to the player.
                int horizontalDir = dx > 0 ? 2 : 1; // 2: RIGHT, 1: LEFT
                int verticalDir = dy > 0 ? 3 : 0; // 3: UP, 0: DOWN

                // If close enough to player (touching / near), trigger battle, don't move into
                // them
                if (distance <= 52f) { // slightly more than 48 to count adjacency
                    // We are right next to the player. Just face them!
                    if (Math.abs(dx) > Math.abs(dy)) {
                        turn(horizontalDir);
                    } else {
                        turn(verticalDir);
                    }
                    return;
                }

                // Determine primary axis of movement
                boolean moveHorizontalFirst = Math.abs(dx) > Math.abs(dy);

                boolean stepTaken = false;
                if (moveHorizontalFirst) {
                    stepTaken = tryChaseStep(horizontalDir);
                    if (!stepTaken) {
                        stepTaken = tryChaseStep(verticalDir);
                    }
                } else {
                    stepTaken = tryChaseStep(verticalDir);
                    if (!stepTaken) {
                        stepTaken = tryChaseStep(horizontalDir);
                    }
                }

                if (!stepTaken) {
                    // Face the player if blocked completely
                    if (moveHorizontalFirst) {
                        turn(horizontalDir);
                    } else {
                        turn(verticalDir);
                    }
                }
            }
            // Make sure the walking/idle animation is updated correctly by calling
            // super.update
            super.update(delta);
        } else {
            // Not in range. Do normal autonomous walk/idle from MapEvent.
            super.update(delta);
        }
    }

    private boolean tryChaseStep(int direction) {
        float TILE_SIZE = 48f;
        float nextX = this.x;
        float nextY = this.y;
        switch (direction) {
            case 0:
                nextY -= TILE_SIZE;
                break; // Down
            case 1:
                nextX -= TILE_SIZE;
                break; // Left
            case 2:
                nextX += TILE_SIZE;
                break; // Right
            case 3:
                nextY += TILE_SIZE;
                break; // Up
        }

        // Check collision
        boolean blocked = LevelMapManager.getInstance().isAreaBlocked(nextX, nextY, this.width, this.height, this);
        if (!blocked) {
            moveGrid(direction, TILE_SIZE);
            return true;
        }
        return false;
    }

    private boolean isFaceToFace(Direction playerDir, int enemyDir) {
        // player directions: LEFT, RIGHT, DOWN, UP
        // enemy directions: 0: DOWN, 1: LEFT, 2: RIGHT, 3: UP
        if (playerDir == Direction.UP && enemyDir == 0)
            return true;
        if (playerDir == Direction.DOWN && enemyDir == 3)
            return true;
        if (playerDir == Direction.LEFT && enemyDir == 2)
            return true;
        if (playerDir == Direction.RIGHT && enemyDir == 1)
            return true;
        return false;
    }

    // Normal encounter (They touched you, or you walked into them)
    @Override
    public void interact(Player player, EventManager events) {
        if (events.isBusy())
            return;

        // Calculate player advantage
        boolean advantage = !isFaceToFace(player.getCurrentDirection(), getCurrentDirection());

        System.out.println("Battle Start: " + enemyGroupId + " (Advantage: " + advantage + ")");

        Array<String> ids = new Array<>();
        if (enemyGroupId != null) {
            String[] tokens = enemyGroupId.split(",");
            for (String t : tokens) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    ids.add(trimmed);
                }
            }
        }
        if (ids.size == 0) {
            ids.add("pirate");
        }

        events.queue(new CmdBattle(ids, advantage, getName()));
    }

    // Advantage encounter (Legacy weapon swing strike) - deprecated but kept for
    // compatibility
    public void strikeAdvantage(Player player, EventManager events) {
        interact(player, events);
    }
}
