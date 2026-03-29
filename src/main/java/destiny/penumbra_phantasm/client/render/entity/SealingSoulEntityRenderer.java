package destiny.penumbra_phantasm.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
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
        int tick = sealingSoulEntity.getTick();

        float endingSoulAlpha = 0f;
        float endingSoulSize = 1f;
        float endingSoulSecondarySize = 0f;

        float endingSoulAppearStart = 0;
        float endingSoulAppearDuration = 10f;
        float endingSoulAppearDelta = (tick - endingSoulAppearStart) / endingSoulAppearDuration;
        if (tick < endingSoulAppearStart + endingSoulAppearDuration) {
            endingSoulSize = Mth.lerp(endingSoulAppearDelta, 0f, 0.5f);
        }

        float endingSoulSecondaryAppearDuration = 20f;
        float endingSoulSecondaryAppearDelta = (tick - endingSoulAppearStart) / endingSoulSecondaryAppearDuration;
        if (tick < endingSoulAppearStart + endingSoulSecondaryAppearDuration) {
            endingSoulAlpha = Mth.lerp(endingSoulSecondaryAppearDelta, 1f, 0f);
            endingSoulSecondarySize = Mth.lerp(endingSoulSecondaryAppearDelta, 0f, 5f);
        }

        float endingSoulShineStart = 4 * 20;
        float endingSoulShineDuration = 20;
        float endingSoulSecondaryShineDelta = (tick - endingSoulShineStart) / endingSoulShineDuration;
        if (tick >= endingSoulShineStart && tick < endingSoulShineStart + endingSoulShineDuration) {
            endingSoulAlpha = Mth.lerp(endingSoulSecondaryShineDelta, 1f, 0f);
            endingSoulSecondarySize = Mth.lerp(endingSoulSecondaryShineDelta, 0f, 5f);
        }

        pose.pushPose();
        pose.translate(0f, 0.5f, 0f);
        pose.scale(1.25f * endingSoulSize, 1.25f * endingSoulSize, 1.25f * endingSoulSize);
        renderSoul(sealingSoulEntity, pose, buffer, packedLight, 1);
        pose.popPose();

        pose.pushPose();
        pose.translate(0f, 0.5f, 0f);
        pose.scale(endingSoulSecondarySize, endingSoulSecondarySize, endingSoulSecondarySize);
        renderSoul(sealingSoulEntity, pose, buffer, packedLight, endingSoulAlpha);
        pose.popPose();
    }

    public void renderSoul(SealingSoulEntity sealingSoulEntity, PoseStack pose, MultiBufferSource buffer, int packedLight, float alpha) {
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));

        Matrix4f matrix = pose.last().pose();
        Matrix3f normalMatrix = pose.last().normal();
        VertexConsumer consumerA = buffer.getBuffer(RenderTypes.fountain(getTextureLocation(sealingSoulEntity)));
        int overlay = OverlayTexture.NO_OVERLAY;

        consumerA.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255 * alpha).uv(0, 1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumerA.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255 * alpha).uv(1, 1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumerA.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255 * alpha).uv(1, 0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumerA.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255 * alpha).uv(0, 0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SealingSoulEntity sealingSoulEntity) {
        int soulType = sealingSoulEntity.getEntityData().get(SealingSoulEntity.SOUL_TYPE_ENTITY_DATA);
        return new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");
    }
}
