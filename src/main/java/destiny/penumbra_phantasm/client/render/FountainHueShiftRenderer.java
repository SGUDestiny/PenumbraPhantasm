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
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class FountainHueShiftRenderer {
    private static final int GL_COLOR_BUFFER_BIT = 16384;
    private static final int GL_NEAREST = 9728;
    private static final int GL_READ_FRAMEBUFFER = 36008;
    private static final int GL_DRAW_FRAMEBUFFER = 36009;

    private static RenderTarget scratch;

    private FountainHueShiftRenderer() {}

    public static void render(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
        if (!(minecraft.level instanceof ClientLevel)) {
            return;
        }

        ShaderInstance shader = ModShaders.HUE_SHIFT;
        if (shader == null) {
            return;
        }

        ClientLevel level = minecraft.level;
        Vec3 camPos = gameRenderer.getMainCamera().getPosition();
        DarkFountain fountain = getClosestFountain(level, camPos);
        if (fountain == null) {
            return;
        }

        if (!DarkWorldUtil.isDarkWorld(level)) return;

        float distance = (float) Math.sqrt(camPos.distanceToSqr(Vec3.atLowerCornerOf(fountain.getFountainPos())));
        float fadeRange = FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_START - FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_END;
        float distanceFade = (FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_START - distance) / fadeRange;
        distanceFade = Math.max(0.0F, Math.min(1.0F, distanceFade));

        float sealingFade = 1.0F;
        if (fountain.sealingTick >= 0) {
            float sealDelta = Mth.clamp((fountain.sealingTick + partialTick) / (float) DarkFountain.SEAL_DURATION, 0.0F, 1.0F);
            sealingFade = 1.0F - sealDelta;
        }

        float finalStrength = distanceFade * sealingFade;
        if (finalStrength <= 0.0F) {
            return;
        }

        float fountainHue = ((level.getGameTime() + partialTick) * 0.003F) % 1.0F;

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

            Uniform uHueTarget = shader.getUniform("HueTarget");
            if (uHueTarget != null) {
                uHueTarget.set(fountainHue);
            }

            Uniform uStrength = shader.getUniform("Strength");
            if (uStrength != null) {
                uStrength.set(finalStrength);
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

    private static DarkFountain getClosestFountain(ClientLevel level, Vec3 camPos) {
        DarkFountainCapability cap = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).resolve().orElse(null);
        if (cap == null || cap.darkFountains.isEmpty()) {
            return null;
        }

        DarkFountain best = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (DarkFountain fountain : cap.darkFountains.values()) {
            double dx = camPos.x - (fountain.getFountainPos().getX() + 0.5);
            double dy = camPos.y - (fountain.getFountainPos().getY() + 0.5);
            double dz = camPos.z - (fountain.getFountainPos().getZ() + 0.5);
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < bestDistanceSq) {
                bestDistanceSq = distSq;
                best = fountain;
            }
        }

        if (best == null) {
            return null;
        }

        double distance = Math.sqrt(bestDistanceSq);
        if (distance > FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_START) {
            return null;
        }

        return best;
    }
}