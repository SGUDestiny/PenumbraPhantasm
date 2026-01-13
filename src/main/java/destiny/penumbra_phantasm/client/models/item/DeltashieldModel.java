package destiny.penumbra_phantasm.client.models.item;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class DeltashieldModel extends Model {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "deltashield"), "main");
	private final ModelPart bone;

	public DeltashieldModel(ModelPart root) {
		super(RenderType::entityCutout);
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, -2.0F, 16.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-3.0F, 3.0F, -2.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 18).addBox(-2.0F, 5.0F, -2.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-1.0F, 6.0F, -2.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 22).addBox(0.0F, 7.0F, -2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(1.0F, 8.0F, -2.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 11).addBox(-4.0F, 0.0F, -2.0F, 14.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(29, 34).addBox(-2.5F, -6.0F, -2.25F, 11.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(34, 0).addBox(5.0F, -4.0F, -1.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(34, 0).addBox(0.0F, -4.0F, -1.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0, 0));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}