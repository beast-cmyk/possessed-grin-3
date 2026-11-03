package com.possessedgrin.power;

/** The key-activated powers. Default keys can be changed in Options > Controls > Possessed Powers. */
public enum Power {
    TELEKINESIS("telekinesis", 10, 82 /* R */),
    POLTERGEIST("poltergeist", 60, 71 /* G */),
    TELEPATHY("telepathy", 100, 86 /* V */),
    HEAD_TURN("head_turn", 60, 72 /* H */),
    BILE("bile", 80, 66 /* B */);

    public final String id;
    /** Ticks before the power can be used again (20 ticks = 1 second). */
    public final int cooldown;
    /** GLFW key code (kept as a plain number so this class is safe to load on servers). */
    public final int defaultKey;

    Power(String id, int cooldown, int defaultKey) {
        this.id = id;
        this.cooldown = cooldown;
        this.defaultKey = defaultKey;
    }
}
