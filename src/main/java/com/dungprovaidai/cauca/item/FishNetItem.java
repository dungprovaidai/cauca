package com.dungprovaidai.cauca.item;

import com.dungprovaidai.cauca.entity.FishEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FishNetItem extends Item {
    public FishNetItem(Properties properties) {
        super(properties.durability(96));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            FishEntity fish = level.getEntitiesOfClass(FishEntity.class, player.getBoundingBox().inflate(3.2D),
                    candidate -> candidate.isCapturable() && candidate.distanceToSqr(player) < 10.5D)
                    .stream().findFirst().orElse(null);
            if (fish != null) fish.capture(player, stack, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
