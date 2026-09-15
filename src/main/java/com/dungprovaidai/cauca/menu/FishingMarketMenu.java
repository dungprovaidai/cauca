package com.dungprovaidai.cauca.menu;

import com.dungprovaidai.cauca.block.entity.FishingMarketBlockEntity;
import com.dungprovaidai.cauca.registry.ModMenus;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

public class FishingMarketMenu extends FishContainerMenu {
    public FishingMarketMenu(int id, Inventory inventory, FishingMarketBlockEntity market) { super(ModMenus.FISHING_MARKET.get(), id, inventory, market, 3); }
    public FishingMarketMenu(int id, Inventory inventory) { super(ModMenus.FISHING_MARKET.get(), id, inventory, new SimpleContainer(3), 3); }
}
