package com.dungprovaidai.cauca.block.entity;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.menu.FishProcessingMenu;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import com.dungprovaidai.cauca.registry.ModComponents;
import com.dungprovaidai.cauca.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class FishProcessingTableBlockEntity extends FishContainerBlockEntity implements MenuProvider {
    private int progress;

    public FishProcessingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_PROCESSING_TABLE.get(), pos, state, 6);
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, FishProcessingTableBlockEntity table) {
        if (level.getGameTime() % 10L != 0L) return;
        if (table.getItem(0).isEmpty()) return;
        table.progress++;
        if (table.progress >= 20) {
            table.processOne();
            table.progress = 0;
        }
        table.setChanged();
    }

    public void processOne() {
        ItemStack input = getItem(0);
        FishCatchData data = input.get(ModComponents.FISH_CATCH);
        if (data == null) return;
        ItemStack meat = new ItemStack(ModItems.FISH_MEAT.get());
        meat.set(ModComponents.FISH_CATCH, data.dead());
        insertOrDrop(meat, 1);
        insertOrDrop(new ItemStack(ModItems.FISH_HEAD.get()), 1);
        insertOrDrop(new ItemStack(ModItems.FISH_BONE.get()), 1);
        insertOrDrop(new ItemStack(ModItems.FISH_SCALE.get()), 1);
        if (data.species().contains("pufferfish")) insertOrDrop(new ItemStack(ModItems.POISON_SAC.get()), 1);
        input.shrink(1);
    }

    private void insertOrDrop(ItemStack stack, int count) {
        stack.setCount(count);
        for (int slot = 1; slot < getContainerSize(); slot++) {
            ItemStack existing = getItem(slot);
            if (existing.isEmpty()) { setItem(slot, stack); return; }
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                existing.grow(move);
                stack.shrink(move);
                if (stack.isEmpty()) return;
            }
        }
        if (level != null) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), stack);
    }

    @Override public Component getDisplayName() { return Component.translatable("block.cauca_fishing.fish_processing_table"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new FishProcessingMenu(id, inventory, this); }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Progress", progress);
        super.saveAdditional(tag, registries);
    }
}
