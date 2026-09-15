package com.dungprovaidai.cauca.item;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.registry.ModComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FishMeatItem extends Item {
    public FishMeatItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.35F).build()));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || level.getGameTime() % 20L != 0L) return;
        FishCatchData catchData = stack.get(ModComponents.FISH_CATCH);
        if (catchData == null || catchData.freshness() <= 0) return;
        // One freshness point is one in-game second; salted/smoked/dried/frozen variants use their own decay.
        stack.set(ModComponents.FISH_CATCH, catchData.withFreshness(catchData.freshness() - 1));
    }
}
