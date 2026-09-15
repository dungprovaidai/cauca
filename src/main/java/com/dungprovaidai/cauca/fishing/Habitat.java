package com.dungprovaidai.cauca.fishing;

public enum Habitat {
    RIVER,
    LAKE,
    SWAMP,
    WARM_OCEAN,
    COLD_OCEAN,
    DEEP_OCEAN,
    UNDERGROUND_WATER;

    public static Habitat parse(String value) {
        try {
            return value == null ? RIVER : valueOf(value.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return RIVER;
        }
    }
}
