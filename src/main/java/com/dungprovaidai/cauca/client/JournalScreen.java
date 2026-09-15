package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.component.JournalData;
import com.dungprovaidai.cauca.registry.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class JournalScreen extends Screen {
    public JournalScreen() { super(Component.translatable("item.cauca_fishing.fishermans_journal")); }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 145;
        int top = height / 2 - 95;
        graphics.fill(left, top, left + 290, top + 190, 0xFF332A23);
        graphics.fill(left + 5, top + 5, left + 285, top + 185, 0xFFEEE1C6);
        graphics.drawString(font, title, left + 14, top + 13, 0xFF3E2E22, false);
        if (Minecraft.getInstance().player != null) {
            JournalData journal = Minecraft.getInstance().player.getData(ModAttachments.JOURNAL);
            graphics.drawString(font, Component.translatable("screen.cauca_fishing.records", journal.totalCatches()), left + 14, top + 28, 0xFF604B3A, false);
            int y = top + 44;
            int count = 0;
            for (var entry : journal.entries().entrySet()) {
                graphics.drawString(font, Component.literal(entry.getKey()), left + 14, y, 0xFF2D2520, false);
                graphics.drawString(font, Component.translatable("screen.cauca_fishing.record", entry.getValue().largestWeight(), entry.getValue().longestLength()), left + 122, y, 0xFF604B3A, false);
                y += 16;
                if (++count >= 8) break;
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
