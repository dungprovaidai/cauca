package com.dungprovaidai.cauca.block;

import com.dungprovaidai.cauca.block.entity.FishProcessingTableBlockEntity;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BlockEntityTicker;
import net.minecraft.world.level.block.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FishProcessingTableBlock extends FishContainerBlock {
    public FishProcessingTableBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FishProcessingTableBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide() && type == ModBlockEntities.FISH_PROCESSING_TABLE.get()
                ? (lvl, blockPos, blockState, blockEntity) -> FishProcessingTableBlockEntity.serverTick(lvl, blockPos, blockState, (FishProcessingTableBlockEntity) blockEntity) : null;
    }
}
