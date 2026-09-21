package destiny.penumbra_phantasm.client.render.fountain;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import destiny.penumbra_phantasm.client.render.ModShaders;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL46;

import java.util.Optional;

public final class FountainOpeningPosterizeRenderer {
    private static final int GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT;
    private static final int GL_NEAREST = GL11.GL_NEAREST;
    private static final int GL_READ_FRAMEBUFFER = GL46.GL_READ_FRAMEBUFFER;
    private static final int GL_DRAW_FRAMEBUFFER = GL46.GL_DRAW_FRAMEBUFFER;

    private static RenderTarget target;

    private FountainOpeningPosterizeRenderer() {}

    public static void render(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
        if (!(minecraft.level instanceof ClientLevel)) return;

        ShaderInstance shader = ModShaders.OPENING_POSTERIZE;
        if (shader == null) return;

        ClientLevel level = minecraft.level;
        Vec3 camPos = gameRenderer.getMainCamera().getPosition();
        Optional<DarkFountain> fountainOpt = FountainOpeningPosterize.findClosestOpeningFountain(level, camPos, partialTick);
        if (fountainOpt.isEmpty()) return;

        DarkFountain fountain = fountainOpt.get();

        float distance = FountainOpeningPosterize.distanceInBlocks(camPos, fountain.getFountainPos());
        float fade = FountainOpeningPosterize.distanceFade(distance);
        float tick = fountain.getOpeningTick(partialTick);
        float strength = FountainOpeningPosterize.strength(tick);
        float white = FountainOpeningPosterize.whiteLevel(tick);
        float strengthUniform = strength * fade * FountainRenderUtil.OPENING_POSTERIZE_STRENGTH_MAX;
        float whiteUniform = white * fade;

        if (strength <= 0 || fade <= 0) return;

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        ensureTarget(mainTarget);
        Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting savedSorting = RenderSystem.getVertexSorting();

        GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, target.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, mainTarget.width, mainTarget.height, 0, 0, target.width, target.height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

        mainTarget.bindWrite(false);

        RenderSystem.viewport(0, 0, mainTarget.width, mainTarget.height);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        GlStateManager._depthMask(false);
        GlStateManager._colorMask(true, true, true, false);

        Matrix4f ortho = new Matrix4f().setOrtho(0f, (float) mainTarget.width, (float) mainTarget.height, 0f, 1000f, 3000f);

        RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.setShader(() -> shader);

        shader.setSampler("Sampler0", target.getColorTextureId());

        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(new Matrix4f().translation(0f, 0f, -2000f));
        }

        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(ortho);
        }

        Uniform uStrength = shader.getUniform("Strength");
        if (uStrength != null) {
            uStrength.set(strengthUniform);
        }

        Uniform uWhite = shader.getUniform("WhiteLevel");
        if (uWhite != null) {
            uWhite.set(whiteUniform);
        }

        Uniform uThreshold = shader.getUniform("Threshold");
        if (uThreshold != null) {
            uThreshold.set(FountainRenderUtil.openingPosterizeLumaThresholdForCameraBlockLight(level, camPos, FountainRenderUtil.OPENING_POSTERIZE_LUMA_THRESHOLD));
        }

        shader.apply();

        BufferBuilder buffer = RenderSystem.renderThreadTesselator().getBuilder();

        float targetWidth = (float) mainTarget.width;
        float targetHeight = (float) mainTarget.height;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(0, targetHeight, 0).uv(0, 0).endVertex();
        buffer.vertex(targetWidth, targetHeight, 0).uv(1, 0).endVertex();
        buffer.vertex(targetWidth, 0, 0).uv(1, 1).endVertex();
        buffer.vertex(0, 0, 0).uv(0, 1).endVertex();

        BufferUploader.draw(buffer.end());

        shader.clear();

        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._depthMask(true);

        RenderSystem.setProjectionMatrix(savedProjection, savedSorting);
        RenderSystem.enableDepthTest();

        mainTarget.bindWrite(false);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void ensureTarget(RenderTarget main) {
        if (target == null) {
            target = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            target.setFilterMode(GL_NEAREST);
        } else if (target.width != main.width || target.height != main.height) {
            target.resize(main.width, main.height, Minecraft.ON_OSX);
            target.setFilterMode(GL_NEAREST);
        }
    }
}