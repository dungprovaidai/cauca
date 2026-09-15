package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.block.entity.FishTrophyBlockEntity;
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

public class FishTrophyRenderer implements BlockEntityRenderer<FishTrophyBlockEntity> {
    private final ItemRenderer itemRenderer;
    public FishTrophyRenderer(BlockEntityRendererProvider.Context context) { itemRenderer = context.getItemRenderer(); }

    @Override
    public void render(FishTrophyBlockEntity trophy, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (trophy.catchData() == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.58F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees((trophy.getBlockPos().getX() + trophy.getBlockPos().getZ()) % 360));
        poseStack.scale(Math.max(0.45F, Math.min(1.4F, trophy.catchData().lengthCm() / 55.0F)), 0.8F, 0.35F);
        itemRenderer.renderStatic(new ItemStack(Items.COD), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, trophy.getLevel(), trophy.getBlockPos().hashCode());
        poseStack.popPose();
    }
}
