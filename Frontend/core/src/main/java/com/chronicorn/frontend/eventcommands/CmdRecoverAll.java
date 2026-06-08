package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

import java.util.List;

public class CmdRecoverAll implements EventCommand {

    @Override
    public void start() {
        // 1. Recover Overworld Player
        Player player = LevelMapManager.getInstance().getPlayer();
        if (player != null) {
            player.recoverAll();
        }

        // 2. Recover all party members (Actors)
        List<Actor> party = GameSession.getInstance().getParty().getActivePartyActors();
        for (Actor actor : party) {
            if (actor != null) {
                // Fully heal HP
                actor.heal(actor.getMaxHp(), com.chronicorn.frontend.managers.battleManager.enums.Elements.NONE);
                // Fully recover energy/mana
                actor.energyChange(actor.getMaxEnergy());
                // Clear debuffs / active status effects
                actor.getActiveStates().clear();
            }
        }
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
