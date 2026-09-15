package com.dungprovaidai.cauca.fishing;

import net.minecraft.util.RandomSource;

public record FishSize(float weightKg, float lengthCm, SizeTier tier, FishQuality quality) {
    public static FishSize roll(FishSpecies species, RandomSource random) {
        float percentile = random.nextFloat();
        // Squaring makes average catches common while still allowing records.
        float curve = percentile * percentile;
        float length = species.minLengthCm() + (species.maxLengthCm() - species.minLengthCm()) * curve;
        float weight = species.minWeightKg() + (species.maxWeightKg() - species.minWeightKg()) * curve;
        SizeTier tier = SizeTier.fromPercentile(percentile, species.legendary());
        return new FishSize(weight, length, tier, FishQuality.fromPercentile(percentile, species.legendary()));
    }

    public String weightText() {
        return String.format(java.util.Locale.ROOT, "%.2f kg", weightKg);
    }

    public String lengthText() {
        return String.format(java.util.Locale.ROOT, "%.0f cm", lengthCm);
    }
}
