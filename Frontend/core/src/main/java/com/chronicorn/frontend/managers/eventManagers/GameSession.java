package com.chronicorn.frontend.managers.eventManagers;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

public class GameSession {
    private static GameSession instance = new GameSession();

    // 1. Switches
    private ObjectSet<String> flags = new ObjectSet<>();
    private ObjectSet<String> snapshotFlags = new ObjectSet<>();

    // 2. Variables
    private ObjectMap<String, Object> variables = new ObjectMap<>();
    private ObjectMap<String, Object> snapshotVariables = new ObjectMap<>();

    // Speedrun timer
    private float timer = 0f;
    private boolean isTimerRunning = false;

    public static GameSession getInstance() {
        return instance;
    }

    // --- TIMER LOGIC ---
    public void update(float delta) {
        if (isTimerRunning) {
            timer += delta;
        }
    }

    public void startTimer() { isTimerRunning = true; }
    public void stopTimer() { isTimerRunning = false; }

    public void resetSession() {
        flags.clear();
        variables.clear();
        timer = 0f;
        isTimerRunning = false;
    }

    public String getFormattedTime() {
        int minutes = (int) timer / 60;
        int seconds = (int) timer % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getScoreInt() {
        return (int) timer;
    }

    // --- SWITCHES ---
    public boolean isSet(String flag) {
        return flags.contains(flag);
    }

    public void set(String flag) {
        flags.add(flag);
    }

    public void unset(String flag) {
        flags.remove(flag);
    }

    // --- VARIABLES ---
    public void setVar(String key, Object value) {
        variables.put(key, value);
    }

    public Object getVar(String key) {
        return variables.get(key);
    }

    public int getInt(String key) {
        Object val = variables.get(key);
        if (val instanceof Integer) {
            return (Integer) val;
        }
        return 0; // Default value if null or not an int
    }

    // --- SNAPSHOT SYSTEM ---

    public void createSnapshot() {
        // 1. Save Flags
        snapshotFlags.clear();
        snapshotFlags.addAll(flags);

        // 2. Save Variables
        snapshotVariables.clear();
        snapshotVariables.putAll(variables);

        System.out.println("GameSession: Snapshot created.");
        System.out.println("- Flags: " + flags.size);
        System.out.println("- Vars: " + variables.size);
    }

    public void restoreSnapshot() {
        // 1. Restore Flags
        flags.clear();
        flags.addAll(snapshotFlags);

        // 2. Restore Variables
        variables.clear();
        variables.putAll(snapshotVariables);

        System.out.println("GameSession: Snapshot restored.");
    }

    public void reset() {
        flags.clear();
        snapshotFlags.clear();
        variables.clear();
        snapshotVariables.clear();
    }
}
