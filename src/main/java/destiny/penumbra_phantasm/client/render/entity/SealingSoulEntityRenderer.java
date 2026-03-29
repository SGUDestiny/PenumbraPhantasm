package destiny.penumbra_phantasm.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.entity.SealingSoulEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

public class SealingSoulEntityRenderer extends EntityRenderer<SealingSoulEntity> {
    public static final ResourceLocation WHITE_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/white_screen.png");

    public SealingSoulEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(@NotNull SealingSoulEntity sealingSoulEntity, float entity, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int packedLight) {
        pose.pushPose();

        pose.translate(0.5f, 0.5f, 0.5f);

        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));

        VertexConsumer consumerA = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(sealingSoulEntity)));

        consumerA.vertex(pose.last().pose(), -0.5f, -0.5f, 0).color(FastColor.ABGR32.color(255, 255, 255, 255)).uv(0, 1).endVertex();
        consumerA.vertex(pose.last().pose(), 0.5f, -0.5f, 0).color(FastColor.ABGR32.color(255, 255, 255, 255)).uv(1, 1).endVertex();
        consumerA.vertex(pose.last().pose(), 0.5f, 0.5f, 0).color(FastColor.ABGR32.color(255, 255, 255, 255)).uv(1, 0).endVertex();
        consumerA.vertex(pose.last().pose(), -0.5f, 0.5f, 0).color(FastColor.ABGR32.color(255, 255, 255, 255)).uv(0, 0).endVertex();

        pose.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SealingSoulEntity sealingSoulEntity) {
        return new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + sealingSoulEntity.soulType + ".png");
    }
}
