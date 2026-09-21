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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL46;

public final class FountainHueShiftRenderer {
    private static final int GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT;
    private static final int GL_NEAREST = GL11.GL_NEAREST;
    private static final int GL_READ_FRAMEBUFFER = GL46.GL_READ_FRAMEBUFFER;
    private static final int GL_DRAW_FRAMEBUFFER = GL46.GL_DRAW_FRAMEBUFFER;

    private static RenderTarget target;

    private FountainHueShiftRenderer() {}

    public static void render(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
        if (!(minecraft.level instanceof ClientLevel)) return;

        ShaderInstance shader = ModShaders.HUE_SHIFT;
        if (shader == null) return;

        ClientLevel level = minecraft.level;

        if (!DarkWorldUtil.isDarkWorld(level) || DarkWorldUtil.isDepths(level)) return;

        Vec3 camPos = gameRenderer.getMainCamera().getPosition();
        DarkFountain fountain = getClosestFountain(level, camPos);
        if (fountain == null) return;

        float distance = (float) Math.sqrt(camPos.distanceToSqr(Vec3.atLowerCornerOf(fountain.getFountainPos())));
        float fadeRange = FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_START - FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_END;
        float distanceFade = (FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_START - distance) / fadeRange;
        distanceFade = Math.max(0, Math.min(1, distanceFade));

        float sealingFade = 1f;
        if (fountain.sealingTick >= 0) {
            float sealDelta = Mth.clamp((fountain.sealingTick + partialTick) / (float) DarkFountain.SEAL_DURATION, 0, 1);

            sealingFade = 1 - (sealDelta * sealDelta);
        }

        float finalStrength = distanceFade * sealingFade;
        if (finalStrength <= 0) {
            return;
        }

        float fountainHue = ((level.getGameTime() + partialTick) * 0.003f) % 1;

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

    private static DarkFountain getClosestFountain(ClientLevel level, Vec3 camPos) {
        DarkFountainCapability cap = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).resolve().orElse(null);

        if (cap == null || cap.darkFountains.isEmpty()) return null;

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

        if (best == null) return null;

        double distance = Math.sqrt(bestDistanceSq);
        if (distance > FountainRenderUtil.FOUNTAIN_SCREEN_TINT_FADE_START) return null;

        return best;
    }
}