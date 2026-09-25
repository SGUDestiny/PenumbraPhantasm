package destiny.penumbra_phantasm.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.server.item.BlackKnifeItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import static destiny.penumbra_phantasm.server.item.BlackKnifeItem.SWOON_READY_TICK;
import static destiny.penumbra_phantasm.server.item.BlackKnifeItem.SWOON_TICKER;

public class BlackKnifeItemRenderer extends GeoItemRenderer<BlackKnifeItem> {
    private static final ResourceLocation BLACK_KNIFE = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/item/black_knife.png");
    private static final ResourceLocation BLACK_KNIFE_POWERUP = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/item/black_knife_powerup.png");

    public BlackKnifeItemRenderer() {
        super(new DefaultedItemGeoModel<>(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "black_knife")));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, BlackKnifeItem animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        ItemStack stack = getCurrentItemStack();
        CompoundTag tag = stack.getTag();

        if (tag == null) return;

        int animationTicker = tag.getInt(SWOON_TICKER);

        float brightnessProgress;
        if (animationTicker < SWOON_READY_TICK && animationTicker > -1) {
            brightnessProgress = Mth.clamp(animationTicker / 40f, 0, 1);
        } else if (animationTicker >= SWOON_READY_TICK) {
            brightnessProgress = 1;
        } else {
            brightnessProgress = 0;
        }

        for (GeoBone bone : model.topLevelBones()) {
            if (!bone.getName().contains("emissive")) {
                RenderType translucentType = RenderType.entityTranslucent(BLACK_KNIFE, false);
                VertexConsumer cutoutConsumer = bufferSource.getBuffer(translucentType);

                renderCubesOfBone(poseStack, bone, cutoutConsumer, packedLight, packedOverlay,
                        red - brightnessProgress, green - brightnessProgress, blue - brightnessProgress, alpha);
            }
        }

        for (GeoBone bone : model.topLevelBones()) {
            if (bone.getName().contains("emissive")) {
                if (animationTicker > -1) {
                    RenderType translucentType = RenderTypes.getEmissiveRenderType(BLACK_KNIFE_POWERUP);
                    VertexConsumer translucentConsumer = bufferSource.getBuffer(translucentType);

                    renderCubesOfBone(poseStack, bone, translucentConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, red, green, blue, brightnessProgress);
                }
            }
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, BlackKnifeItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
