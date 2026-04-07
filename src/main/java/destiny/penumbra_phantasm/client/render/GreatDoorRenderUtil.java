package destiny.penumbra_phantasm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.model.great_door.GreatDoorClosedModel;
import destiny.penumbra_phantasm.client.render.model.great_door.GreatDoorOpenModel;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class GreatDoorRenderUtil {
    public static GreatDoorOpenModel greatDoorOpenModel;
    public static GreatDoorClosedModel greatDoorClosedModel;

    public static final ResourceLocation greatDoorLightWorldTexture = new ResourceLocation(PenumbraPhantasm.MODID, "textures/great_door/great_door_light_world.png");
    public static final ResourceLocation greatDoorDarkWorldTexture = new ResourceLocation(PenumbraPhantasm.MODID, "textures/great_door/great_door_dark_world.png");

    public static GreatDoorOpenModel getGreatDoorOpenModel() {
        if (greatDoorOpenModel == null)
            greatDoorOpenModel = new GreatDoorOpenModel(Minecraft.getInstance().getEntityModels().bakeLayer(GreatDoorOpenModel.LAYER_LOCATION));
        return greatDoorOpenModel;
    }

    public static GreatDoorClosedModel getGreatDoorClosedModel() {
        if (greatDoorClosedModel == null)
            greatDoorClosedModel = new GreatDoorClosedModel(Minecraft.getInstance().getEntityModels().bakeLayer(GreatDoorClosedModel.LAYER_LOCATION));
        return greatDoorClosedModel;
    }

    public static void renderOpenGreatDoor(GreatDoor greatDoor, PoseStack pose, MultiBufferSource buffer, int packedLight, int overlay) {
        if (greatDoor.destinationFountainDimension != null) {
            pose.pushPose();
            getGreatDoorOpenModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorDarkWorldTexture)),
                    LightTexture.block(packedLight), overlay, 1F, 1F, 1F, 1f);
            pose.popPose();
        } else {
            pose.pushPose();
            getGreatDoorOpenModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorLightWorldTexture)),
                    LightTexture.block(packedLight), overlay, 1F, 1F, 1F, 1f);
            pose.popPose();
        }
    }

    public static void renderClosedGreatDoor(GreatDoor greatDoor, PoseStack pose, MultiBufferSource buffer, int packedLight, int overlay) {
        if (greatDoor.destinationFountainDimension != null) {
            pose.pushPose();
            getGreatDoorClosedModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorDarkWorldTexture)),
                    LightTexture.block(packedLight), overlay, 1F, 1F, 1F, 1f);
            pose.popPose();
        } else {
            pose.pushPose();
            getGreatDoorClosedModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorLightWorldTexture)),
                    LightTexture.block(packedLight), overlay, 1F, 1F, 1F, 1f);
            pose.popPose();
        }
    }
}
