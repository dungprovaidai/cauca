package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.menu.AquariumMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AquariumScreen extends FishContainerScreen<AquariumMenu> {
    public AquariumScreen(AquariumMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
