package com.chronicorn.frontend.managers.battleManager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.skills.Action;
import com.chronicorn.frontend.skills.ReactionAction;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.SkillDatabase;

import java.util.ArrayList;
import java.util.Comparator;

public class BattleManager {

    public enum TurnState {
        TURN_START, NEXT_BATTLER, INPUT, EXECUTE_ACTION, CHECK_BATTLE_END, BATTLE_END
    }
    BattleDelegate followUpDelegate;

    private ArrayList<Battler> allBattlers;
    private Array<Battler> turnQueue;
    private ArrayList<Actor> gameParty;
    private ArrayList<Enemy> gameTroop;
    private Queue<Action> followUpQueue;
    private TurnState currentState;
    private Battler activeBattler;
    private Action selectedAction;
    private int turn;

    public BattleManager(ArrayList<Battler> allBattlers) {
        this.allBattlers = allBattlers;
        this.turnQueue = new Array<>();
        this.currentState = TurnState.TURN_START;
        this.gameParty = new ArrayList<>();
        this.gameTroop = new ArrayList<>();
        this.followUpQueue = new Queue<>();

        followUpDelegate = new BattleDelegate() {
            @Override
            public void onFollowUpRequested(Battler source, String skillId, Battler target) {
                // Construct the action here at the engine level
                Skill followUpSkill = SkillDatabase.get(skillId);
                Action action = new Action(source);
                action.setSkill(followUpSkill);
                action.setPrimaryTarget(target);

                // We have access to allBattlers here
                action.resolveTargets(BattleManager.this.allBattlers);

                queueFollowUp(action);
            }

            @Override
            public void onReactionRequested(String reactionSkillId, Battler centerTarget, int flatDamage) {
                Skill reactionData = SkillDatabase.get(reactionSkillId);
                ReactionAction reaction = new ReactionAction(reactionData, centerTarget, flatDamage);

                // Resolve who gets hit based on the JSON scope (e.g., ALL_ENEMIES)
                reaction.resolveTargets(BattleManager.this.allBattlers);

                // Push it to the front or back of the queue
                queueFollowUp(reaction);
            }
        };

        for (Battler a : allBattlers) {
            a.setBattleDelegate(followUpDelegate);

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
                b.triggerTurnStart();
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
            processTurnEnd();
            currentState = TurnState.TURN_START;
            return;
        }

        activeBattler = turnQueue.removeIndex(0);

        if (!activeBattler.isAlive()) {
            // Skip dead battlers
            return;
        }

        activeBattler.triggerActionStart();

        // If a start-of-turn effect killed the battler, skip their input and check for death
        if (!activeBattler.isAlive()) {
            currentState = TurnState.CHECK_BATTLE_END;
            return;
        }

        currentState = TurnState.INPUT;

        if (!activeBattler.isPlayerControlled()) {
            // Trigger AI logic immediately
            activeBattler.calculateAI(this);
        }
    }

    public void processTurnEnd() {
        turn++;
        for (Battler b : allBattlers) {
            if (b.isAlive()) {
                b.triggerTurnEnd();
            }
        }
        System.out.println("Current Turn: " + turn);
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
        if (activeBattler != null && activeBattler.isAlive() && followUpQueue.isEmpty()) {
            activeBattler.triggerActionEnd();
        }

        if (!followUpQueue.isEmpty()) {
            // Pop the follow-up, set it as the active action, and execute it immediately
            selectedAction = followUpQueue.removeFirst();
            currentState = TurnState.EXECUTE_ACTION;
            return; // Skip checking battle end until the follow-up finishes
        }

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

    public void queueFollowUp(Action followUpAction) {
        this.followUpQueue.addLast(followUpAction);
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
