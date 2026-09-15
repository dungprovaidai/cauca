package com.dungprovaidai.cauca.registry;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.block.entity.AquariumBlockEntity;
import com.dungprovaidai.cauca.block.entity.FishBasketBlockEntity;
import com.dungprovaidai.cauca.block.entity.FishProcessingTableBlockEntity;
import com.dungprovaidai.cauca.block.entity.FishTrophyBlockEntity;
import com.dungprovaidai.cauca.block.entity.FishingMarketBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishBasketBlockEntity>> FISH_BASKET =
            CaucaFishing.BLOCK_ENTITY_TYPES.register("fish_basket", () -> BlockEntityType.Builder.of(
                    FishBasketBlockEntity::new, ModBlocks.FISH_BASKET.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AquariumBlockEntity>> AQUARIUM =
            CaucaFishing.BLOCK_ENTITY_TYPES.register("aquarium", () -> BlockEntityType.Builder.of(
                    AquariumBlockEntity::new, ModBlocks.AQUARIUM.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishProcessingTableBlockEntity>> FISH_PROCESSING_TABLE =
            CaucaFishing.BLOCK_ENTITY_TYPES.register("fish_processing_table", () -> BlockEntityType.Builder.of(
                    FishProcessingTableBlockEntity::new, ModBlocks.FISH_PROCESSING_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishingMarketBlockEntity>> FISHING_MARKET =
            CaucaFishing.BLOCK_ENTITY_TYPES.register("fishing_market", () -> BlockEntityType.Builder.of(
                    FishingMarketBlockEntity::new, ModBlocks.FISHING_MARKET.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishTrophyBlockEntity>> FISH_TROPHY =
            CaucaFishing.BLOCK_ENTITY_TYPES.register("fish_trophy", () -> BlockEntityType.Builder.of(
                    FishTrophyBlockEntity::new, ModBlocks.FISH_TROPHY.get()).build(null));
}
