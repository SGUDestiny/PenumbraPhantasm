package destiny.penumbra_phantasm.client.dimension;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DarkDepthsDimensionEffects extends DimensionSpecialEffects {
    public static final ResourceLocation DARK_DEPTHS_EFFECT = new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths");
    protected VertexBuffer skyBuffer;
    public DarkDepthsDimensionEffects() {
        super(OverworldEffects.CLOUD_LEVEL, true, SkyType.NORMAL, false, false);
    }

/*    public static VertexBuffer createLightSky() {
        VertexBuffer skyBuffer = new VertexBuffer();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = buildSkyDisc(bufferbuilder, 16.0F);
        skyBuffer.bind();
        skyBuffer.upload(bufferbuilder$renderedbuffer);
        VertexBuffer.unbind();

        return skyBuffer;
    }*/

/*    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shaderinstance);

        return true;
    }*/

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float v) {
        return null;
    }

    @Override
    public boolean isFoggyAt(int i, int i1) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return false;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        return false;
    }
}
