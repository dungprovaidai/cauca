package com.dungprovaidai.cauca.block.entity;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.item.FishBucketItem;
import com.dungprovaidai.cauca.menu.AquariumMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AquariumBlockEntity extends FishContainerBlockEntity implements MenuProvider {
    private int waterLevel = 100;
    private int oxygen = 100;
    private int breedingProgress;
    private float swimTime;

    public AquariumBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AQUARIUM.get(), pos, state, 8);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AquariumBlockEntity aquarium) {
        aquarium.swimTime += 0.08F;
        if (level.getGameTime() % 20L != 0L) return;
        int live = 0;
        String firstSpecies = null;
        boolean compatible = true;
        for (int slot = 0; slot < aquarium.getContainerSize(); slot++) {
            FishCatchData data = aquarium.getItem(slot).get(ModComponents.FISH_CATCH);
            if (data != null && data.alive()) {
                live++;
                if (firstSpecies == null) firstSpecies = data.species();
                else if (!firstSpecies.equals(data.species())) compatible = false;
            }
        }
        aquarium.oxygen = Math.max(0, aquarium.oxygen - (live == 0 ? 0 : Math.max(1, live / 2)));
        if (live > 0 && compatible && live >= 2) aquarium.breedingProgress++;
        if (aquarium.breedingProgress > 600 && live < aquarium.getContainerSize()) {
            for (int slot = 0; slot < aquarium.getContainerSize(); slot++) {
                if (aquarium.getItem(slot).isEmpty() && firstSpecies != null) {
                    FishCatchData child = new FishCatchData(firstSpecies, 12.0F, 24.0F, 1, 1200, true);
                    aquarium.setItem(slot, FishBucketItem.filled(child));
                    aquarium.breedingProgress = 0;
                    break;
                }
            }
        }
        if (aquarium.oxygen == 0) {
            for (int slot = 0; slot < aquarium.getContainerSize(); slot++) {
                ItemStack stack = aquarium.getItem(slot);
                FishCatchData data = stack.get(ModComponents.FISH_CATCH);
                if (data != null && data.alive()) stack.set(ModComponents.FISH_CATCH, data.dead());
            }
        }
        aquarium.setChanged();
    }

    public float swimTime() { return swimTime; }
    public int waterLevel() { return waterLevel; }
    public int oxygen() { return oxygen; }
    public void addWater() { waterLevel = Math.min(100, waterLevel + 25); oxygen = Math.min(100, oxygen + 15); setChanged(); }
    public boolean addFish(FishCatchData data) {
        for (int slot = 0; slot < getContainerSize(); slot++) if (getItem(slot).isEmpty()) {
            setItem(slot, FishBucketItem.filled(data));
            return true;
        }
        return false;
    }

    @Override public Component getDisplayName() { return Component.translatable("block.cauca_fishing.aquarium"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new AquariumMenu(id, inventory, this); }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        waterLevel = tag.getInt("WaterLevel");
        oxygen = tag.getInt("Oxygen");
        breedingProgress = tag.getInt("Breeding");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("WaterLevel", waterLevel);
        tag.putInt("Oxygen", oxygen);
        tag.putInt("Breeding", breedingProgress);
        super.saveAdditional(tag, registries);
    }
}
