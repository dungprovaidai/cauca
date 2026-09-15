package com.dungprovaidai.cauca.block;

import com.dungprovaidai.cauca.block.entity.FishBasketBlockEntity;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FishBasketBlock extends FishContainerBlock {
    public FishBasketBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FishBasketBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide() && type == ModBlockEntities.FISH_BASKET.get()
                ? (lvl, blockPos, blockState, blockEntity) -> FishBasketBlockEntity.serverTick(lvl, blockPos, blockState, (FishBasketBlockEntity) blockEntity) : null;
    }
}
