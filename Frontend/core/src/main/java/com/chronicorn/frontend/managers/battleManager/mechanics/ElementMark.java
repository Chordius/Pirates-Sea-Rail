package com.chronicorn.frontend.managers.battleManager.mechanics;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public class ElementMark {
    public Elements element;
    public Battler source;
    public int duration;

    public ElementMark(Elements e, Battler s, int d) {
        this.element = e;
        this.source = s;
        this.duration = d;
        System.out.print("Elemental Mark of " + this.element.name() + " applied!");
    }
}
