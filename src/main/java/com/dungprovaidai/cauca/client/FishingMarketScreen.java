package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.menu.FishingMarketMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FishingMarketScreen extends FishContainerScreen<FishingMarketMenu> {
    public FishingMarketScreen(FishingMarketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
