package com.dungprovaidai.cauca.fishing;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Immutable data contract for a species. Behavior is selected by movementPattern;
 * everything else is intentionally data so datapacks can add species without code.
 */
public record FishSpecies(
        ResourceLocation id,
        String displayName,
        String model,
        float minLengthCm,
        float maxLengthCm,
        float minWeightKg,
        float maxWeightKg,
        float stamina,
        MovementPattern movementPattern,
        Set<Habitat> habitats,
        int preferredMinDepth,
        int preferredMaxDepth,
        Set<String> preferredBaits,
        Set<String> preferredWeather,
        Set<String> preferredTimes,
        int rarityWeight,
        boolean legendary,
        String lootTable,
        int color
) {
    public FishSpecies {
        habitats = habitats == null || habitats.isEmpty()
                ? Collections.unmodifiableSet(EnumSet.of(Habitat.RIVER))
                : Collections.unmodifiableSet(EnumSet.copyOf(habitats));
        preferredBaits = immutable(preferredBaits);
        preferredWeather = immutable(preferredWeather);
        preferredTimes = immutable(preferredTimes);
        model = model == null || model.isBlank() ? id.getPath() : model;
        displayName = displayName == null || displayName.isBlank() ? id.getPath() : displayName;
        rarityWeight = Math.max(1, rarityWeight);
        preferredMaxDepth = Math.max(preferredMinDepth, preferredMaxDepth);
    }

    private static Set<String> immutable(Set<String> values) {
        if (values == null || values.isEmpty()) return Set.of();
        Set<String> normalized = new LinkedHashSet<>();
        values.forEach(value -> normalized.add(value.toLowerCase(java.util.Locale.ROOT)));
        return Collections.unmodifiableSet(normalized);
    }

    public boolean likesBait(String bait) {
        return bait != null && (preferredBaits.isEmpty() || preferredBaits.contains(bait.toLowerCase(java.util.Locale.ROOT)));
    }

    public boolean likesCondition(String weather, String time) {
        boolean weatherOk = preferredWeather.isEmpty() || preferredWeather.contains(weather.toLowerCase(java.util.Locale.ROOT));
        boolean timeOk = preferredTimes.isEmpty() || preferredTimes.contains(time.toLowerCase(java.util.Locale.ROOT));
        return weatherOk && timeOk;
    }

    public boolean likesDepth(int depth) {
        return depth >= preferredMinDepth && depth <= preferredMaxDepth;
    }

    public float sizePressure(float weightKg) {
        float span = Math.max(0.01F, maxWeightKg - minWeightKg);
        return Math.min(1.0F, Math.max(0.0F, (weightKg - minWeightKg) / span));
    }
}
