package destiny.penumbra_phantasm.client.render.model.fountain;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class DarkFountainOpeningStartModel extends EntityModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_opening_start_model"), "main");
    public final ModelPart main;

    public DarkFountainOpeningStartModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, -96).addBox(0.0F, -64.0F, -48.0F, 0.0F, 128.0F, 96.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -64.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r2 = main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, -96).addBox(0.0F, -64.0F, -48.0F, 0.0F, 128.0F, 96.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -64.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 192, -128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
