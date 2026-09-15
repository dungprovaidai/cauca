package com.dungprovaidai.cauca.fishing;

import net.minecraft.core.BlockPos;

public record FishingSpot(FishingSpotType type, Habitat habitat, int depth, BlockPos position) {
    public boolean isSpecial() {
        return type.isSpecial();
    }
}
