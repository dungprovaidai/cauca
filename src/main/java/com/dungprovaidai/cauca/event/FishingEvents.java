package com.dungprovaidai.cauca.event;

import com.dungprovaidai.cauca.component.JournalData;
import com.dungprovaidai.cauca.entity.FishEntity;
import com.dungprovaidai.cauca.entity.FishingFloatEntity;
import com.dungprovaidai.cauca.fishing.FishingService;
import com.dungprovaidai.cauca.fishing.FishingState;
import com.dungprovaidai.cauca.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FishingEvents {
    private FishingEvents() {}

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 5 != 0) return;
        FishingService.activeFloat(player).ifPresent(floatEntity -> {
            if (floatEntity.fishId() >= 0 && player.level().getEntity(floatEntity.fishId()) instanceof FishEntity fish) FishingService.syncState(player, fish);
        });
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) FishingService.activeFloat(player).ifPresent(floatEntity -> FishingService.releaseFish(null, floatEntity));
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        event.getEntity().setData(ModAttachments.JOURNAL, event.getOriginal().getData(ModAttachments.JOURNAL));
        event.getEntity().setData(ModAttachments.FISHING_STATE, FishingState.IDLE);
    }
}
