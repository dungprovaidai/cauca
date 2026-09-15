package com.dungprovaidai.cauca.fishing;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Cheap, local resolver: no global scan and no per-fish pathfinding. */
public final class FishingSpotResolver {
    private FishingSpotResolver() {}

    public static FishingSpot resolve(Level level, BlockPos pos) {
        Habitat habitat = habitat(level, pos);
        int depth = waterDepth(level, pos);
        FishingSpotType type = baseType(habitat);

        int kelp = 0;
        int bubbles = 0;
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            BlockState state = level.getBlockState(nearby);
            if (state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT) || state.is(Blocks.SEAGRASS)
                    || state.is(Blocks.TALL_SEAGRASS)) kelp++;
            if (state.is(Blocks.BUBBLE_COLUMN)) bubbles++;
        }
        if (depth >= 12 && (Math.floorMod(pos.asLong(), 3L) == 0L || bubbles > 5)) type = FishingSpotType.DEEP_SPOT;
        if (bubbles > 2) type = bubbles > 7 ? FishingSpotType.WHIRLPOOL : FishingSpotType.BUBBLE_SPOT;
        if (kelp > 4 && type == baseType(habitat)) type = FishingSpotType.FISH_SCHOOL;
        if (Math.floorMod(pos.asLong(), 97L) == 0L) type = FishingSpotType.RARE_SPOT;
        return new FishingSpot(type, habitat, depth, pos);
    }

    public static int waterDepth(Level level, BlockPos pos) {
        int depth = 0;
        BlockPos cursor = pos;
        while (depth < 48 && level.getFluidState(cursor).is(FluidTags.WATER)) {
            depth++;
            cursor = cursor.below();
        }
        return depth;
    }

    private static Habitat habitat(Level level, BlockPos pos) {
        var biome = level.getBiome(pos);
        String key = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getKey(biome.value()).toString();
        if (key.contains("swamp") || key.contains("mangrove")) return Habitat.SWAMP;
        if (biome.is(BiomeTags.IS_RIVER)) return Habitat.RIVER;
        if (biome.is(BiomeTags.IS_OCEAN)) {
            if (key.contains("warm") || key.contains("lukewarm") || key.contains("jungle")) return Habitat.WARM_OCEAN;
            if (key.contains("deep")) return Habitat.DEEP_OCEAN;
            return Habitat.COLD_OCEAN;
        }
        if (pos.getY() < level.getSeaLevel() - 18) return Habitat.UNDERGROUND_WATER;
        return Habitat.LAKE;
    }

    private static FishingSpotType baseType(Habitat habitat) {
        return switch (habitat) {
            case RIVER -> FishingSpotType.RIVER;
            case LAKE -> FishingSpotType.LAKE;
            case SWAMP -> FishingSpotType.SWAMP;
            case WARM_OCEAN -> FishingSpotType.WARM_OCEAN;
            case COLD_OCEAN -> FishingSpotType.COLD_OCEAN;
            case DEEP_OCEAN -> FishingSpotType.DEEP_OCEAN;
            case UNDERGROUND_WATER -> FishingSpotType.UNDERGROUND_WATER;
        };
    }
}
