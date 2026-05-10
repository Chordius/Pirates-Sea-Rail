package com.chronicorn.frontend.managers.animationManager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.ui.EnemyWidget;

public class ActionSequenceProcessor {
    public interface SequenceEventListener {
        void onCameraZoom(float targetScale, float duration);
        void onCameraReset();
    }

    private BattleManager battleManager;
    private AnimationManager animationManager;
    private SequenceEventListener eventListener;

    private Queue<String> commandQueue;
    private float waitTimer = 0f;
    private int activeAnimations = 0;

    public ActionSequenceProcessor(BattleManager battleManager, AnimationManager animationManager, SequenceEventListener eventListener) {
        this.battleManager = battleManager;
        this.animationManager = animationManager;
        this.eventListener = eventListener;
        this.commandQueue = new Queue<>();
    }

    public void startSequence(Array<String> actionList) {
        commandQueue.clear();
        activeAnimations = 0;
        waitTimer = 0f;

        if (actionList == null || actionList.size == 0) {
            // Fallback: ensure gameplay still progresses even if sequence data is missing.
            commandQueue.addLast("ACTION EFFECT");
            return;
        }

        for (String cmd : actionList) {
            if (cmd != null) {
                commandQueue.addLast(cmd.trim());
            }
        }

        if (commandQueue.isEmpty()) {
            commandQueue.addLast("ACTION EFFECT");
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
            Battler activeUser = battleManager.getActiveBattler();
            if (activeUser instanceof Enemy) {
                EnemyWidget widget = animationManager.findEnemyWidget(activeUser);
                if (widget != null) {
                    widget.playCastAnimation();
                }
            }
        }
        else if (upperCmd.equals("PERFORM FINISH")) {
            // TODO: PERFORM FINISH ANIMATION
            Battler activeUser = battleManager.getActiveBattler();
            if (activeUser instanceof Enemy) {
                EnemyWidget widget = animationManager.findEnemyWidget(activeUser);
                if (widget != null) {
                    widget.resetCastAnimation();
                }
            }
        }
        else if (upperCmd.startsWith("CAMERA ZOOM:")) {
            // Syntax: "CAMERA ZOOM: 1.15: 0.2"
            String[] parts = command.split(":");
            if (parts.length == 3 && eventListener != null) {
                float targetScale = Float.parseFloat(parts[1].trim());
                float duration = Float.parseFloat(parts[2].trim());
                eventListener.onCameraZoom(targetScale, duration);
            }
        }
        else if (upperCmd.equals("CAMERA RESET")) {
            if (eventListener != null) {
                eventListener.onCameraReset();
            }
        }
    }
}
