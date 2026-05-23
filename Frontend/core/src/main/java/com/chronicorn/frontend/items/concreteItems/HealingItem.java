package com.chronicorn.frontend.items.concreteItems;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.items.Item;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public class HealingItem extends Item {
    private int hpRestoreAmount;
    private int energyRestoreAmount;

    public HealingItem(String id, String name, String icon, String description, int hpRestoreAmount, int energyRestoreAmount) {
        super(id, name, icon, description, true); // True because potions are consumed on use
        this.hpRestoreAmount = hpRestoreAmount;
        this.energyRestoreAmount = energyRestoreAmount;
    }

    @Override
    public boolean useOn(Actor target) {
        // Prevent using standard potions on dead characters
        if (!target.isAlive()) {
            return false;
        }

        boolean itemUsed = false;

        // --- HP HEALING LOGIC ---
        if (hpRestoreAmount > 0 && target.getHp() < target.getMaxHp()) {
            // Assuming your Actor class has a setHp or heal method.
            // If not, you just add the amount and clamp it to maxHp.
            int newHp = Math.min(target.getHp() + hpRestoreAmount, target.getMaxHp());
            target.heal(newHp, Elements.NONE);
            itemUsed = true;
        }

        // --- ENERGY HEALING LOGIC ---
        if (energyRestoreAmount > 0 && target.getEnergy() < target.getMaxEnergy()) {
            int newEnergy = Math.min(target.getEnergy() + energyRestoreAmount, target.getMaxEnergy());
            target.energyChange(newEnergy);
            itemUsed = true;
        }

        return itemUsed;
    }
}
