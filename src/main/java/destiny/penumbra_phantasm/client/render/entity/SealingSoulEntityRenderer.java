package destiny.penumbra_phantasm.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.server.entity.SealingSoulEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SealingSoulEntityRenderer extends EntityRenderer<SealingSoulEntity> {
    public SealingSoulEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(@NotNull SealingSoulEntity sealingSoulEntity, float entity, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int packedLight) {
        float tick = sealingSoulEntity.getTick() + partialTick;;

        float soulAlpha = 0f;
        float soulSize = 0.5f;
        float shineSize = 0f;

        float soulAppearStart = 0;
        float soulAppearDuration = 10f;
        float soulAppearDelta = (tick - soulAppearStart) / soulAppearDuration;
        if (tick < soulAppearStart + soulAppearDuration) {
            soulSize = Mth.lerp(soulAppearDelta, 0f, 0.5f);
        }

        float shineAppearDuration = 20f;
        float shineAppearDelta = (tick - soulAppearStart) / shineAppearDuration;
        if (tick < soulAppearStart + shineAppearDuration) {
            soulAlpha = Mth.lerp(shineAppearDelta, 1f, 0f);
            shineSize = Mth.lerp(shineAppearDelta, 0f, 4f);
        }

        float endingShineAppearStart = 4 * 20;
        float endingShineAppearDuration = 20;
        float endingShineDuration = (tick - endingShineAppearStart) / endingShineAppearDuration;
        if (tick >= endingShineAppearStart && tick < endingShineAppearStart + endingShineAppearDuration) {
            soulAlpha = Mth.lerp(endingShineDuration, 1f, 0f);
            shineSize = Mth.lerp(endingShineDuration, 0f, 4f);
        }

        //Soul
        pose.pushPose();
        pose.translate(0f, 0.5f, 0f);
        pose.scale(soulSize, soulSize, soulSize);

        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));

        Matrix4f soulMatrix = pose.last().pose();
        Matrix3f soulNormalMatrix = pose.last().normal();
        VertexConsumer soulConsumer = buffer.getBuffer(RenderTypes.fountain(getTextureLocation(sealingSoulEntity)));
        int overlay = OverlayTexture.NO_OVERLAY;

        soulConsumer.vertex(soulMatrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(overlay).uv2(packedLight).normal(soulNormalMatrix, 0, 0, 1).endVertex();
        soulConsumer.vertex(soulMatrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(overlay).uv2(packedLight).normal(soulNormalMatrix, 0, 0, 1).endVertex();
        soulConsumer.vertex(soulMatrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(overlay).uv2(packedLight).normal(soulNormalMatrix, 0, 0, 1).endVertex();
        soulConsumer.vertex(soulMatrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(overlay).uv2(packedLight).normal(soulNormalMatrix, 0, 0, 1).endVertex();
        pose.popPose();

        //Shine
        pose.pushPose();
        pose.translate(0f, 0.5f, 0f);
        pose.scale(shineSize, shineSize, shineSize);

        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));

        Matrix4f matrix = pose.last().pose();
        Matrix3f normalMatrix = pose.last().normal();
        VertexConsumer consumerA = buffer.getBuffer(RenderTypes.fountain(getTextureLocation(sealingSoulEntity)));

        consumerA.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, (int)(soulAlpha * 255)).uv(0, 1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumerA.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, (int)(soulAlpha * 255)).uv(1, 1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumerA.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, (int)(soulAlpha * 255)).uv(1, 0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumerA.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, (int)(soulAlpha * 255)).uv(0, 0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        pose.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SealingSoulEntity sealingSoulEntity) {
        int soulType = sealingSoulEntity.getEntityData().get(SealingSoulEntity.SOUL_TYPE_ENTITY_DATA);
        return new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");
    }
}
