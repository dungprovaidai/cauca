package com.dungprovaidai.cauca.block.entity;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import com.dungprovaidai.cauca.registry.ModComponents;
import com.dungprovaidai.cauca.item.FishBucketItem;
import com.dungprovaidai.cauca.menu.FishBasketMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FishBasketBlockEntity extends FishContainerBlockEntity implements MenuProvider {
    private int waterLevel = 100;
    private int oxygen = 100;

    public FishBasketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_BASKET.get(), pos, state, 6);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FishBasketBlockEntity basket) {
        if (level.getGameTime() % 20L != 0L) return;
        boolean hasLiveFish = false;
        for (int slot = 0; slot < basket.getContainerSize(); slot++) {
            FishCatchData data = basket.getItem(slot).get(ModComponents.FISH_CATCH);
            if (data != null && data.alive()) hasLiveFish = true;
        }
        if (hasLiveFish) {
            basket.oxygen = Math.max(0, basket.oxygen - (basket.waterLevel > 0 ? 1 : 4));
            if (basket.oxygen == 0) {
                for (int slot = 0; slot < basket.getContainerSize(); slot++) {
                    ItemStack stack = basket.getItem(slot);
                    FishCatchData data = stack.get(ModComponents.FISH_CATCH);
                    if (data != null && data.alive()) stack.set(ModComponents.FISH_CATCH, data.dead());
                }
            }
            basket.setChanged();
        } else if (basket.oxygen < 100 && basket.waterLevel > 0) {
            basket.oxygen = Math.min(100, basket.oxygen + 1);
            basket.setChanged();
        }
    }

    public boolean addLiveFish(FishCatchData data) {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (getItem(slot).isEmpty()) {
                setItem(slot, FishBucketItem.filled(data));
                return true;
            }
        }
        return false;
    }

    public int waterLevel() { return waterLevel; }
    public int oxygen() { return oxygen; }
    public void addWater() { waterLevel = Math.min(100, waterLevel + 25); oxygen = Math.min(100, oxygen + 15); setChanged(); }

    @Override public Component getDisplayName() { return Component.translatable("block.cauca_fishing.fish_basket"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new FishBasketMenu(id, inventory, this); }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        waterLevel = tag.getInt("WaterLevel");
        oxygen = tag.getInt("Oxygen");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("WaterLevel", waterLevel);
        tag.putInt("Oxygen", oxygen);
        super.saveAdditional(tag, registries);
    }
}
