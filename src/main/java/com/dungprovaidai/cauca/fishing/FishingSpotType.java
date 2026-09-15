package com.dungprovaidai.cauca.fishing;

/** A resolved habitat plus a local modifier such as bubbles or a whirlpool. */
public enum FishingSpotType {
    RIVER(Habitat.RIVER, 1.0F, 0x5CA9D6),
    LAKE(Habitat.LAKE, 1.0F, 0x5C9FD6),
    SWAMP(Habitat.SWAMP, 0.95F, 0x6E9562),
    WARM_OCEAN(Habitat.WARM_OCEAN, 1.05F, 0xF1B35B),
    COLD_OCEAN(Habitat.COLD_OCEAN, 1.05F, 0x8AC6E8),
    DEEP_OCEAN(Habitat.DEEP_OCEAN, 1.2F, 0x5269B4),
    UNDERGROUND_WATER(Habitat.UNDERGROUND_WATER, 0.8F, 0x6C6C9C),
    BUBBLE_SPOT(null, 1.25F, 0x65E4E7),
    FISH_SCHOOL(null, 1.35F, 0xDDEB7A),
    DEEP_SPOT(null, 1.45F, 0x5868E6),
    WHIRLPOOL(null, 1.55F, 0xA688D6),
    RARE_SPOT(null, 1.8F, 0xFFD45E);

    private final Habitat habitat;
    private final float encounterMultiplier;
    private final int color;

    FishingSpotType(Habitat habitat, float encounterMultiplier, int color) {
        this.habitat = habitat;
        this.encounterMultiplier = encounterMultiplier;
        this.color = color;
    }

    public Habitat habitat() {
        return habitat;
    }

    public float encounterMultiplier() {
        return encounterMultiplier;
    }

    public int color() {
        return color;
    }

    public boolean isSpecial() {
        return habitat == null;
    }
}
