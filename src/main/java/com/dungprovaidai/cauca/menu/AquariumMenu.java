package com.dungprovaidai.cauca.menu;

import com.dungprovaidai.cauca.block.entity.AquariumBlockEntity;
import com.dungprovaidai.cauca.registry.ModMenus;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

public class AquariumMenu extends FishContainerMenu {
    public AquariumMenu(int id, Inventory inventory, AquariumBlockEntity aquarium) { super(ModMenus.AQUARIUM.get(), id, inventory, aquarium, 8); }
    public AquariumMenu(int id, Inventory inventory) { super(ModMenus.AQUARIUM.get(), id, inventory, new SimpleContainer(8), 8); }
}
