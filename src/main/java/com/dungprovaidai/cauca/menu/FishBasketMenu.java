package com.dungprovaidai.cauca.menu;

import com.dungprovaidai.cauca.block.entity.FishBasketBlockEntity;
import com.dungprovaidai.cauca.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;

public class FishBasketMenu extends FishContainerMenu {
    public FishBasketMenu(int id, Inventory inventory, FishBasketBlockEntity basket) {
        super(ModMenus.FISH_BASKET.get(), id, inventory, basket, 6);
    }
    public FishBasketMenu(int id, Inventory inventory) { super(ModMenus.FISH_BASKET.get(), id, inventory, new SimpleContainer(6), 6); }
}
