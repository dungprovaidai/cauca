package com.dungprovaidai.cauca.block.entity;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.registry.ModBlockEntities;
import com.dungprovaidai.cauca.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FishTrophyBlockEntity extends BlockEntity {
    private FishCatchData catchData;
    private String displayName = "";

    public FishTrophyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_TROPHY.get(), pos, state);
    }

    public FishCatchData catchData() { return catchData; }
    public String displayName() { return displayName; }
    public void setCatchData(FishCatchData data, String name) { catchData = data; displayName = name == null ? "" : name; setChanged(); }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Catch")) catchData = FishCatchData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag.get("Catch")).result().orElse(null);
        displayName = tag.getString("DisplayName");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (catchData != null) FishCatchData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, catchData).result().ifPresent(value -> tag.put("Catch", value));
        tag.putString("DisplayName", displayName);
        super.saveAdditional(tag, registries);
    }
}
