package destiny.penumbra_phantasm.client.render.dimension;

import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DarkWorldDimensionEffects extends DimensionSpecialEffects {
    public static final ResourceLocation DARK_WORLD_DIMENSION_EFFECTS = new ResourceLocation(PenumbraPhantasm.MODID, "dark_world_dimension_effects");

    protected VertexBuffer skyBuffer;

    public DarkWorldDimensionEffects() {
        super(OverworldEffects.CLOUD_LEVEL, true, SkyType.NORMAL, false, false);
        this.skyBuffer = createDarkSky();
    }

    public static VertexBuffer createSkyBuffer(float scale) {
        VertexBuffer skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        BufferBuilder.RenderedBuffer renderedBuffer = DarkWorldDimensionEffects.buildSkyDisc(bufferBuilder, scale);
        skyBuffer.bind();
        skyBuffer.upload(renderedBuffer);
        VertexBuffer.unbind();

        return skyBuffer;
    }

    public static VertexBuffer createDarkSky() {
        VertexBuffer skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        BufferBuilder.RenderedBuffer renderedbuffer = buildSkyDisc(bufferbuilder, 16f);
        skyBuffer.bind();
        skyBuffer.upload(renderedbuffer);
        VertexBuffer.unbind();

        return skyBuffer;
    }

    public static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder builder, float scale) {
        float baseRadius = 512f;
        float invertibleBaseRadius = Math.signum(scale) * baseRadius;

        RenderSystem.setShader(GameRenderer::getPositionShader);

        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        builder.vertex(0, scale, 0).endVertex();

        for(int i = -180; i <= 180; i += 45) {
            float radians = (float) Math.toRadians(i);
            builder.vertex(invertibleBaseRadius * Mth.cos(radians), scale, baseRadius * Mth.sin(radians)).endVertex();
        }

        return builder.end();
    }

    public static BufferBuilder.RenderedBuffer buildDepthsSkyDisc(BufferBuilder builder, float scale) {
        float baseRadius = 512f;
        float invertibleBaseRadius = Math.signum(scale) * baseRadius;

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);

        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR_TEX);
        builder.vertex(0, scale, 0).color(255, 255, 255, 255).uv(0.5f, 0.5f).endVertex();

        for(int i = -180; i <= 180; i += 45) {
            float radians = (float) Math.toRadians(i);
            builder.vertex(invertibleBaseRadius * Mth.cos(radians), scale, baseRadius * Mth.sin(radians))
                    .color(255, 255, 255, 255).uv(0.5f, 0.5f).endVertex();
        }

        return builder.end();
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        Vec3 skyColor = level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), partialTick);
        float skyX = (float)skyColor.x;
        float skyY = (float)skyColor.y;
        float skyZ = (float)skyColor.z;
        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(skyX, skyY, skyZ, 1);
        RenderSystem.setShader(GameRenderer::getPositionShader);

        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.depthMask(true);

        return true;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float v) {
        return vec3.multiply(v * 0.94f + 0.06f, v * 0.94f + 0.06f, v * 0.91f + 0.09f);
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
