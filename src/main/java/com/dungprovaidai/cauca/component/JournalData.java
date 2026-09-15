package com.dungprovaidai.cauca.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.LinkedHashMap;
import java.util.Map;

/** Player-owned collection data. It is immutable at the attachment boundary, so every update syncs safely. */
public record JournalData(Map<String, JournalEntry> entries, int totalCatches, String bestSpot) {
    public static final JournalEntry EMPTY_ENTRY = new JournalEntry(false, 0.0F, 0.0F, "—", "—", "—");
    public static final JournalData EMPTY = new JournalData(Map.of(), 0, "—");

    public JournalData {
        entries = Map.copyOf(entries == null ? Map.of() : entries);
        bestSpot = bestSpot == null ? "—" : bestSpot;
    }

    public static final Codec<JournalEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("discovered").forGetter(JournalEntry::discovered),
            Codec.FLOAT.fieldOf("largest_weight").forGetter(JournalEntry::largestWeight),
            Codec.FLOAT.fieldOf("longest_length").forGetter(JournalEntry::longestLength),
            Codec.STRING.fieldOf("best_spot").forGetter(JournalEntry::bestSpot),
            Codec.STRING.fieldOf("best_bait").forGetter(JournalEntry::bestBait),
            Codec.STRING.fieldOf("best_time").forGetter(JournalEntry::bestTime)
    ).apply(instance, JournalEntry::new));

    public static final Codec<JournalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ENTRY_CODEC).fieldOf("entries").forGetter(JournalData::entries),
            Codec.INT.fieldOf("total_catches").forGetter(JournalData::totalCatches),
            Codec.STRING.fieldOf("best_spot").forGetter(JournalData::bestSpot)
    ).apply(instance, JournalData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JournalData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public JournalData decode(RegistryFriendlyByteBuf buffer) {
            Map<String, JournalEntry> entries = new LinkedHashMap<>();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                String id = ByteBufCodecs.STRING_UTF8.decode(buffer);
                entries.put(id, new JournalEntry(
                        buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(),
                        ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.STRING_UTF8.decode(buffer)));
            }
            return new JournalData(entries, buffer.readVarInt(), ByteBufCodecs.STRING_UTF8.decode(buffer));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, JournalData value) {
            buffer.writeVarInt(value.entries.size());
            value.entries.forEach((id, entry) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, id);
                buffer.writeBoolean(entry.discovered());
                buffer.writeFloat(entry.largestWeight());
                buffer.writeFloat(entry.longestLength());
                ByteBufCodecs.STRING_UTF8.encode(buffer, entry.bestSpot());
                ByteBufCodecs.STRING_UTF8.encode(buffer, entry.bestBait());
                ByteBufCodecs.STRING_UTF8.encode(buffer, entry.bestTime());
            });
            buffer.writeVarInt(value.totalCatches);
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.bestSpot);
        }
    };

    public JournalData recordCatch(String species, float weight, float length, String spot, String bait, String time) {
        Map<String, JournalEntry> updated = new LinkedHashMap<>(entries);
        JournalEntry old = updated.getOrDefault(species, EMPTY_ENTRY);
        String bestSpotValue = old.discovered() && old.bestSpot() != null ? old.bestSpot() : spot;
        JournalEntry next = new JournalEntry(
                true,
                Math.max(old.largestWeight(), weight),
                Math.max(old.longestLength(), length),
                weight > old.largestWeight() ? spot : bestSpotValue,
                weight > old.largestWeight() ? bait : old.bestBait(),
                weight > old.largestWeight() ? time : old.bestTime());
        updated.put(species, next);
        return new JournalData(updated, totalCatches + 1, spot);
    }

    public record JournalEntry(boolean discovered, float largestWeight, float longestLength,
                               String bestSpot, String bestBait, String bestTime) {}
}
