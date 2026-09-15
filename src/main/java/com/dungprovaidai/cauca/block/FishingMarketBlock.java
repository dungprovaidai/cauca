package com.dungprovaidai.cauca.block;

import com.dungprovaidai.cauca.block.entity.FishingMarketBlockEntity;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class FishingMarketBlock extends FishContainerBlock {
    public FishingMarketBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FishingMarketBlockEntity(pos, state); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown() && !level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof FishingMarketBlockEntity market) {
            int price = market.totalQuote();
            if (price > 0) {
                market.clearContent();
                ItemStack payment = new ItemStack(Items.EMERALD, Math.min(64, price));
                if (!serverPlayer.getInventory().add(payment)) serverPlayer.drop(payment, false);
                serverPlayer.sendSystemMessage(Component.translatable("message.cauca_fishing.market_paid", price));
            } else serverPlayer.sendSystemMessage(Component.translatable("message.cauca_fishing.market_empty"));
            return InteractionResult.CONSUME;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
