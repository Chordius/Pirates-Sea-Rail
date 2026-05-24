package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.screens.BattleScreen;

public class CmdBattle implements EventCommand {
    private Array<String> enemyIds;
    private boolean playerAdvantage = false;
    private String eventName;

    /**
     * Creates a new battle transition command.
     * @param enemyIds The list of enemy database IDs to face in battle.
     */
    public CmdBattle(Array<String> enemyIds) {
        this.enemyIds = enemyIds;
    }

    /**
     * Creates a new battle transition command with advantage flag.
     * @param enemyIds The list of enemy database IDs to face in battle.
     * @param playerAdvantage True if player touches from side/behind.
     */
    public CmdBattle(Array<String> enemyIds, boolean playerAdvantage) {
        this.enemyIds = enemyIds;
        this.playerAdvantage = playerAdvantage;
    }

    /**
     * Creates a new battle transition command with advantage flag and event name.
     * @param enemyIds The list of enemy database IDs to face in battle.
     * @param playerAdvantage True if player touches from side/behind.
     * @param eventName Name of the MapEvent triggering the battle.
     */
    public CmdBattle(Array<String> enemyIds, boolean playerAdvantage, String eventName) {
        this.enemyIds = enemyIds;
        this.playerAdvantage = playerAdvantage;
        this.eventName = eventName;
    }

    /**
     * Creates a new battle transition command.
     * @param enemyIds Varargs list of enemy database IDs.
     */
    public CmdBattle(String... enemyIds) {
        this.enemyIds = new Array<>(enemyIds);
    }

    @Override
    public void start() {
        SceneManager.getInstance().pushScreen(new BattleScreen(enemyIds, playerAdvantage, eventName));
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isFinished() {
        return true;
    }
}
