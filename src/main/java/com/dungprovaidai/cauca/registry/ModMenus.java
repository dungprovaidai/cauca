package com.dungprovaidai.cauca.registry;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.menu.AquariumMenu;
import com.dungprovaidai.cauca.menu.FishBasketMenu;
import com.dungprovaidai.cauca.menu.FishProcessingMenu;
import com.dungprovaidai.cauca.menu.FishingMarketMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenus {
    private ModMenus() {}

    public static final DeferredHolder<MenuType<?>, MenuType<FishBasketMenu>> FISH_BASKET = CaucaFishing.MENUS.register("fish_basket", () -> new MenuType<>(FishBasketMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<AquariumMenu>> AQUARIUM = CaucaFishing.MENUS.register("aquarium", () -> new MenuType<>(AquariumMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<FishProcessingMenu>> FISH_PROCESSING_TABLE = CaucaFishing.MENUS.register("fish_processing_table", () -> new MenuType<>(FishProcessingMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<FishingMarketMenu>> FISHING_MARKET = CaucaFishing.MENUS.register("fishing_market", () -> new MenuType<>(FishingMarketMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
