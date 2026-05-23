package com.chronicorn.frontend.items;

import com.chronicorn.frontend.items.concreteItems.HealingItem;
import com.chronicorn.frontend.items.concreteItems.Weapon;

import java.util.HashMap;
import java.util.Map;

public class ItemDatabase {
    private static final Map<String, Item> items = new HashMap<>();

    // Initialize all items in the game here
    static {
        items.put("ITEM_POTION", new HealingItem(
            "ITEM_POTION",
            "Basic Potion",
            "Icon_10",
            "Restores 50 HP to an ally.",
            50, 0
        ));

        items.put("ITEM_ELIXIR", new HealingItem(
            "ITEM_ELIXIR",
            "Energy Elixir",
            "Icon_11",
            "Restores 30 Energy to an ally.",
            0, 30
        ));

        items.put("W001", new Weapon(
            "W001",
            "Traveler's Blade",
            "Icon_1",
            "A standard iron sword.",
            Equippable.StatType.ATTACK, 10,
            Equippable.StatType.CRIT_RATE
        ));

        items.put("W002", new Weapon(
            "W002",
            "Wooden Staff",
            "Icon_3",
            "A simple wooden staff.",
            Equippable.StatType.MAGIC, 12,
            Equippable.StatType.CRIT_DMG
        ));

        items.put("W003", new Weapon(
            "W003",
            "Steel Axe",
            "Icon_2",
            "A sturdy steel axe.",
            Equippable.StatType.ATTACK, 15,
            Equippable.StatType.CRIT_RATE
        ));

        items.put("W004", new Weapon(
            "W004",
            "Assassin's Bracers",
            "Icon_5",
            "A sharp diamond blade.",
            Equippable.StatType.ATTACK, 20,
            Equippable.StatType.CRIT_DMG
        ));
    }

    public static Item getItem(String id) {
        return items.get(id);
    }

    /**
     * Creates a new instance of an item, essential for Equippables so they
     * roll a fresh random stat each time they are dropped.
     */
    public static Item createItem(String id) {
        Item template = items.get(id);
        if (template == null) return null;

        if (template instanceof Weapon) {
            Weapon w = (Weapon) template;
            // Create a brand new instance to roll new random percentages
            return new Weapon(w.getId(), w.getName(), w.getIcon(), w.getDescription(),
                              w.getFixedStat(), w.getFixedValue(), w.getRandomStat());
        } else if (template instanceof HealingItem) {
            HealingItem h = (HealingItem) template;
            // Or just return the static one since they don't have random values
            // but returning a new one is safer if they might store state
            return h;
        }
        return template;
    }
}
