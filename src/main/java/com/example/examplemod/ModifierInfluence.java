package com.example.examplemod;

import java.util.List;

/** Stable IDs are saved on parts; NONE is the default and is never assigned by natural rolls. */
public enum ModifierInfluence {
    NONE("", "Uninfluenced", 0x9CAAB5),
    NETHER("nether", "Nether", 0xF0A075),
    END("end", "End", 0xC5A3F1);

    public static final List<ModifierInfluence> CHOICES = List.of(NETHER, END);
    public static final int MATCHING_WEIGHT_MULTIPLIER = 6;
    public final String id, label;
    public final int color;
    ModifierInfluence(String id, String label, int color) { this.id=id; this.label=label; this.color=color; }

    /** Null denotes an unknown future influence, which mutation guards preserve rather than reinterpret. */
    public static ModifierInfluence byId(String id) {
        for (var influence : values()) if (influence.id.equals(id)) return influence;
        return null;
    }
    public static ModifierInfluence choice(int index) {
        return index>=0 && index<CHOICES.size() ? CHOICES.get(index) : null;
    }
    public boolean allows(ModifierInfluence tag, boolean influenceOnly) {
        if (this==NONE) return !influenceOnly;
        return tag==NONE || tag==this;
    }
    public int weight(int baseWeight, ModifierInfluence tag) {
        return this!=NONE && tag==this ? Math.multiplyExact(baseWeight,MATCHING_WEIGHT_MULTIPLIER) : baseWeight;
    }
}
