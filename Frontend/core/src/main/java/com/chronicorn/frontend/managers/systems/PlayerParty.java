package com.chronicorn.frontend.managers.systems;

import com.chronicorn.frontend.battlers.Actor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerParty {
    // Maps the backend String ID (e.g., "CHAR_LUFFY") to the local Actor object
    private Map<String, Actor> ownedCharacters;

    // Parallel arrays to manage the 4-slot battle team
    private String[] activePartyIds;
    private Actor[] activeParty;

    public PlayerParty() {
        this.ownedCharacters = new HashMap<>();
        this.activePartyIds = new String[4]; // Indices 0, 1, 2, 3
        this.activeParty = new Actor[4];
    }

    /**
     * Call this when the backend confirms a successful gacha pull.
     * You pass the ID from the server, and the newly instantiated Actor object.
     */
    public void unlockCharacter(String charId, Actor newActor) {
        if (!ownedCharacters.containsKey(charId)) {
            ownedCharacters.put(charId, newActor);
            autoEquipIfSpaceAvailable(charId);
        }
    }

    // Automatically equips the character if the party is not full
    private void autoEquipIfSpaceAvailable(String charId) {
        for (int i = 0; i < activePartyIds.length; i++) {
            if (activePartyIds[i] == null) {
                setActivePartyMember(i, charId);
                return;
            }
        }
    }

    /**
     * Assigns an owned Actor to a specific battle slot (0-3).
     */
    public void setActivePartyMember(int slotIndex, String charId) {
        if (slotIndex < 0 || slotIndex >= 4) return;

        // INTEGRITY CHECK: Refuse to equip a character not in the owned map.
        if (charId != null && !ownedCharacters.containsKey(charId)) {
            System.err.println("Integrity Error: Attempted to equip an unowned character.");
            return;
        }

        if (charId == null) {
            activePartyIds[slotIndex] = null;
            activeParty[slotIndex] = null;
        } else {
            activePartyIds[slotIndex] = charId;
            activeParty[slotIndex] = ownedCharacters.get(charId);
        }
    }

    // Returns the actual Actor array to load directly into your BattleManager
    public List<Actor> getActivePartyActors() {
        List<Actor> list = new ArrayList<>();
        for (Actor actor : activeParty) {
            if (actor != null) {
                list.add(actor);
            }
        }
        return list;
    }

    // Returns the String IDs to send back to the Spring Boot server for verification
    public List<String> getActivePartyIds() {
        List<String> list = new ArrayList<>();
        for (String id : activePartyIds) {
            if (id != null) {
                list.add(id);
            }
        }
        return list;
    }

    public Map<String, Actor> getOwnedCharacters() {
        return ownedCharacters;
    }

    public String[] getActivePartyIdsArray() {
        return activePartyIds;
    }
}
