package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.server.block.entity.AnchorOfLightBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtils;

public class AnchorOfLightBlockEntityRenderer extends GeoBlockRenderer<AnchorOfLightBlockEntity> {
    public static final ResourceLocation ANCHOR_OF_LIGHT_GLOW = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/misc/anchor_of_light_glow.png");

    public AnchorOfLightBlockEntityRenderer() {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "anchor_of_light")));
    }

    @Override
    public void preRender(PoseStack poseStack, AnchorOfLightBlockEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.pushPose();

        GeoBone middleBone = this.model.getBone("Middle").orElse(null);
        if (middleBone == null) {
            poseStack.popPose();
            return;
        }

        poseStack.translate(0.5f, 0.5f, 0);

        Vector3d localPos = middleBone.getLocalPosition();
        poseStack.translate(localPos.x, localPos.y, localPos.z);

        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        float animationTicker = animatable.animationTicker + partialTick;
        float scale;
        if (animationTicker <= 10) {
            scale = Mth.lerp(animationTicker / 10f, 0f, 5f);
        } else if (animationTicker < 17) {
            scale = Mth.lerp((animationTicker - 10f) / 7f, 5f, 4f);
        } else {
            scale = 4f;
        }

        poseStack.scale(scale, scale, 1f);

        int overlay = OverlayTexture.NO_OVERLAY;
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.fountainShockwave(ANCHOR_OF_LIGHT_GLOW));

        consumer.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();

        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, AnchorOfLightBlockEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(poseState, this.blockRenderTranslations);
            Matrix4f worldState = new Matrix4f(localMatrix);
            BlockPos pos = this.animatable.getBlockPos();
            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(localMatrix);
            bone.setWorldSpaceMatrix(worldState.translate(new Vector3f((float)pos.getX(), (float)pos.getY(), (float)pos.getZ())));
        }

        int soulType = animatable.soulType;
        ResourceLocation anchorTexture = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/block/anchor_of_light_" + soulType + ".png");
        RenderType emissiveType = RenderTypes.getEmissiveRenderType(anchorTexture);
        VertexConsumer emissiveConsumer = bufferSource.getBuffer(emissiveType);

        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);
        super.renderCubesOfBone(poseStack, bone, emissiveConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);

        if (!isReRender) {
            this.applyRenderLayersForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }

        this.renderChildBones(poseStack, animatable, bone, emissiveType, bufferSource, emissiveConsumer, isReRender, partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(AnchorOfLightBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(AnchorOfLightBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
