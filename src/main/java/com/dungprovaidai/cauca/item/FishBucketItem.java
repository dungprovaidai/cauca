package com.dungprovaidai.cauca.item;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.entity.FishEntity;
import com.dungprovaidai.cauca.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

public class FishBucketItem extends Item {
    public FishBucketItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack filled(FishCatchData data) {
        ItemStack stack = new ItemStack(com.dungprovaidai.cauca.registry.ModItems.FISH_BUCKET.get());
        stack.set(ModComponents.FISH_CATCH, data);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || level.getGameTime() % 20L != 0L) return;
        FishCatchData data = stack.get(ModComponents.FISH_CATCH);
        if (data != null && data.alive() && data.freshness() > 0) stack.set(ModComponents.FISH_CATCH, data.withFreshness(data.freshness() - 1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        FishCatchData data = stack.get(ModComponents.FISH_CATCH);
        if (data == null || !level.getFluidState(context.getClickedPos()).is(net.minecraft.tags.FluidTags.WATER)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            BlockPos pos = context.getClickedPos().above();
            FishEntity fish = FishEntity.fromCatch(com.dungprovaidai.cauca.registry.ModEntities.FISH.get(), level, data);
            fish.setPos(pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D);
            level.addFreshEntity(fish);
            if (!context.getPlayer().getAbilities().instabuild) {
                context.getPlayer().setItemInHand(context.getHand(), new ItemStack(net.minecraft.world.item.Items.BUCKET));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
