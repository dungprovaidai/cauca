package com.dungprovaidai.cauca.menu;

import com.dungprovaidai.cauca.block.entity.FishProcessingTableBlockEntity;
import com.dungprovaidai.cauca.registry.ModMenus;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

public class FishProcessingMenu extends FishContainerMenu {
    public FishProcessingMenu(int id, Inventory inventory, FishProcessingTableBlockEntity table) { super(ModMenus.FISH_PROCESSING_TABLE.get(), id, inventory, table, 6); }
    public FishProcessingMenu(int id, Inventory inventory) { super(ModMenus.FISH_PROCESSING_TABLE.get(), id, inventory, new SimpleContainer(6), 6); }
}
