package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.menu.FishProcessingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FishProcessingScreen extends FishContainerScreen<FishProcessingMenu> {
    public FishProcessingScreen(FishProcessingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
