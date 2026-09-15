package com.dungprovaidai.cauca.item;

import com.dungprovaidai.cauca.fishing.BaitProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

import java.util.List;

public class BaitItem extends Item {
    private final BaitProfile profile;

    public BaitItem(BaitProfile profile, Properties properties) {
        super(properties);
        this.profile = profile;
    }

    public BaitProfile profile() {
        return profile;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cauca_fishing.bait", profile.id()));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
