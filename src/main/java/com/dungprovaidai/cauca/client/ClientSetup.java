package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.network.ModNetworking;
import com.dungprovaidai.cauca.registry.ModEntities;
import com.dungprovaidai.cauca.registry.ModMenus;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class ClientSetup {
    public static final KeyMapping REEL_KEY = new KeyMapping("key.cauca_fishing.reel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, "key.categories.cauca_fishing");

    private ClientSetup() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerScreens);
        modEventBus.addListener(ClientSetup::registerGuiLayers);
        modEventBus.addListener(ClientSetup::registerKeys);
        NeoForge.EVENT_BUS.addListener(ClientSetup::clientTick);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FISH.get(), FishRenderer::new);
        event.registerEntityRenderer(ModEntities.FISHING_FLOAT.get(), FishingFloatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AQUARIUM.get(), AquariumRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FISH_TROPHY.get(), FishTrophyRenderer::new);
    }

    private static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModMenus.FISH_BASKET.get(), FishBasketScreen::new);
        event.register(ModMenus.AQUARIUM.get(), AquariumScreen::new);
        event.register(ModMenus.FISH_PROCESSING_TABLE.get(), FishProcessingScreen::new);
        event.register(ModMenus.FISHING_MARKET.get(), FishingMarketScreen::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(CaucaFishing.MOD_ID, "fishing_hud"), FishingHud::render);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(REEL_KEY);
    }

    private static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (REEL_KEY.isDown() && minecraft.player.tickCount % 2 == 0) ModNetworking.sendReelPulse();
        if (CaucaFishing.JOURNAL_SCREEN_REQUESTED) {
            CaucaFishing.JOURNAL_SCREEN_REQUESTED = false;
            minecraft.setScreen(new JournalScreen());
        }
    }
}
