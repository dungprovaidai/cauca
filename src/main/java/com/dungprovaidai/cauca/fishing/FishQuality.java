package com.dungprovaidai.cauca.fishing;

public enum FishQuality {
    POOR(0.65F, 0x8A8A8A),
    FAIR(0.85F, 0xB7B7A4),
    GOOD(1.0F, 0xF0E68C),
    EXCELLENT(1.35F, 0x7CDB9A),
    RECORD(1.8F, 0xFFD65A);

    private final float valueMultiplier;
    private final int color;

    FishQuality(float valueMultiplier, int color) {
        this.valueMultiplier = valueMultiplier;
        this.color = color;
    }

    public float valueMultiplier() {
        return valueMultiplier;
    }

    public int color() {
        return color;
    }

    public static FishQuality fromPercentile(float percentile, boolean legendary) {
        if (legendary && percentile > 0.78F) return RECORD;
        if (percentile > 0.93F) return RECORD;
        if (percentile > 0.76F) return EXCELLENT;
        if (percentile > 0.48F) return GOOD;
        if (percentile > 0.2F) return FAIR;
        return POOR;
    }
}
