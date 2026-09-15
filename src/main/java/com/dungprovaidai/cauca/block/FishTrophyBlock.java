package com.dungprovaidai.cauca.block;

import com.dungprovaidai.cauca.block.entity.FishTrophyBlockEntity;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class FishTrophyBlock extends Block implements EntityBlock {
    public FishTrophyBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FishTrophyBlockEntity(pos, state); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof FishTrophyBlockEntity trophy) {
            if (!trophy.displayName().isBlank()) serverPlayer.sendSystemMessage(Component.literal(trophy.displayName()));
            else if (trophy.catchData() != null) serverPlayer.sendSystemMessage(Component.translatable("message.cauca_fishing.trophy", trophy.catchData().species(), trophy.catchData().weightKg()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
