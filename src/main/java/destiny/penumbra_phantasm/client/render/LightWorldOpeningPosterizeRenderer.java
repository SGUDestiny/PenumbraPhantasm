package destiny.penumbra_phantasm.client.render;

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
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Optional;

public final class LightWorldOpeningPosterizeRenderer {

    private static final int GL_COLOR_BUFFER_BIT = 16384;
    private static final int GL_NEAREST = 9728;
    private static final int GL_READ_FRAMEBUFFER = 36008;
    private static final int GL_DRAW_FRAMEBUFFER = 36009;

    private static RenderTarget scratch;

    private LightWorldOpeningPosterizeRenderer() {
    }

    public static void render(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
        if (!(minecraft.level instanceof ClientLevel)) {
            return;
        }

        ShaderInstance shader = ModShaders.OPENING_POSTERIZE;
        if (shader == null) {
            return;
        }

        ClientLevel level = minecraft.level;
        Vec3 camPos = gameRenderer.getMainCamera().getPosition();
        Optional<DarkFountain> fountainOpt = LightWorldOpeningPosterize.findClosestOpeningFountain(level, camPos, partialTick);
        if (fountainOpt.isEmpty()) {
            return;
        }

        DarkFountain fountain = fountainOpt.get();

        float d = LightWorldOpeningPosterize.distanceInBlocks(camPos, fountain.getFountainPos());
        float fade = LightWorldOpeningPosterize.distanceFade(d);
        float tick = fountain.getOpeningTick(partialTick);
        float s = LightWorldOpeningPosterize.strength(tick);
        float w = LightWorldOpeningPosterize.whiteLevel(tick);
        float strengthUniform = s * fade * FountainRenderUtil.OPENING_POSTERIZE_STRENGTH_MAX;

        if (strengthUniform <= 1e-4f) {
            return;
        }

        float whiteUniform = w * fade;
        RenderTarget main = minecraft.getMainRenderTarget();
        ensureScratch(main);
        Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting savedSorting = RenderSystem.getVertexSorting();

        try {
            GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, main.frameBufferId);
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, scratch.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, main.width, main.height, 0, 0, scratch.width, scratch.height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
            GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

            main.bindWrite(false);

            RenderSystem.viewport(0, 0, main.width, main.height);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();

            GlStateManager._depthMask(false);
            GlStateManager._colorMask(true, true, true, false);

            Matrix4f ortho = new Matrix4f().setOrtho(0f, (float) main.width, (float) main.height, 0f, 1000f, 3000f);

            RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.setShader(() -> shader);

            shader.setSampler("Sampler0", scratch.getColorTextureId());

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
                uThreshold.set(FountainRenderUtil.OPENING_POSTERIZE_LUMA_THRESHOLD);
            }

            shader.apply();

            BufferBuilder buffer = RenderSystem.renderThreadTesselator().getBuilder();

            float pw = (float) main.width;
            float ph = (float) main.height;

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(0.0D, ph, 0.0D).uv(0.0F, 0.0F).endVertex();
            buffer.vertex(pw, ph, 0.0D).uv(1.0F, 0.0F).endVertex();
            buffer.vertex(pw, 0.0D, 0.0D).uv(1.0F, 1.0F).endVertex();
            buffer.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, 1.0F).endVertex();

            BufferUploader.draw(buffer.end());

            shader.clear();
        } finally {
            GlStateManager._colorMask(true, true, true, true);
            GlStateManager._depthMask(true);

            RenderSystem.setProjectionMatrix(savedProjection, savedSorting);
            RenderSystem.enableDepthTest();

            main.bindWrite(false);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    private static void ensureScratch(RenderTarget main) {
        if (scratch == null) {
            scratch = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            scratch.setFilterMode(GL_NEAREST);
        } else if (scratch.width != main.width || scratch.height != main.height) {
            scratch.resize(main.width, main.height, Minecraft.ON_OSX);
            scratch.setFilterMode(GL_NEAREST);
        }
    }
}
