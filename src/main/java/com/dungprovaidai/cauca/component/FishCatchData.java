package com.dungprovaidai.cauca.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Data component shared by the live bucket, processed meat and market. */
public record FishCatchData(
        String species,
        float weightKg,
        float lengthCm,
        int quality,
        int freshness,
        boolean alive
) {
    public static final Codec<FishCatchData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("species").forGetter(FishCatchData::species),
            Codec.FLOAT.fieldOf("weight_kg").forGetter(FishCatchData::weightKg),
            Codec.FLOAT.fieldOf("length_cm").forGetter(FishCatchData::lengthCm),
            Codec.INT.fieldOf("quality").forGetter(FishCatchData::quality),
            Codec.INT.fieldOf("freshness").forGetter(FishCatchData::freshness),
            Codec.BOOL.fieldOf("alive").forGetter(FishCatchData::alive)
    ).apply(instance, FishCatchData::new));

    public static final StreamCodec<ByteBuf, FishCatchData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FishCatchData::species,
            ByteBufCodecs.FLOAT, FishCatchData::weightKg,
            ByteBufCodecs.FLOAT, FishCatchData::lengthCm,
            ByteBufCodecs.VAR_INT, FishCatchData::quality,
            ByteBufCodecs.VAR_INT, FishCatchData::freshness,
            ByteBufCodecs.BOOL, FishCatchData::alive,
            FishCatchData::new
    );

    public FishCatchData withFreshness(int value) {
        return new FishCatchData(species, weightKg, lengthCm, quality, Math.max(0, value), alive);
    }

    public FishCatchData dead() {
        return new FishCatchData(species, weightKg, lengthCm, quality, freshness, false);
    }
}
