package com.chronicorn.frontend.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.items.Equippable;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.managers.networkManager.NetworkCallback;
import com.chronicorn.frontend.managers.networkManager.NetworkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveManager {
    private static final SaveManager instance = new SaveManager();
    private static final String SAVE_FILE_NAME = "save.json";
    private final Json json;

    private SaveManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setIgnoreUnknownFields(true);
    }

    public static SaveManager getInstance() {
        return instance;
    }

    public boolean hasSaveFile() {
        return Gdx.files.local(SAVE_FILE_NAME).exists();
    }

    public void saveGame() {
        try {
            SaveData saveData = new SaveData();
            LevelMapManager mapManager = LevelMapManager.getInstance();
            saveData.currentMapName = mapManager.getCurrentMapName();

            Player player = mapManager.getPlayer();
            if (player != null) {
                saveData.playerX = player.getPosition().x / 48f;
                saveData.playerY = mapManager.getMapHeight() - 1 - (player.getPosition().y / 48f);
                saveData.playerDirection = player.getCurrentDirection().name();
                saveData.playerHp = player.hp;
            }

            GameSession session = GameSession.getInstance();

            // 1. Flags
            saveData.flags = new Array<>();
            if (session.getFlags() != null) {
                for (String flag : session.getFlags()) {
                    saveData.flags.add(flag);
                }
            }

            // 2. Variables
            saveData.variables = new ObjectMap<>();
            if (session.getVariables() != null) {
                for (ObjectMap.Entry<String, Object> entry : session.getVariables().entries()) {
                    saveData.variables.put(entry.key, entry.value);
                }
            }

            // 3. Inventory Consumables
            saveData.inventoryConsumables = new HashMap<>();
            if (session.getInventory() != null && session.getInventory().getAllItems() != null) {
                saveData.inventoryConsumables.putAll(session.getInventory().getAllItems());
            }

            // 4. Inventory Equipments
            saveData.ownedEquipments = new Array<>();
            Map<Equippable, Integer> equipToIndex = new HashMap<>();
            if (session.getInventory() != null && session.getInventory().getOwnedEquipments() != null) {
                Array<Equippable> ownedEquips = session.getInventory().getOwnedEquipments();
                for (int i = 0; i < ownedEquips.size; i++) {
                    Equippable eq = ownedEquips.get(i);
                    equipToIndex.put(eq, i);

                    EquippableSaveData eqSave = new EquippableSaveData();
                    eqSave.id = eq.getId();
                    eqSave.fixedStat = eq.getFixedStat().name();
                    eqSave.fixedValue = eq.getFixedValue();
                    eqSave.randomStat = eq.getRandomStat().name();
                    eqSave.randomPercentBonus = eq.getRandomPercentBonus();
                    saveData.ownedEquipments.add(eqSave);
                }
            }

            // 5. Party Characters
            saveData.ownedCharacters = new ObjectMap<>();
            if (session.getParty() != null && session.getParty().getOwnedCharacters() != null) {
                for (Map.Entry<String, Actor> entry : session.getParty().getOwnedCharacters().entrySet()) {
                    String charId = entry.getKey();
                    Actor actor = entry.getValue();

                    ActorSaveData actorSave = new ActorSaveData();
                    actorSave.charId = charId;
                    actorSave.level = actor.getLevel();
                    actorSave.hp = actor.getHp();
                    actorSave.equippedItemIndices = new Array<>();

                    if (actor.getEquipments() != null) {
                        for (Equippable eq : actor.getEquipments()) {
                            if (equipToIndex.containsKey(eq)) {
                                actorSave.equippedItemIndices.add(equipToIndex.get(eq));
                            }
                        }
                    }
                    saveData.ownedCharacters.put(charId, actorSave);
                }
            }

            // 6. Active Party
            if (session.getParty() != null && session.getParty().getActivePartyIdsArray() != null) {
                String[] activeIds = session.getParty().getActivePartyIdsArray();
                saveData.activePartyIds = new String[activeIds.length];
                System.arraycopy(activeIds, 0, saveData.activePartyIds, 0, activeIds.length);
            }

            // Serialize & write to file
            FileHandle file = Gdx.files.local(SAVE_FILE_NAME);
            file.writeString(json.toJson(saveData), false);
            System.out.println("SaveManager: Game saved successfully to " + file.file().getAbsolutePath());
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error saving game: " + e.getMessage(), e);
        }
    }

    public void loadGame(final Runnable loadCallback) {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE_NAME);
            if (!file.exists()) {
                System.out.println("SaveManager: No save file found to load.");
                return;
            }

            final SaveData saveData = json.fromJson(SaveData.class, file.readString());
            if (saveData == null) {
                System.out.println("SaveManager: Save data is empty or invalid.");
                return;
            }

            // Prepare list of characters to verify with backend
            final List<String> charactersToVerify = new ArrayList<>();
            if (saveData.ownedCharacters != null) {
                for (String charId : saveData.ownedCharacters.keys()) {
                    charactersToVerify.add(charId);
                }
            }

            if (charactersToVerify.isEmpty()) {
                // Nothing to verify, proceed directly
                applySaveDataAndStart(saveData, new ArrayList<>(), loadCallback);
                return;
            }

            final String[] charIdsArray = charactersToVerify.toArray(new String[0]);

            // Single bulk verification request
            NetworkManager.verifyParty(Main.currentLocalId, charIdsArray, new NetworkCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean allVerified) {
                    if (allVerified != null && allVerified) {
                        // Bulk verification succeeded! All characters are owned.
                        System.out.println("SaveManager: Bulk verification succeeded for all characters.");
                        applySaveDataAndStart(saveData, charactersToVerify, loadCallback);
                    } else {
                        // Verification failed for at least one character. Fallback to individual checks.
                        System.out.println("SaveManager: Bulk verification returned false. Checking characters individually...");
                        verifyCharactersIndividually(saveData, charactersToVerify, loadCallback);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Check if it's an explicit 403 / unowned check, otherwise it is a network failure
                    if (errorMessage != null && errorMessage.contains("Error 403")) {
                        System.out.println("SaveManager: Bulk verification returned 403. Checking characters individually...");
                        verifyCharactersIndividually(saveData, charactersToVerify, loadCallback);
                    } else {
                        // Network error! Do NOT prune characters. Trust the local save.
                        System.err.println("SaveManager: Network error during bulk verification: " + errorMessage + ". Trusting local save.");
                        applySaveDataAndStart(saveData, charactersToVerify, loadCallback);
                    }
                }
            });

        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error loading game: " + e.getMessage(), e);
        }
    }

    private void verifyCharactersIndividually(final SaveData saveData, final List<String> charactersToVerify, final Runnable loadCallback) {
        final List<String> verifiedOwnedIds = new ArrayList<>();
        final int totalChecks = charactersToVerify.size();
        final int[] completedChecks = { 0 };

        for (final String charId : charactersToVerify) {
            NetworkManager.verifyParty(Main.currentLocalId, new String[] { charId },
                    new NetworkCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (result != null && result) {
                                synchronized (verifiedOwnedIds) {
                                    verifiedOwnedIds.add(charId);
                                }
                            } else {
                                System.out.println(
                                        "SaveManager: Character " + charId + " is illegal (failed verification).");
                            }
                            checkCompletion();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // If it's a network error (not 403), trust the local save for this character
                            if (errorMessage != null && !errorMessage.contains("Error 403")) {
                                System.err.println("SaveManager: Network error during individual verification for " + charId + ": " + errorMessage + ". Keeping character.");
                                synchronized (verifiedOwnedIds) {
                                    verifiedOwnedIds.add(charId);
                                }
                            } else {
                                System.err.println("SaveManager: Verification failed (unowned) for character " + charId + ": " + errorMessage);
                            }
                            checkCompletion();
                        }

                        private void checkCompletion() {
                            boolean allDone = false;
                            synchronized (completedChecks) {
                                completedChecks[0]++;
                                if (completedChecks[0] == totalChecks) {
                                    allDone = true;
                                }
                            }
                            if (allDone) {
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        applySaveDataAndStart(saveData, verifiedOwnedIds, loadCallback);
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private void applySaveDataAndStart(SaveData saveData, List<String> verifiedOwnedIds, Runnable loadCallback) {
        try {
            // 1. Restore GameSession switches (flags) and variables first
            GameSession session = GameSession.getInstance();
            session.reset();

            if (saveData.flags != null) {
                for (String flag : saveData.flags) {
                    session.set(flag);
                }
            }
            if (saveData.variables != null) {
                for (ObjectMap.Entry<String, Object> entry : saveData.variables.entries()) {
                    session.setVar(entry.key, entry.value);
                }
            }

            // 2. Restore map position & direction
            if (saveData.currentMapName != null) {
                LevelMapManager.getInstance().changeLevel(saveData.currentMapName, saveData.playerX, saveData.playerY);
            }
            Player player = LevelMapManager.getInstance().getPlayer();
            if (player != null) {
                if (saveData.playerDirection != null) {
                    try {
                        player.setCurrentDirection(
                                com.chronicorn.frontend.contants.Direction.valueOf(saveData.playerDirection));
                    } catch (Exception ignored) {
                    }
                }
                player.hp = saveData.playerHp;
            }

            // 3. Restore Inventory Consumables
            session.inventory = new com.chronicorn.frontend.managers.systems.PlayerInventory();
            if (saveData.inventoryConsumables != null) {
                for (Map.Entry<String, Integer> entry : saveData.inventoryConsumables.entrySet()) {
                    session.getInventory().addItem(entry.getKey(), entry.getValue());
                }
            }

            // 4. Restore Inventory Equipments
            Array<Equippable> restoredEquipments = new Array<>();
            if (saveData.ownedEquipments != null) {
                for (EquippableSaveData eqData : saveData.ownedEquipments) {
                    com.chronicorn.frontend.items.Item item = com.chronicorn.frontend.items.ItemDatabase
                            .createItem(eqData.id);
                    if (item instanceof Equippable) {
                        Equippable eq = (Equippable) item;
                        try {
                            eq.setFixedStat(Equippable.StatType.valueOf(eqData.fixedStat));
                            eq.setFixedValue(eqData.fixedValue);
                            eq.setRandomStat(Equippable.StatType.valueOf(eqData.randomStat));
                            eq.setRandomPercentBonus(eqData.randomPercentBonus);
                        } catch (Exception e) {
                            System.err.println("SaveManager: Failed to parse stats for equippable: " + e.getMessage());
                        }
                        session.getInventory().getOwnedEquipments().add(eq);
                        restoredEquipments.add(eq);
                    }
                }
            }

            // 5. Restore Party (excluding illegal characters)
            session.party = new com.chronicorn.frontend.managers.systems.PlayerParty();
            if (saveData.ownedCharacters != null) {
                for (ObjectMap.Entry<String, ActorSaveData> entry : saveData.ownedCharacters.entries()) {
                    String charId = entry.key;
                    ActorSaveData actorData = entry.value;

                    // Prune check
                    if (!verifiedOwnedIds.contains(charId)) {
                        System.out.println("SaveManager: Pruned character " + charId
                                + " from local save roster (not owned on backend).");
                        continue;
                    }

                    Actor actor = com.chronicorn.frontend.battlers.ActorFactory.createActor(charId);
                    actor.setId(charId);
                    actor.changeLevel(actorData.level);
                    actor.heal(actorData.hp - actor.getHp(), null); // set saved HP

                    // Restore equipments
                    if (actorData.equippedItemIndices != null) {
                        for (int idx : actorData.equippedItemIndices) {
                            if (idx >= 0 && idx < restoredEquipments.size) {
                                actor.equip(restoredEquipments.get(idx));
                            }
                        }
                    }

                    session.getParty().getOwnedCharacters().put(charId, actor);
                }
            }

            // 6. Restore Active Party Members (excluding pruned ones)
            if (saveData.activePartyIds != null) {
                for (int i = 0; i < saveData.activePartyIds.length; i++) {
                    String charId = saveData.activePartyIds[i];
                    if (charId != null && verifiedOwnedIds.contains(charId)) {
                        session.getParty().setActivePartyMember(i, charId);
                    } else {
                        session.getParty().setActivePartyMember(i, null);
                    }
                }
            }

            System.out.println("SaveManager: Loaded save data successfully.");
            if (loadCallback != null) {
                loadCallback.run();
            }
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error applying loaded save data: " + e.getMessage(), e);
        }
    }

    public static class SaveData {
        public String currentMapName;
        public float playerX;
        public float playerY;
        public String playerDirection;
        public int playerHp;

        // Switches and variables
        public Array<String> flags;
        public ObjectMap<String, Object> variables;

        // Inventory
        public HashMap<String, Integer> inventoryConsumables;
        public Array<EquippableSaveData> ownedEquipments;

        // Party
        public ObjectMap<String, ActorSaveData> ownedCharacters;
        public String[] activePartyIds;
    }

    public static class EquippableSaveData {
        public String id;
        public String fixedStat;
        public float fixedValue;
        public String randomStat;
        public float randomPercentBonus;
    }

    public static class ActorSaveData {
        public String charId;
        public int level;
        public int hp;
        public Array<Integer> equippedItemIndices;
    }
}
