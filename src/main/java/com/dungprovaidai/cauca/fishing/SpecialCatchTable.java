package com.dungprovaidai.cauca.fishing;

import com.dungprovaidai.cauca.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Small vanilla-compatible treasure table; special catches never replace the normal fish loop. */
public final class SpecialCatchTable {
    private SpecialCatchTable() {}

    public static Optional<ItemStack> roll(RandomSource random, FishingSpot spot, String baitId) {
        float chance = spot.isSpecial() ? 0.018F : 0.004F;
        if ("special_bait".equals(baitId)) chance += 0.012F;
        if (random.nextFloat() >= chance) return Optional.empty();
        return Optional.of(random.nextFloat() < 0.72F
                ? new ItemStack(ModItems.ANCIENT_COIN.get())
                : new ItemStack(ModItems.MAP_FRAGMENT.get()));
    }
}
