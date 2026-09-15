package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.menu.FishBasketMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FishBasketScreen extends FishContainerScreen<FishBasketMenu> {
    public FishBasketScreen(FishBasketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
