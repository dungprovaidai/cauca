package com.dungprovaidai.cauca.registry;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.entity.FishEntity;
import com.dungprovaidai.cauca.entity.FishingFloatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredHolder<EntityType<?>, EntityType<FishEntity>> FISH = CaucaFishing.ENTITY_TYPES.register(
            "fish", () -> EntityType.Builder.of(FishEntity::new, MobCategory.WATER_CREATURE)
                    .sized(0.75F, 0.35F)
                    .clientTrackingRange(64)
                    .updateInterval(2)
                    .build("fish"));

    public static final DeferredHolder<EntityType<?>, EntityType<FishingFloatEntity>> FISHING_FLOAT = CaucaFishing.ENTITY_TYPES.register(
            "fishing_float", () -> EntityType.Builder.of(FishingFloatEntity::new, MobCategory.MISC)
                    .sized(0.22F, 0.22F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("fishing_float"));
}
