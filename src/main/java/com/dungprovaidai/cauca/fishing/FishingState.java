package com.dungprovaidai.cauca.fishing;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Small synced HUD snapshot; the entity remains the authoritative source of fight physics. */
public record FishingState(FishingPhase phase, float tension, float stamina, String species, boolean reeling) {
    public static final FishingState IDLE = new FishingState(FishingPhase.IDLE, 0.0F, 0.0F, "", false);
    public static final StreamCodec<RegistryFriendlyByteBuf, FishingState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, state -> state.phase.ordinal(),
            ByteBufCodecs.FLOAT, FishingState::tension,
            ByteBufCodecs.FLOAT, FishingState::stamina,
            ByteBufCodecs.STRING_UTF8, FishingState::species,
            ByteBufCodecs.BOOL, FishingState::reeling,
            (phase, tension, stamina, species, reeling) -> new FishingState(
                    FishingPhase.values()[Math.max(0, Math.min(FishingPhase.values().length - 1, phase))],
                    tension, stamina, species, reeling));
}
