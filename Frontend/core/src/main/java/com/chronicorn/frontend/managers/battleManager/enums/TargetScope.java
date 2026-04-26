package com.chronicorn.frontend.managers.battleManager.enums;

public enum TargetScope {
    SINGLE,  // One target
    BLAST,   // Target + 1 adjacent on each side
    ALL,     // Everyone in the array
    SELF,    // Only the user
    ALLY,    // one Ally
    ALLIES   // All allies
}
