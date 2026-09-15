package com.dungprovaidai.cauca.item;

import com.dungprovaidai.cauca.fishing.FishingService;
import com.dungprovaidai.cauca.fishing.RodStats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CaucaFishingRodItem extends Item {
    private final RodStats stats;

    public CaucaFishingRodItem(RodStats stats, Properties properties) {
        super(properties.durability(stats.durability()));
        this.stats = stats;
    }

    public RodStats stats() {
        return stats;
    }

    /**
     * Right click is a deliberate, server-authoritative state transition:
     * idle -> cast, bite -> hook, fight -> reel pulse, landed -> detach.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            FishingService.handleRodAction(serverPlayer, hand, this, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
