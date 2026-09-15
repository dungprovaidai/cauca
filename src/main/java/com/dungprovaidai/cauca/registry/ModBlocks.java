package com.dungprovaidai.cauca.registry;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.block.AquariumBlock;
import com.dungprovaidai.cauca.block.FishBasketBlock;
import com.dungprovaidai.cauca.block.FishProcessingTableBlock;
import com.dungprovaidai.cauca.block.FishTrophyBlock;
import com.dungprovaidai.cauca.block.FishingMarketBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredBlock<Block> FISH_BASKET = CaucaFishing.BLOCKS.register("fish_basket",
            () -> new FishBasketBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.2F).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> AQUARIUM = CaucaFishing.BLOCKS.register("aquarium",
            () -> new AquariumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.8F).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredBlock<Block> FISH_PROCESSING_TABLE = CaucaFishing.BLOCKS.register("fish_processing_table",
            () -> new FishProcessingTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> FISHING_MARKET = CaucaFishing.BLOCKS.register("fishing_market",
            () -> new FishingMarketBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> FISH_TROPHY = CaucaFishing.BLOCKS.register("fish_trophy",
            () -> new FishTrophyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noOcclusion()));
}
