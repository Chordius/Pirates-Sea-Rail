package com.chronicorn.frontend.managers.animationManager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.screens.BattleScreen;

public class ActionSequenceProcessor {
    private BattleManager battleManager;
    private AnimationManager animationManager;

    private Queue<String> commandQueue;
    private float waitTimer = 0f;
    private int activeAnimations = 0;

    public ActionSequenceProcessor(BattleManager battleManager, AnimationManager animationManager) {
        this.battleManager = battleManager;
        this.animationManager = animationManager;
        this.commandQueue = new Queue<>();
    }

    public void startSequence(Array<String> actionList) {
        commandQueue.clear();
        for (String cmd : actionList) {
            commandQueue.addLast(cmd.trim());
        }
    }

    public void update(float delta) {
        if (waitTimer > 0) {
            waitTimer -= delta;
            return;
        }

        if (commandQueue.isEmpty()) {
            battleManager.finishActionExecution();
            return;
        }

        // FIX: Peek at the next command before consuming it from the queue
        String nextCmd = commandQueue.first().toUpperCase();

        if (nextCmd.equals("WAIT FOR ANIMATION")) {
            if (activeAnimations > 0) {
                return; // Halt execution loop here. The command remains in the queue.
            } else {
                commandQueue.removeFirst(); // Animations are done. Consume the command and move on.
                return;
            }
        }

        String currentCommand = commandQueue.removeFirst();
        executeCommand(currentCommand);
    }

    private void executeCommand(String command) {
        String upperCmd = command.toUpperCase();

        if (upperCmd.startsWith("WAIT:")) {
            String[] parts = command.split(":");
            waitTimer = Float.parseFloat(parts[1].trim());
        }
        else if (upperCmd.startsWith("ANIMATION:")) {
            String[] parts = command.split(":");
            String vfxName = parts[1].trim();

            activeAnimations++;

            // Pass the action and callback directly to the manager
            animationManager.playVFX(vfxName, battleManager.getSelectedAction(), new Runnable() {
                @Override
                public void run() {
                    activeAnimations--;
                }
            });
        }
        else if (upperCmd.equals("ACTION EFFECT")) {
            battleManager.triggerActionEffect();
        }
        else if (upperCmd.equals("PERFORM START")) {
            // TODO: PERFORM START ANIMATION
        }
        else if (upperCmd.equals("PERFORM FINISH")) {
            // TODO: PERFORM FINISH ANIMATION
        }
    }
}
