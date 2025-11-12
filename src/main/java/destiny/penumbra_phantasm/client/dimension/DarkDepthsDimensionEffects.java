package destiny.penumbra_phantasm.client.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DarkDepthsDimensionEffects extends DimensionSpecialEffects {
    public static final ResourceLocation DARK_DEPTHS_EFFECT = new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths");
    protected VertexBuffer skyBuffer;
    public DarkDepthsDimensionEffects() {
        super(OverworldEffects.CLOUD_LEVEL, true, SkyType.NORMAL, false, false);
        this.skyBuffer = createLightSky();
    }

    public static VertexBuffer createLightSky() {
        VertexBuffer skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = buildSkyDisc(bufferbuilder, 16.0F);
        skyBuffer.bind();
        skyBuffer.upload(bufferbuilder$renderedbuffer);
        VertexBuffer.unbind();

        return skyBuffer;
    }

    // Create the dark blue or black shading in the sky / the black circle below the horizon when in the void or below ground
    public static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder builder, float scale)
    {
        // invert the base radius based on the sign of scale to ensure the faces are facing the correct way.
        float baseRadius = 512.0F;
        float invertibleBaseRadius = Math.signum(scale) * baseRadius;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        // Create a circle with it's vertex centered by the player
        // the circle is further above / below the horizon depending on the scale
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        builder.vertex(0.0D, (double)scale, 0.0D).endVertex();
        // Create the circle
        for(int i = -180; i <= 180; i += 45)
        {
            float radians = (float) Math.toRadians(i);

            builder.vertex(invertibleBaseRadius * Mth.cos(radians), scale,
                    baseRadius * Mth.sin(radians)).endVertex();
        }

        return builder.end();
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        return true;
    }

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
