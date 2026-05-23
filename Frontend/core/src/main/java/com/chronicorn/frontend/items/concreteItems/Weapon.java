package com.chronicorn.frontend.items.concreteItems;

import com.chronicorn.frontend.items.Equippable;

public class Weapon extends Equippable {

    public Weapon(String id, String name, String icon, String description, StatType fixedStat, float fixedValue, StatType randomStat) {
        super(id, name, icon, description, fixedStat, fixedValue, randomStat);
    }
}
