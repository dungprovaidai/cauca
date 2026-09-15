package com.dungprovaidai.cauca.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.dungprovaidai.cauca.block.entity.AquariumBlockEntity;
import com.dungprovaidai.cauca.block.entity.FishBasketBlockEntity;
import com.dungprovaidai.cauca.registry.ModComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Shared menu interaction for server-authoritative fish containers. */
public abstract class FishContainerBlock extends Block implements EntityBlock {
    protected FishContainerBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        MenuProvider provider = getMenuProvider(state, level, pos);
        if (provider == null) return InteractionResult.PASS;
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) serverPlayer.openMenu(provider);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FishBasketBlockEntity basket) {
            if (stack.is(Items.WATER_BUCKET)) {
                if (!level.isClientSide()) {
                    basket.addWater();
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
            var data = stack.get(ModComponents.FISH_CATCH);
            if (data != null && data.alive() && !level.isClientSide() && basket.addLiveFish(data)) {
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return ItemInteractionResult.SUCCESS;
            }
        } else if (level.getBlockEntity(pos) instanceof AquariumBlockEntity aquarium) {
            if (stack.is(Items.WATER_BUCKET)) {
                if (!level.isClientSide()) {
                    aquarium.addWater();
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
            var data = stack.get(ModComponents.FISH_CATCH);
            if (data != null && data.alive() && !level.isClientSide() && aquarium.addFish(data)) {
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider provider ? provider : null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean movedByPiston) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, replacement, movedByPiston);
    }
}
