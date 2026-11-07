package destiny.penumbra_phantasm.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypeRegistry;
import destiny.penumbra_phantasm.server.entity.DEVICE_FRIEND;
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

public class DEVICE_FRIEND_RENDERER extends EntityRenderer<DEVICE_FRIEND> {
    public DEVICE_FRIEND_RENDERER(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(@NotNull DEVICE_FRIEND friend, float entity, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int packedLight) {
        int disappearSilentTicker = friend.getDisappearSilentTicker();
        int disappearLaughTicker = friend.getDisappearLaughTicker();

        float alpha;
        if (disappearLaughTicker > -1) {
            alpha = Mth.lerp(disappearLaughTicker / 40f, 1f, 0f);
        } else if (disappearSilentTicker > -1) {
            alpha = Mth.lerp(disappearSilentTicker / 20f, 1f, 0f);
        } else {
            alpha = 1f;
        }

        pose.pushPose();

        pose.translate(0f, 0.5f, 0f);
        pose.scale(3f, 3f, 3f);

        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));

        Matrix4f poseMatrix = pose.last().pose();
        Matrix3f poseNormalMatrix = pose.last().normal();
        VertexConsumer friendConsumer = buffer.getBuffer(RenderTypeRegistry.getEmissiveRenderType(getTextureLocation(friend)));
        int overlay = OverlayTexture.NO_OVERLAY;

        friendConsumer.vertex(poseMatrix, -0.5f, -0.5f, 0).color(1f, 1f, 1f, alpha).uv(0, 1).overlayCoords(overlay).uv2(packedLight)
                .normal(poseNormalMatrix, 0, 0, 1).endVertex();
        friendConsumer.vertex(poseMatrix, 0.5f, -0.5f, 0).color(1f, 1f, 1f, alpha).uv(1, 1).overlayCoords(overlay).uv2(packedLight)
                .normal(poseNormalMatrix, 0, 0, 1).endVertex();
        friendConsumer.vertex(poseMatrix, 0.5f, 0.5f, 0).color(1f, 1f, 1f, alpha).uv(1, 0).overlayCoords(overlay).uv2(packedLight)
                .normal(poseNormalMatrix, 0, 0, 1).endVertex();
        friendConsumer.vertex(poseMatrix, -0.5f, 0.5f, 0).color(1f, 1f, 1f, alpha).uv(0, 0).overlayCoords(overlay).uv2(packedLight)
                .normal(poseNormalMatrix, 0, 0, 1).endVertex();

        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DEVICE_FRIEND friend) {
        int animationTicker = friend.getAnimationTicker();
        int frame = animationTicker < 5 ? 1 : 0;

        String action = friend.getDisappearLaughTicker() > -1 ? "laugh" : "idle";

        return ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/entity/device_friend_" + action + "_" + frame + ".png");
    }
}
