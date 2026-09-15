package com.dungprovaidai.cauca.block.entity;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.menu.FishingMarketMenu;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import com.dungprovaidai.cauca.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class FishingMarketBlockEntity extends FishContainerBlockEntity implements MenuProvider {
    public FishingMarketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHING_MARKET.get(), pos, state, 3);
    }

    public static int quote(ItemStack stack) {
        FishCatchData data = stack.get(ModComponents.FISH_CATCH);
        if (data == null || data.alive() || data.freshness() <= 0) return 0;
        float freshnessMultiplier = data.freshness() > 800 ? 1.0F : data.freshness() > 300 ? 0.75F : 0.25F;
        float qualityMultiplier = 0.65F + data.quality() * 0.12F;
        float rarityMultiplier = data.species().contains("golden") || data.species().contains("giant") ? 3.0F : 1.0F;
        return Math.max(1, Math.round(data.weightKg() * 2.0F * qualityMultiplier * rarityMultiplier * freshnessMultiplier));
    }

    public int totalQuote() {
        int total = 0;
        for (int i = 0; i < getContainerSize(); i++) total += quote(getItem(i));
        return total;
    }

    @Override public Component getDisplayName() { return Component.translatable("block.cauca_fishing.fishing_market"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new FishingMarketMenu(id, inventory, this); }
}
