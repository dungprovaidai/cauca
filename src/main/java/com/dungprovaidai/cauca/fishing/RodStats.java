package com.dungprovaidai.cauca.fishing;

public record RodStats(
        String id,
        float strength,
        float sensitivity,
        float reelSpeed,
        float castingDistance,
        float lineCapacity,
        int durability,
        String tier
) {
    public float landingPower() {
        return Math.max(0.1F, (strength + reelSpeed) * 0.5F);
    }
}
