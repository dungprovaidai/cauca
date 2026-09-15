package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.entity.FishEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/** Uses the vanilla cod model as a safe fallback until datapack species models are supplied. */
public class FishRenderer extends EntityRenderer<FishEntity> {
    private final ItemRenderer itemRenderer;

    public FishRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.18F;
    }

    @Override
    public void render(FishEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(entity.renderScale(), entity.renderScale(), entity.renderScale());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((entity.tickCount + partialTick) * 0.18F) * 7.0F));
        itemRenderer.renderStatic(new ItemStack(Items.COD), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FishEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/cod.png");
    }
}
