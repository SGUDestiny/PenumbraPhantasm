package destiny.penumbra_phantasm.client.render.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class MindscapeDimensionEffects extends DimensionSpecialEffects {
    public static final ResourceLocation MINDSCAPE_DIMENSION_EFFECTS = new ResourceLocation(PenumbraPhantasm.MODID, "mindscape_dimension_effects");

    private final VertexBuffer skyBuffer;
    private final VertexBuffer lowerSkyBuffer;

    public MindscapeDimensionEffects() {
        super(Float.NaN, false, SkyType.NONE, false, false);
        this.skyBuffer = createMindscapeSkyBuffer(0.1f);
        this.lowerSkyBuffer = createMindscapeSkyBuffer(-0.1f);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(0, 0, 0, 1);
        RenderSystem.setShader(GameRenderer::getPositionShader);

        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();

        setupFog.run();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(0, 0, 0, 1);
        RenderSystem.setShader(GameRenderer::getPositionShader);

        this.lowerSkyBuffer.bind();
        this.lowerSkyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.depthMask(true);

        return true;
    }

    public static VertexBuffer createMindscapeSkyBuffer(float scale) {
        VertexBuffer skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        BufferBuilder.RenderedBuffer renderedBuffer = DarkWorldDimensionEffects.buildDepthsSkyDisc(bufferBuilder, scale, 512);

        skyBuffer.bind();
        skyBuffer.upload(renderedBuffer);
        VertexBuffer.unbind();

        return skyBuffer;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float v) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int i, int i1) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true;
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
