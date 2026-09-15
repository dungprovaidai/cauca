package com.dungprovaidai.cauca.network;

import com.dungprovaidai.cauca.fishing.FishingService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ModNetworking {
    private ModNetworking() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ReelInputPayload.TYPE, ReelInputPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        FishingService.onReelInput(player, payload.reeling());
                    }
                });
    }

    /** Called only from physical client code. */
    public static void sendReelPulse() {
        PacketDistributor.sendToServer(new ReelInputPayload(true));
    }
}
