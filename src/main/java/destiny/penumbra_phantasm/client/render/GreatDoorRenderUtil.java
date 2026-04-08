package destiny.penumbra_phantasm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.model.great_door.GreatDoorClosedModel;
import destiny.penumbra_phantasm.client.render.model.great_door.GreatDoorOpenModel;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
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
        Direction direction = greatDoor.direction;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180f - direction.toYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        if (direction == Direction.EAST) {
            pose.translate(0, 0, -1);
        } else if (direction == Direction.SOUTH) {
            pose.translate(1, 0, -1);
        } else if (direction == Direction.WEST) {
            pose.translate(1, 0, 0);
        }
        if (greatDoor.lightDoorDimension != null && DarkWorldUtil.isDarkWorldKey(greatDoor.lightDoorDimension)) {
            getGreatDoorOpenModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorDarkWorldTexture)),
                    packedLight, overlay, 1F, 1F, 1F, 1f);
        } else {
            getGreatDoorOpenModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorLightWorldTexture)),
                    packedLight, overlay, 1F, 1F, 1F, 1f);
        }
        pose.popPose();
    }

    public static void renderClosedGreatDoor(GreatDoor greatDoor, PoseStack pose, MultiBufferSource buffer, int packedLight, int overlay) {
        Direction direction = greatDoor.direction;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180f - direction.toYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        if (direction == Direction.EAST) {
            pose.translate(0, 0, -1);
        } else if (direction == Direction.SOUTH) {
            pose.translate(1, 0, -1);
        } else if (direction == Direction.WEST) {
            pose.translate(1, 0, 0);
        }
        if (greatDoor.lightDoorDimension != null && DarkWorldUtil.isDarkWorldKey(greatDoor.lightDoorDimension)) {
            getGreatDoorClosedModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorDarkWorldTexture)),
                    packedLight, overlay, 1F, 1F, 1F, 1f);
        } else {
            getGreatDoorClosedModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.entityCutout(greatDoorLightWorldTexture)),
                    packedLight, overlay, 1F, 1F, 1F, 1f);
        }
        pose.popPose();
    }
}
