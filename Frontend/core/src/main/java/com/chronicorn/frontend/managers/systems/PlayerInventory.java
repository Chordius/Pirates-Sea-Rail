package com.chronicorn.frontend.managers.systems;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.items.Equippable;
import com.chronicorn.frontend.items.Item;
import com.chronicorn.frontend.items.ItemDatabase;

import java.util.HashMap;
import java.util.Map;

public class PlayerInventory {
    // Maps a String ID (e.g., "ITEM_POTION") to its total quantity for stacking consumables
    private Map<String, Integer> items;
    
    // Explicit object array for equippables since they hold unique randomized stats
    private Array<Equippable> ownedEquipments;

    public PlayerInventory() {
        this.items = new HashMap<>();
        this.ownedEquipments = new Array<>();
    }

    public void addItem(String itemId, int amount) {
        if (amount <= 0) return;
        
        Item newItem = ItemDatabase.createItem(itemId);
        if (newItem instanceof Equippable) {
            // Weapons/Equippables have unique random rolls, so they cannot be stacked.
            // We store each instance individually.
            for (int i = 0; i < amount; i++) {
                // Must call createItem again inside the loop to ensure separate random rolls if amount > 1
                ownedEquipments.add((Equippable) ItemDatabase.createItem(itemId));
            }
        } else {
            // Stack consumables by increasing quantity
            items.put(itemId, items.getOrDefault(itemId, 0) + amount);
        }
    }

    public void removeEquippable(Equippable equippable) {
        ownedEquipments.removeValue(equippable, true);
    }

    public Array<Equippable> getOwnedEquipments() {
        return ownedEquipments;
    }

    public void removeItem(String itemId, int amount) {
        if (amount <= 0 || !items.containsKey(itemId)) return;

        int currentQuantity = items.get(itemId);
        if (currentQuantity <= amount) {
            // Remove the item entirely if it drops to 0
            items.remove(itemId);
        } else {
            items.put(itemId, currentQuantity - amount);
        }
    }

    public int getItemQuantity(String itemId) {
        return items.getOrDefault(itemId, 0);
    }

    public boolean hasItem(String itemId, int minimumAmount) {
        return getItemQuantity(itemId) >= minimumAmount;
    }

    public Map<String, Integer> getAllItems() {
        return items;
    }
}
