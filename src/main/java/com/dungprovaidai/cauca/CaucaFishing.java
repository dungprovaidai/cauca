package com.dungprovaidai.cauca;

import com.dungprovaidai.cauca.network.ModNetworking;
import com.dungprovaidai.cauca.registry.ModAttachments;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import com.dungprovaidai.cauca.registry.ModBlocks;
import com.dungprovaidai.cauca.registry.ModComponents;
import com.dungprovaidai.cauca.registry.ModEntities;
import com.dungprovaidai.cauca.registry.ModItems;
import com.dungprovaidai.cauca.registry.ModMenus;
import com.dungprovaidai.cauca.event.FishingEvents;
import com.dungprovaidai.cauca.fishing.SpeciesManager;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import com.dungprovaidai.cauca.client.ClientSetup;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(CaucaFishing.MOD_ID)
public final class CaucaFishing {
    public static final String MOD_ID = "cauca_fishing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MOD_ID);

    /** Set by the common journal item and consumed by the physical-client event handler. */
    public static volatile boolean JOURNAL_SCREEN_REQUESTED = false;

    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> FISHING_TAB =
            CREATIVE_TABS.register("fishing", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cauca_fishing"))
                    .icon(() -> new ItemStack(ModItems.BASIC_ROD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BASIC_ROD.get());
                        output.accept(ModItems.REINFORCED_ROD.get());
                        output.accept(ModItems.SWIFT_ROD.get());
                        output.accept(ModItems.HEAVY_ROD.get());
                        output.accept(ModItems.DEEPWATER_ROD.get());
                        output.accept(ModItems.LUCKY_ROD.get());
                        output.accept(ModItems.WORM.get());
                        output.accept(ModItems.GRUB.get());
                        output.accept(ModItems.MINNOW.get());
                        output.accept(ModItems.SHRIMP.get());
                        output.accept(ModItems.GLOW_WORM.get());
                        output.accept(ModItems.FISH_MEAT_BAIT.get());
                        output.accept(ModItems.SPECIAL_BAIT.get());
                        output.accept(ModItems.BASIC_LINE.get());
                        output.accept(ModItems.STRONG_LINE.get());
                        output.accept(ModItems.FINE_LINE.get());
                        output.accept(ModItems.REINFORCED_LINE.get());
                        output.accept(ModItems.SMALL_HOOK.get());
                        output.accept(ModItems.MEDIUM_HOOK.get());
                        output.accept(ModItems.LARGE_HOOK.get());
                        output.accept(ModItems.BARBED_HOOK.get());
                        output.accept(ModItems.DEEP_HOOK.get());
                        output.accept(ModItems.FISH_NET.get());
                        output.accept(ModItems.FISH_BUCKET.get());
                        output.accept(ModItems.FISHING_KNIFE.get());
                        output.accept(ModItems.FISHERMANS_JOURNAL.get());
                        output.accept(ModItems.FISH_BASKET_ITEM.get());
                        output.accept(ModItems.AQUARIUM_ITEM.get());
                        output.accept(ModItems.FISH_PROCESSING_TABLE_ITEM.get());
                        output.accept(ModItems.FISHING_MARKET_ITEM.get());
                        output.accept(ModItems.FISH_MEAT.get());
                        output.accept(ModItems.SALTED_FISH.get());
                        output.accept(ModItems.SMOKED_FISH.get());
                        output.accept(ModItems.DRIED_FISH.get());
                        output.accept(ModItems.FROZEN_FISH.get());
                        output.accept(ModItems.FISH_TROPHY_ITEM.get());
                    }).build());

    // Force-load each registry holder class before its DeferredRegister is attached.
    @SuppressWarnings("unused")
    private static final Object REGISTRATION_LINK = new Object[] {
            ModItems.BASIC_ROD, ModBlocks.FISH_BASKET, ModEntities.FISH,
            ModBlockEntities.FISH_BASKET, ModMenus.FISH_BASKET,
            ModAttachments.FISHING_STATE, ModComponents.FISH_CATCH
    };

    public CaucaFishing(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);

        modEventBus.addListener(this::addCreativeFallback);
        modEventBus.addListener(ModNetworking::registerPayloads);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(FishingEvents.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(SpeciesManager::registerReloadListener);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.init(modEventBus));
    }

    private void addCreativeFallback(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        // The dedicated tab is populated above. This hook intentionally leaves vanilla tabs untouched.
    }
}
