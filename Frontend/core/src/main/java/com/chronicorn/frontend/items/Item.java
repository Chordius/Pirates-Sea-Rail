package com.chronicorn.frontend.items;

import com.chronicorn.frontend.battlers.Actor;

public abstract class Item {
    protected String id;
    protected String name;
    protected String icon;
    protected String description;
    protected boolean isConsumable;

    public Item(String id, String name, String icon, String description, boolean isConsumable) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.isConsumable = isConsumable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public boolean isConsumable() { return isConsumable; }

    /**
     * Executes the item's effect on a specific character.
     * @return true if the item was successfully used, false if it had no effect (e.g., target already at max HP).
     */
    public abstract boolean useOn(Actor target);
}
