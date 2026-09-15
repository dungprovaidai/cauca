package com.dungprovaidai.cauca.fishing;

public enum SizeTier {
    SMALL,
    AVERAGE,
    LARGE,
    GIANT,
    LEGENDARY;

    public static SizeTier fromPercentile(float percentile, boolean legendary) {
        if (legendary && percentile > 0.9F) return LEGENDARY;
        if (percentile > 0.88F) return GIANT;
        if (percentile > 0.65F) return LARGE;
        if (percentile > 0.22F) return AVERAGE;
        return SMALL;
    }
}
