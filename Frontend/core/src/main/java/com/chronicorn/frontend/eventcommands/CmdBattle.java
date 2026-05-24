package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.screens.BattleScreen;

public class CmdBattle implements EventCommand {
    private Array<String> enemyIds;

    /**
     * Creates a new battle transition command.
     * @param enemyIds The list of enemy database IDs to face in battle.
     */
    public CmdBattle(Array<String> enemyIds) {
        this.enemyIds = enemyIds;
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
        SceneManager.getInstance().pushScreen(new BattleScreen(enemyIds));
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isFinished() {
        return true;
    }
}
