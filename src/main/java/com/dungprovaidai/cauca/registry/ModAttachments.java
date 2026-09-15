package com.dungprovaidai.cauca.registry;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.component.JournalData;
import com.dungprovaidai.cauca.fishing.FishingState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModAttachments {
    private ModAttachments() {}

    /** Current fight state is transient but synced to the owning player for the HUD. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FishingState>> FISHING_STATE =
            CaucaFishing.ATTACHMENTS.register("fishing_state", () -> AttachmentType.builder(() -> FishingState.IDLE)
                    .sync(FishingState.STREAM_CODEC)
                    .build());

    /** The journal survives death and is synchronized so the client screen is presentation-only. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<JournalData>> JOURNAL =
            CaucaFishing.ATTACHMENTS.register("fisher_journal", () -> AttachmentType.builder(() -> JournalData.EMPTY)
                    .serialize(JournalData.CODEC)
                    .sync(JournalData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());
}
