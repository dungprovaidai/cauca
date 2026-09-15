package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.block.entity.AquariumBlockEntity;
import com.dungprovaidai.cauca.registry.ModComponents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class AquariumRenderer implements BlockEntityRenderer<AquariumBlockEntity> {
    private final ItemRenderer itemRenderer;

    public AquariumRenderer(BlockEntityRendererProvider.Context context) { itemRenderer = context.getItemRenderer(); }

    @Override
    public void render(AquariumBlockEntity aquarium, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float phase = aquarium.swimTime() + partialTick * 0.08F;
        for (int slot = 0; slot < aquarium.getContainerSize(); slot++) {
            var data = aquarium.getItem(slot).get(ModComponents.FISH_CATCH);
            if (data == null || !data.alive()) continue;
            poseStack.pushPose();
            float angle = phase + slot * 1.7F;
            poseStack.translate(0.5F + (float) Math.cos(angle) * 0.27F, 0.42F + (slot % 3) * 0.12F, 0.5F + (float) Math.sin(angle) * 0.27F);
            poseStack.mulPose(Axis.YP.rotation(angle));
            poseStack.scale(0.22F + Math.min(0.16F, data.lengthCm() / 500.0F), 0.22F, 0.22F);
            itemRenderer.renderStatic(new ItemStack(Items.COD), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, aquarium.getLevel(), aquarium.getBlockPos().hashCode() + slot);
            poseStack.popPose();
        }
    }
}
