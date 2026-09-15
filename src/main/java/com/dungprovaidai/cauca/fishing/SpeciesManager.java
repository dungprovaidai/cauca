package com.dungprovaidai.cauca.fishing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.GsonBuilder;

/** Server reloadable species catalogue. The built-in catalogue is always a safe fallback. */
public final class SpeciesManager {
    private static final AtomicReference<Map<ResourceLocation, FishSpecies>> SPECIES = new AtomicReference<>(builtins());

    private SpeciesManager() {}

    public static void registerReloadListener(AddReloadListenerEvent event) {
        event.addListener(new Listener());
    }

    public static FishSpecies get(ResourceLocation id) {
        return SPECIES.get().getOrDefault(id, SPECIES.get().get(ResourceLocation.fromNamespaceAndPath("cauca_fishing", "cod")));
    }

    public static FishSpecies get(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        return parsed == null ? get(ResourceLocation.fromNamespaceAndPath("cauca_fishing", "cod")) : get(parsed);
    }

    public static List<FishSpecies> all() {
        return List.copyOf(SPECIES.get().values());
    }

    public static FishSpecies pick(RandomSource random, Habitat habitat, FishingSpotType spot, int depth,
                                   String bait, String weather, String time, int moonPhase) {
        List<FishSpecies> candidates = new ArrayList<>();
        List<Float> weights = new ArrayList<>();
        for (FishSpecies species : SPECIES.get().values()) {
            if (!species.habitats().contains(habitat) || !species.likesDepth(depth)) continue;
            if (species.legendary() && (!spot.isSpecial() || random.nextFloat() > 0.075F)) continue;

            float weight = species.rarityWeight();
            if (species.likesBait(bait)) weight *= 1.85F;
            else if (bait != null && !species.preferredBaits().isEmpty()) weight *= 0.35F;
            if (!species.likesCondition(weather, time)) weight *= 0.3F;
            if (time.equals("night") && species.movementPattern() == MovementPattern.NIGHT_EEL) weight *= 2.5F;
            if ((moonPhase == 0 || moonPhase == 4) && species.legendary()) weight *= 1.65F;
            weight *= spot.encounterMultiplier();
            candidates.add(species);
            weights.add(Math.max(0.01F, weight));
        }
        if (candidates.isEmpty()) return get(ResourceLocation.fromNamespaceAndPath("cauca_fishing", "cod"));

        float total = 0.0F;
        for (float weight : weights) total += weight;
        float selected = random.nextFloat() * total;
        for (int i = 0; i < candidates.size(); i++) {
            selected -= weights.get(i);
            if (selected <= 0.0F) return candidates.get(i);
        }
        return candidates.get(candidates.size() - 1);
    }

    private static Map<ResourceLocation, FishSpecies> builtins() {
        Map<ResourceLocation, FishSpecies> map = new LinkedHashMap<>();
        put(map, "cod", "Cod", MovementPattern.CRUISE, 28, 55, 0.4F, 2.2F, 36,
                Habitat.COLD_OCEAN, Habitat.DEEP_OCEAN, Habitat.RIVER, "worm", "minnow");
        put(map, "salmon", "Salmon", MovementPattern.DART, 35, 82, 0.8F, 5.5F, 55,
                Habitat.RIVER, Habitat.COLD_OCEAN, "worm", "minnow");
        put(map, "pufferfish", "Pufferfish", MovementPattern.ERRATIC, 12, 31, 0.2F, 1.4F, 44,
                Habitat.WARM_OCEAN, "shrimp", "grub");
        put(map, "tropical_fish", "Tropical Fish", MovementPattern.ERRATIC, 8, 24, 0.08F, 0.5F, 24,
                Habitat.WARM_OCEAN, "grub", "shrimp");
        put(map, "catfish", "Catfish", MovementPattern.NIGHT_EEL, 42, 105, 1.8F, 13.0F, 82,
                Habitat.RIVER, Habitat.SWAMP, Habitat.UNDERGROUND_WATER, "worm", "fish_meat");
        put(map, "trout", "Trout", MovementPattern.DART, 24, 66, 0.3F, 3.5F, 48,
                Habitat.RIVER, Habitat.COLD_OCEAN, "minnow", "worm");
        put(map, "pike", "Pike", MovementPattern.DART, 48, 112, 1.2F, 12.0F, 76,
                Habitat.RIVER, Habitat.LAKE, "minnow", "fish_meat");
        put(map, "eel", "Eel", MovementPattern.NIGHT_EEL, 38, 116, 0.4F, 5.8F, 68,
                Habitat.SWAMP, Habitat.RIVER, Habitat.UNDERGROUND_WATER, "grub", "glow_worm");
        put(map, "tuna", "Tuna", MovementPattern.DEEP_DIVER, 75, 190, 8.0F, 42.0F, 130,
                Habitat.DEEP_OCEAN, Habitat.COLD_OCEAN, "minnow", "shrimp");
        put(map, "carp", "Carp", MovementPattern.CRUISE, 32, 91, 0.7F, 17.5F, 72,
                Habitat.LAKE, Habitat.RIVER, Habitat.SWAMP, "worm", "grub");
        put(map, "golden_fish", "Golden Fish", MovementPattern.ERRATIC, 18, 42, 0.25F, 2.2F, 64,
                Habitat.RIVER, Habitat.LAKE, Habitat.UNDERGROUND_WATER, "glow_worm", "special_bait");
        put(map, "giant_catfish", "Giant Catfish", MovementPattern.LEGENDARY, 90, 145, 12.0F, 28.0F, 190,
                Habitat.RIVER, Habitat.SWAMP, "fish_meat", "special_bait");
        put(map, "ancient_carp", "Ancient Carp", MovementPattern.LEGENDARY, 72, 130, 9.0F, 24.0F, 175,
                Habitat.LAKE, Habitat.UNDERGROUND_WATER, "special_bait");
        put(map, "great_tuna", "Great Tuna", MovementPattern.LEGENDARY, 150, 250, 28.0F, 80.0F, 250,
                Habitat.DEEP_OCEAN, "shrimp", "special_bait");
        put(map, "abyssal_fish", "Abyssal Fish", MovementPattern.LEGENDARY, 80, 180, 16.0F, 48.0F, 230,
                Habitat.DEEP_OCEAN, Habitat.UNDERGROUND_WATER, "glow_worm", "special_bait");
        return map;
    }

    private static void put(Map<ResourceLocation, FishSpecies> map, String id, String name,
                            MovementPattern pattern, float minLength, float maxLength,
                            float minWeight, float maxWeight, float stamina,
                            Habitat first, Habitat second, String... baits) {
        put(map, id, name, pattern, minLength, maxLength, minWeight, maxWeight, stamina,
                new Habitat[]{first, second}, baits);
    }

    private static void put(Map<ResourceLocation, FishSpecies> map, String id, String name,
                            MovementPattern pattern, float minLength, float maxLength,
                            float minWeight, float maxWeight, float stamina,
                            Habitat first, Habitat second, Habitat third, String... baits) {
        put(map, id, name, pattern, minLength, maxLength, minWeight, maxWeight, stamina,
                new Habitat[]{first, second, third}, baits);
    }

    private static void put(Map<ResourceLocation, FishSpecies> map, String id, String name,
                            MovementPattern pattern, float minLength, float maxLength,
                            float minWeight, float maxWeight, float stamina,
                            Habitat[] habitats, String... baits) {
        EnumSet<Habitat> habitatSet = EnumSet.noneOf(Habitat.class);
        java.util.Collections.addAll(habitatSet, habitats);
        map.put(ResourceLocation.fromNamespaceAndPath("cauca_fishing", id), new FishSpecies(
                ResourceLocation.fromNamespaceAndPath("cauca_fishing", id), name, id,
                minLength, maxLength, minWeight, maxWeight, stamina, pattern,
                habitatSet, 0, 48, Set.of(baits), Set.of(), Set.of(),
                pattern == MovementPattern.LEGENDARY ? 1 : 20, pattern == MovementPattern.LEGENDARY,
                "cauca_fishing:fish/" + id, 0xD09B67));
    }

    private static final class Listener extends SimpleJsonResourceReloadListener {
        private Listener() {
            super(new GsonBuilder().create(), "fish_species");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> values, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, FishSpecies> next = new LinkedHashMap<>(builtins());
            values.forEach((id, element) -> {
                try {
                    next.put(id, parse(id, element.getAsJsonObject()));
                } catch (RuntimeException error) {
                    throw new JsonParseException("Could not load fish species " + id + ": " + error.getMessage());
                }
            });
            SPECIES.set(Map.copyOf(next));
        }
    }

    private static FishSpecies parse(ResourceLocation id, JsonObject json) {
        EnumSet<Habitat> habitats = EnumSet.noneOf(Habitat.class);
        for (JsonElement entry : array(json, "habitats")) habitats.add(Habitat.parse(entry.getAsString()));
        return new FishSpecies(
                id,
                GsonHelper.getAsString(json, "display_name", id.getPath()),
                GsonHelper.getAsString(json, "model", id.getPath()),
                GsonHelper.getAsFloat(json, "min_length_cm", 10.0F),
                GsonHelper.getAsFloat(json, "max_length_cm", 40.0F),
                GsonHelper.getAsFloat(json, "min_weight_kg", 0.1F),
                GsonHelper.getAsFloat(json, "max_weight_kg", 2.0F),
                GsonHelper.getAsFloat(json, "stamina", 40.0F),
                MovementPattern.parse(GsonHelper.getAsString(json, "movement_pattern", "CRUISE")),
                habitats,
                GsonHelper.getAsInt(json, "preferred_min_depth", 0),
                GsonHelper.getAsInt(json, "preferred_max_depth", 48),
                strings(json, "preferred_bait"), strings(json, "preferred_weather"),
                strings(json, "preferred_time"), GsonHelper.getAsInt(json, "rarity_weight", 20),
                GsonHelper.getAsBoolean(json, "legendary", false),
                GsonHelper.getAsString(json, "loot_table", "cauca_fishing:fish/" + id.getPath()),
                parseColor(GsonHelper.getAsString(json, "color", "D09B67"))
        );
    }

    private static Set<String> strings(JsonObject json, String key) {
        java.util.Set<String> values = new java.util.LinkedHashSet<>();
        for (JsonElement entry : array(json, key)) values.add(entry.getAsString().toLowerCase(java.util.Locale.ROOT));
        return values;
    }

    private static JsonArray array(JsonObject json, String key) {
        return json.has(key) && json.get(key).isJsonArray() ? json.getAsJsonArray(key) : new JsonArray();
    }

    private static int parseColor(String value) {
        try {
            return Integer.parseInt(value.replace("#", ""), 16);
        } catch (NumberFormatException ignored) {
            return 0xD09B67;
        }
    }
}
