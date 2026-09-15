package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.menu.FishContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Vanilla-style stone/wood panel used by the common menus until textured art is supplied. */
public class FishContainerScreen<T extends FishContainerMenu> extends AbstractContainerScreen<T> {
    public FishContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, 0xFF2D2724);
        graphics.fill(left + 4, top + 4, left + imageWidth - 4, top + 78, 0xFF4A423B);
        graphics.fill(left + 4, top + 80, left + imageWidth - 4, top + imageHeight - 4, 0xFF3A342F);
        for (int i = 0; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            if (i < menu.containerSlotCount()) graphics.fill(left + slot.x - 1, top + slot.y - 1, left + slot.x + 17, top + slot.y + 17, 0xFF171513);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xFFF3E6C2, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0xFFD7D0C8, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
