package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.fishing.FishingState;
import com.dungprovaidai.cauca.registry.ModAttachments;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class FishingHud {
    private FishingHud() {}

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        FishingState state = player.getData(ModAttachments.FISHING_STATE);
        if (state == null || state.phase() == com.dungprovaidai.cauca.fishing.FishingPhase.IDLE) return;

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int left = width / 2 - 92;
        int top = height - 62;
        graphics.fill(left, top, left + 184, top + 52, 0xAA101820);
        graphics.drawString(minecraft.font, Component.translatable("hud.cauca_fishing.phase." + state.phase().name().toLowerCase(java.util.Locale.ROOT)), left + 8, top + 6, 0xFFF3E6C2, true);
        if (!state.species().isBlank()) graphics.drawString(minecraft.font, Component.literal(state.species()), left + 8, top + 18, 0xFFD9E5EF, false);

        int barLeft = left + 8;
        int barTop = top + 34;
        int barWidth = 168;
        graphics.fill(barLeft, barTop, barLeft + barWidth, barTop + 6, 0xFF382C2B);
        graphics.fill(barLeft, barTop, barLeft + Mth.ceil(barWidth * Mth.clamp(state.tension() / 100.0F, 0.0F, 1.0F)), barTop + 6, 0xFFD46B57);
        graphics.drawString(minecraft.font, Component.translatable("hud.cauca_fishing.tension"), barLeft, barTop - 10, 0xFFE9C46A, false);
        graphics.drawString(minecraft.font, Component.translatable("hud.cauca_fishing.stamina"), barLeft + 90, barTop - 10, 0xFF9FD6A5, false);
    }
}
