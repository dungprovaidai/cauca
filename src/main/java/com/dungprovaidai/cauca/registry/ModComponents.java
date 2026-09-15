package com.dungprovaidai.cauca.registry;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.component.FishCatchData;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModComponents {
    private ModComponents() {}

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FishCatchData>> FISH_CATCH =
            CaucaFishing.DATA_COMPONENTS.registerComponentType("fish_catch", builder -> builder
                    .persistent(FishCatchData.CODEC)
                    .networkSynchronized(FishCatchData.STREAM_CODEC));
}
