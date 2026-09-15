package com.dungprovaidai.cauca.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class FishContainerMenu extends AbstractContainerMenu {
    protected final Container container;
    private final int containerSlots;

    protected FishContainerMenu(MenuType<?> type, int id, Inventory inventory, Container container, int containerSlots) {
        super(type, id);
        this.container = container;
        this.containerSlots = containerSlots;
        checkContainerSize(container, containerSlots);
        for (int slot = 0; slot < containerSlots; slot++) addSlot(new Slot(container, slot, 44 + (slot % 9) * 18, 18 + (slot / 9) * 18));
        addPlayerInventory(inventory);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public int containerSlotCount() { return containerSlots; }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return empty;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < containerSlots) {
            if (!moveItemStackTo(source, containerSlots, slots.size(), true)) return empty;
        } else if (!moveItemStackTo(source, 0, containerSlots, false)) return empty;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
}
