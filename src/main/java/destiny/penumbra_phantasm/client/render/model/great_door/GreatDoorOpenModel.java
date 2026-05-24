package destiny.penumbra_phantasm.client.render.model.great_door;

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

public class GreatDoorOpenModel extends EntityModel<Entity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "great_door_open_model"), "main");
	private final ModelPart main;
	private final ModelPart door_right;
	private final ModelPart door_left;
	private final ModelPart frame;
	private final ModelPart inside;
	private final ModelPart deco;
	private final ModelPart front_deco;
	private final ModelPart right_deco;
	private final ModelPart left_deco;

	public GreatDoorOpenModel(ModelPart root) {
		this.main = root.getChild("main");
		this.door_right = this.main.getChild("door_right");
		this.door_left = this.main.getChild("door_left");
		this.frame = this.main.getChild("frame");
		this.inside = this.frame.getChild("inside");
		this.deco = this.main.getChild("deco");
		this.front_deco = this.deco.getChild("front_deco");
		this.right_deco = this.deco.getChild("right_deco");
		this.left_deco = this.deco.getChild("left_deco");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(-91.0F, 0.0F, 0.0F));

		PartDefinition door_right = main.addOrReplaceChild("door_right", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition door_right_r1 = door_right.addOrReplaceChild("door_right_r1", CubeListBuilder.create().texOffs(256, 0).mirror().addBox(1.0F, -144.0F, 0.0F, 43.0F, 144.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition door_left = main.addOrReplaceChild("door_left", CubeListBuilder.create().texOffs(256, 0).addBox(-43.0F, -144.0F, 0.0F, 43.0F, 144.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(86.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition frame = main.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(0, 0).addBox(-48.0F, -144.0F, -8.0F, 96.0F, 144.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(43.0F, 0.0F, 8.0F));

		PartDefinition inside = frame.addOrReplaceChild("inside", CubeListBuilder.create().texOffs(64, 176).addBox(-48.0F, -144.0F, -8.0F, 96.0F, 144.0F, 0.0F, new CubeDeformation(0.01F))
				.texOffs(-32, 464).addBox(-48.0F, -144.0F, -8.0F, 96.0F, 0.0F, 32.0F, new CubeDeformation(0.01F))
				.texOffs(256, 464).addBox(-48.0F, 0.0F, -8.0F, 96.0F, 0.0F, 32.0F, new CubeDeformation(0.01F))
				.texOffs(32, 144).addBox(48.0F, -144.0F, -8.0F, 0.0F, 144.0F, 32.0F, new CubeDeformation(0.01F))
				.texOffs(0, 144).addBox(-48.0F, -144.0F, -8.0F, 0.0F, 144.0F, 32.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition deco = main.addOrReplaceChild("deco", CubeListBuilder.create(), PartPose.offset(-4.975F, -76.5F, -16.25F));

		PartDefinition front_deco = deco.addOrReplaceChild("front_deco", CubeListBuilder.create().texOffs(288, 311).addBox(0.0F, -76.5F, 16.225F, 96.0F, 153.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.025F, 0.0F, 0.0F));

		PartDefinition right_deco = deco.addOrReplaceChild("right_deco", CubeListBuilder.create().texOffs(269, 108).addBox(0.0F, -76.5F, -19.775F, 0.0F, 153.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_deco = deco.addOrReplaceChild("left_deco", CubeListBuilder.create().texOffs(269, 108).addBox(0.0F, -76.5F, -19.775F, 0.0F, 153.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(95.95F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}