package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.entity.FishEntity;
import com.dungprovaidai.cauca.entity.FishingFloatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Renders the bobber and the two visible line segments: rod to hook and hook to fish. */
public class FishingFloatRenderer extends EntityRenderer<FishingFloatEntity> {
    private final ItemRenderer itemRenderer;

    public FishingFloatRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.05F;
    }

    @Override
    public void render(FishingFloatEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.35F, 0.35F, 0.35F);
        itemRenderer.renderStatic(new ItemStack(Items.REDSTONE_TORCH), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();

        VertexConsumer line = buffer.getBuffer(RenderType.lines());
        Entity player = entity.anglerUUID() == null ? null : entity.level().getPlayerByUUID(entity.anglerUUID());
        if (player != null) drawLine(poseStack, line, 0.0F, 0.0F, 0.0F,
                (float) (player.getX() - entity.getX()), (float) (player.getEyeY() - entity.getY()), (float) (player.getZ() - entity.getZ()), 0.82F, 0.82F, 0.78F);
        if (entity.fishId() >= 0 && entity.level().getEntity(entity.fishId()) instanceof FishEntity fish) {
            drawLine(poseStack, line, 0.0F, 0.0F, 0.0F,
                    (float) (fish.getX() - entity.getX()), (float) (fish.getY() - entity.getY()), (float) (fish.getZ() - entity.getZ()), 0.92F, 0.92F, 0.86F);
        }
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer consumer, float x1, float y1, float z1,
                                 float x2, float y2, float z2, float red, float green, float blue) {
        PoseStack.Pose pose = poseStack.last();
        consumer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(FishingFloatEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/redstone_torch.png");
    }
}
