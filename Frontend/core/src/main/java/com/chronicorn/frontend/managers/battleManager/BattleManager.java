package com.chronicorn.frontend.managers.battleManager;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.animationManager.ActionSequenceProcessor;
import com.chronicorn.frontend.skills.Action;

import java.util.ArrayList;
import java.util.Comparator;

public class BattleManager {

    public enum TurnState {
        TURN_START, NEXT_BATTLER, INPUT, EXECUTE_ACTION, CHECK_BATTLE_END, BATTLE_END
    }

    private ArrayList<Battler> allBattlers;
    private Array<Battler> turnQueue;
    private ArrayList<Actor> gameParty;
    private ArrayList<Enemy> gameTroop;
    private TurnState currentState;
    private Battler activeBattler;
    private Action selectedAction;

    public BattleManager(ArrayList<Battler> allBattlers) {
        this.allBattlers = allBattlers;
        this.turnQueue = new Array<>();
        this.currentState = TurnState.TURN_START;
        this.gameParty = new ArrayList<>();
        this.gameTroop = new ArrayList<>();

        for (Battler a : allBattlers) {
            if (a instanceof Actor) {
                gameParty.add((Actor) a);
            }
            if (a instanceof Enemy) {
                gameTroop.add((Enemy) a);
            }
        }
    }

    public void update(float delta) {
        switch (currentState) {
            case TURN_START:
                processTurnStart();
                break;
            case NEXT_BATTLER:
                processNextBattler();
                break;
            case INPUT:
                // Logic pauses here. The UI or AI will call submitAction()
                break;
            case EXECUTE_ACTION:
                // Logic pauses here while the screen handles the sequence
                break;
            case CHECK_BATTLE_END:
                checkBattleEnd();
                break;
            case BATTLE_END:
                // Handle victory or defeat
                break;
        }
    }

    private void processTurnStart() {
        turnQueue.clear();

        // Add only alive battlers
        for (Battler b : allBattlers) {
            if (b.isAlive()) {
                turnQueue.add(b);
            }
        }

        // Sort by speed descending
        turnQueue.sort(new Comparator<Battler>() {
            @Override
            public int compare(Battler b1, Battler b2) {
                return Integer.compare(b2.getSpeed(), b1.getSpeed());
            }
        });

        currentState = TurnState.NEXT_BATTLER;
    }

    private void processNextBattler() {
        if (turnQueue.size == 0) {
            currentState = TurnState.TURN_START;
            return;
        }

        activeBattler = turnQueue.removeIndex(0);

        if (!activeBattler.isAlive()) {
            // Skip dead battlers
            return;
        }

        currentState = TurnState.INPUT;

        if (!activeBattler.isPlayerControlled()) {
            // Trigger AI logic immediately
            activeBattler.calculateAI(this);
        }
    }

    public void submitAction(Action action) {
        action.resolveTargets(allBattlers);
        this.selectedAction = action;
        this.currentState = TurnState.EXECUTE_ACTION;
    }

    // Triggered explicitly by the "ACTION EFFECT" string command
    public void triggerActionEffect() {
        selectedAction.execute(); // Damage is calculated, numbers pop up
    }

    // Triggered when the queue is completely empty
    public void finishActionExecution() {
        selectedAction = null;
        currentState = TurnState.CHECK_BATTLE_END;
    }

    private void checkBattleEnd() {
        for (int i = gameTroop.size() - 1; i >= 0; i--) {
            Enemy enemy = gameTroop.get(i);
            if (!enemy.isAlive()) {
                gameTroop.remove(i);
                allBattlers.remove(enemy);
                turnQueue.removeValue(enemy, true);
            }
        }

        boolean playersAlive = false;
        boolean enemiesAlive = false;

        for (Battler b : allBattlers) {
            if (b.isAlive()) {
                if (b.isPlayerControlled()) playersAlive = true;
                else enemiesAlive = true;
            }
        }

        if (!playersAlive || !enemiesAlive) {
            currentState = TurnState.BATTLE_END;
        } else {
            currentState = TurnState.NEXT_BATTLER;
        }
    }

    // --- Added Getters ---

    public TurnState getCurrentState() {
        return currentState;
    }

    public ArrayList<Battler> getAllBattlers() {
        return allBattlers;
    }

    public Battler getActiveBattler() {
        return activeBattler;
    }

    public ArrayList<Actor> getGameParty() {
        return gameParty;
    }

    public ArrayList<Enemy> getGameTroop() {
        return gameTroop;
    }

    public Action getSelectedAction() {
        return selectedAction;
    }

    public ArrayList<Actor> getGamePartyAliveMembers() {
        ArrayList<Actor> aliveMembers = new ArrayList<>();
        for (Actor a : gameParty) {
            if (a.getHp() > 0) {
                aliveMembers.add(a);
            }
        }
        return aliveMembers;
    }

    public ArrayList<Enemy> getGameTroopAliveMembers() {
        ArrayList<Enemy> aliveMembers = new ArrayList<>();
        for (Enemy a : gameTroop) {
            if (a.getHp() > 0) {
                aliveMembers.add(a);
            }
        }
        return aliveMembers;
    }
}
