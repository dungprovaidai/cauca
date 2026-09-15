package com.dungprovaidai.cauca.network;

import com.dungprovaidai.cauca.CaucaFishing;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ReelInputPayload(boolean reeling) implements CustomPacketPayload {
    public static final Type<ReelInputPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CaucaFishing.MOD_ID, "reel_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReelInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ReelInputPayload::reeling, ReelInputPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
