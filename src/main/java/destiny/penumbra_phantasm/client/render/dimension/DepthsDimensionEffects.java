package destiny.penumbra_phantasm.client.render.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.ModShaders;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static destiny.penumbra_phantasm.client.render.dimension.CardKingdomDimensionEffects.SKY_DISC_HEIGHT;
import static destiny.penumbra_phantasm.client.render.dimension.DarkWorldDimensionEffects.createSkyBuffer;

public class DepthsDimensionEffects extends DimensionSpecialEffects {
    public static final ResourceLocation DEPTHS_DIMENSION_EFFECTS = new ResourceLocation(PenumbraPhantasm.MODID, "depths_dimension_effects");

    public static final ResourceLocation IMAGE_DEPTH = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth.png");
    public static final ResourceLocation WHITE_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/white_screen.png");

    private static final ResourceLocation[] TITAN_TEXTURES = new ResourceLocation[]{
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/titan_1.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/titan_2.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/titan_3.png")
    };
    private static final ResourceLocation[] DEBRIS_TEXTURES = new ResourceLocation[]{
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/debris_1.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/debris_2.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/debris_3.png")
    };

    public static final int MIN_TITAN_COUNT = 7;
    public static final int MAX_TITAN_COUNT = 12;
    public static final int MIN_DEBRIS_COUNT = 10;
    public static final int MAX_DEBRIS_COUNT = 13;

    private static final float HORIZON_OFFSET = -7.5F;
    private static final float SPRITE_SIZE = 24F;

    private static final float FLASH_RADIUS_SCALE = 2.25F;
    private static final int FLASH_SEGMENTS = 24;
    public static final float FLASH_Y = 12F;

    private static final float CYLINDER_RADIUS = 96F;

    private final VertexBuffer skyBuffer;
    private final VertexBuffer lowerSkyBuffer;
    private final VertexBuffer silhouettesBuffer;
    private final VertexBuffer flashesBuffer;
    private final VertexBuffer flashesBackgroundBuffer;

    private long spriteSeed = Long.MIN_VALUE;
    private List<Sprite> sprites = List.of();

    public DepthsDimensionEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
        this.skyBuffer = createSkyBuffer(SKY_DISC_HEIGHT);
        this.lowerSkyBuffer = createSkyBuffer(-SKY_DISC_HEIGHT);
        this.silhouettesBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.flashesBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.flashesBackgroundBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(0F, 0F, 0F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionShader);

        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        FogRenderer.levelFogColor();
        RenderSystem.setShaderFogStart(CYLINDER_RADIUS * 4F);
        RenderSystem.setShaderFogEnd(CYLINDER_RADIUS * 4.5F);

        this.renderFlashesBackground(level, partialTick, poseStack, projectionMatrix);
        this.renderFlashes(level, partialTick, poseStack, projectionMatrix);

        this.createSprites(level);

        this.renderSpriteQuads(poseStack, projectionMatrix);

        this.lowerSkyBuffer.bind();
        this.lowerSkyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.depthMask(true);

        return true;
    }

    private void createSprites(ClientLevel level) {
        long seed = DepthsSkyLightning.getSkySeed(level);
        if (seed == this.spriteSeed) {
            return;
        }

        List<Sprite> sprites = new ArrayList<>();
        sprites.addAll(createTitanSprites(seed));
        sprites.addAll(createDebrisSprites(seed));

        this.sprites = sprites;
        this.spriteSeed = seed;
    }

    private List<Sprite> createTitanSprites(long seed) {
        RandomSource random = RandomSource.create(seed);
        int count = Mth.nextInt(random, MIN_TITAN_COUNT, MAX_TITAN_COUNT);
        List<Sprite> placed = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            float azimuth = random.nextFloat() * ((float) (Math.PI * 2));
            int textureIndex = random.nextInt(TITAN_TEXTURES.length);

            Vec3 center = new Vec3(Mth.cos(azimuth) * CYLINDER_RADIUS, HORIZON_OFFSET + SPRITE_SIZE, Mth.sin(azimuth) * CYLINDER_RADIUS);

            placed.add(new Sprite(center, SPRITE_SIZE, SPRITE_SIZE, true, textureIndex));
        }

        return placed;
    }

    private List<Sprite> createTitanSpritesNew(long seed) {
        RandomSource random = RandomSource.create(seed);
        int count = Mth.nextInt(random, MIN_TITAN_COUNT, MAX_TITAN_COUNT);
        List<Sprite> placed = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            float azimuth = random.nextFloat() * ((float) (Math.PI * 2));

            int textureIndex = random.nextInt(TITAN_TEXTURES.length);

            boolean shouldPlace = false;
            for (Sprite sprite : placed) {
                if (shouldPlace) break;

                Vec3 center = new Vec3(Mth.cos(azimuth) * CYLINDER_RADIUS, HORIZON_OFFSET + SPRITE_SIZE, Mth.sin(azimuth) * CYLINDER_RADIUS);

                for (int tries = 0; tries < 15; tries++) {
                    if (center.distanceTo(sprite.center) < 20) {
                        azimuth = random.nextFloat() * ((float) (Math.PI * 2));
                        center = new Vec3(Mth.cos(azimuth) * CYLINDER_RADIUS, HORIZON_OFFSET + SPRITE_SIZE, Mth.sin(azimuth) * CYLINDER_RADIUS);
                    } else {
                        placed.add(new Sprite(center, SPRITE_SIZE, SPRITE_SIZE, true, textureIndex));
                        shouldPlace = true;
                        break;
                    }
                }
            }
        }

        return placed;
    }

    private List<Sprite> createDebrisSprites(long seed) {
        RandomSource random = RandomSource.create(seed);
        int count = Mth.nextInt(random, MIN_DEBRIS_COUNT, MAX_DEBRIS_COUNT);
        List<Sprite> placed = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            float azimuth = random.nextFloat() * ((float) (Math.PI * 2));
            int textureIndex = random.nextInt(DEBRIS_TEXTURES.length);

            Vec3 center = new Vec3(Mth.cos(azimuth) * CYLINDER_RADIUS, HORIZON_OFFSET + SPRITE_SIZE, Mth.sin(azimuth) * CYLINDER_RADIUS);

            placed.add(new Sprite(center, SPRITE_SIZE, SPRITE_SIZE, false, textureIndex));
        }

        return placed;
    }

    private void renderFlashes(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        boolean drawing = false;

        ActiveFlash flash = this.getActiveFlash(level, partialTick);
        if (flash != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            drawing = true;

            Vec3 flashCenter = new Vec3(Mth.cos(flash.event.azimuth()) * flash.event.radius(), FLASH_Y,
                    Mth.sin(flash.event.azimuth()) * flash.event.radius()
            );
            Basis basis = getUprightBasis(flashCenter);
            float radius = 16F * FLASH_RADIUS_SCALE;
            this.addFlashDisc(bufferBuilder, flashCenter, basis, radius, flash.alpha);
        }

        if (!drawing) {
            return;
        }

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        this.flashesBuffer.bind();
        this.flashesBuffer.upload(renderedBuffer);
        this.flashesBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
    }

    private void renderFlashesBackground(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        boolean drawing = false;

        ActiveFlash flash = this.getActiveFlash(level, partialTick);
        if (flash != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            drawing = true;

            Vec3 flashCenter = new Vec3(Mth.cos(flash.event.azimuth()) * flash.event.radius(), FLASH_Y,
                    Mth.sin(flash.event.azimuth()) * flash.event.radius()
            );
            Basis basis = getUprightBasis(flashCenter);
            float radius = 16F * FLASH_RADIUS_SCALE;
            this.addFlashDisc(bufferBuilder, flashCenter, basis, radius, flash.alpha);
        }

        if (!drawing) {
            return;
        }

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        this.flashesBackgroundBuffer.bind();
        this.flashesBackgroundBuffer.upload(renderedBuffer);

        Color middleColor = Color.getHSBColor(0f, 0f, 0.1f);
        ShaderInstance shaderInstance = ModShaders.FOUNTAIN_MASKED;

        if (shaderInstance != null) {
            float shadertime = (level.getGameTime()) * 0.01f;
            shaderInstance.safeGetUniform("Time").set(shadertime);
            Minecraft mc = Minecraft.getInstance();
            float aspect = (float) mc.getWindow().getWidth() /
                    (float) mc.getWindow().getHeight();

            shaderInstance.safeGetUniform("AspectRatio").set(aspect);

        }

        LocalPlayer player = Minecraft.getInstance().player;

        if(player != null) {
            float middleRed = middleColor.getRed() / 255f;
            float middleGreen = middleColor.getGreen() / 255f;
            float middleBlue = middleColor.getBlue() / 255f;

            float tintRed = 1f + (middleRed - 1f);
            float tintGreen = 1f + (middleGreen - 1f);
            float tintBlue = 1f + (middleBlue - 1f);

            if (shaderInstance != null) {
                shaderInstance.safeGetUniform("TintColor").set(
                        tintRed,
                        tintGreen,
                        tintBlue,
                        1f
                );
            }
        }

        RenderSystem.setShaderTexture(0, WHITE_SCREEN);
        RenderSystem.setShaderTexture(1, IMAGE_DEPTH);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        this.flashesBackgroundBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, ModShaders.FOUNTAIN_MASKED);

        VertexBuffer.unbind();
    }

    private void renderSpriteQuads(PoseStack poseStack, Matrix4f projectionMatrix) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        for (Sprite sprite : sprites) {
            int textureIndex = sprite.textureIndex;
            ResourceLocation texture = sprite.isTitan ? TITAN_TEXTURES[textureIndex] : DEBRIS_TEXTURES[textureIndex];

            RenderSystem.setShaderTexture(0, texture);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            Basis basis = getUprightBasis(sprite.center);
            this.addSilhouetteQuad(bufferBuilder, sprite.center, basis, sprite.width, sprite.height);

            BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
            this.silhouettesBuffer.bind();
            this.silhouettesBuffer.upload(renderedBuffer);
            this.silhouettesBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
            VertexBuffer.unbind();
        }
    }

    private ActiveFlash getActiveFlash(ClientLevel level, float partialTick) {
        long skySeed = DepthsSkyLightning.getSkySeed(level);
        double skyTime = level.getGameTime() + partialTick;
        long slot = (long) Math.floor(skyTime / DepthsSkyLightning.FLASH_PERIOD);
        ActiveFlash active = null;

        for (long currentSlot = slot - 1; currentSlot <= slot; currentSlot++) {
            DepthsSkyLightning.FlashEvent event = DepthsSkyLightning.createFlash(skySeed, currentSlot);
            if (event == null) {
                continue;
            }

            double eventTime = (skyTime - event.startTick()) / event.duration();
            if (eventTime < 0 || eventTime > 1) {
                continue;
            }

            float alpha = event.peak() * (1f - (float) eventTime);
            if (alpha > 0F && (active == null || alpha > active.alpha)) {
                active = new ActiveFlash(event, alpha);
            }
        }

        return active;
    }

    private void addFlashDisc(BufferBuilder bufferBuilder, Vec3 center, Basis basis, float radius, float alpha) {
        Vec3 flashCenter = center.add(center.normalize().scale(-0.25D));

        for (int i = 0; i < FLASH_SEGMENTS; i++) {
            float fromAngle = (float) ((Math.PI * 2f) * i / FLASH_SEGMENTS);
            float toAngle = (float) (Math.PI * 2f) * (i + 1) / FLASH_SEGMENTS;

            Vec3 from = flashCenter.add(basis.right.scale(Mth.cos(fromAngle) * radius)).add(basis.up.scale(Mth.sin(fromAngle) * radius));
            Vec3 to = flashCenter.add(basis.right.scale(Mth.cos(toAngle) * radius)).add(basis.up.scale(Mth.sin(toAngle) * radius));

            bufferBuilder.vertex((float) flashCenter.x, (float) flashCenter.y, (float) flashCenter.z).color(1f, 1f, 1f, alpha).endVertex();
            bufferBuilder.vertex((float) from.x, (float) from.y, (float) from.z).color(1f, 1, 1, 0f).endVertex();
            bufferBuilder.vertex((float) to.x, (float) to.y, (float) to.z).color(1f, 1f, 1f, 0f).endVertex();
        }
    }

    private void addSilhouetteQuad(BufferBuilder bufferBuilder, Vec3 center, Basis basis, float width, float height) {
        Vec3 horizontal = basis.right.scale(width);
        Vec3 vertical = basis.up.scale(height);
        Vec3 topLeft = center.subtract(horizontal).add(vertical);
        Vec3 bottomLeft = center.subtract(horizontal).subtract(vertical);
        Vec3 bottomRight = center.add(horizontal).subtract(vertical);
        Vec3 topRight = center.add(horizontal).add(vertical);

        bufferBuilder.vertex((float) topLeft.x, (float) topLeft.y, (float) topLeft.z).color(0f, 0f, 0f, 1f).uv(0f, 0f)
                .endVertex();
        bufferBuilder.vertex((float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).color(0f, 0f, 0f, 1f)
                .uv(0f, 1f).endVertex();
        bufferBuilder.vertex((float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).color(0f, 0f, 0f, 1f)
                .uv(1f, 1f).endVertex();
        bufferBuilder.vertex((float) topRight.x, (float) topRight.y, (float) topRight.z).color(0f, 0f, 0f, 1f)
                .uv(1f, 0f).endVertex();
    }

    private static Basis getUprightBasis(Vec3 center) {
        Vec3 towardCamera = center.scale(-1).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = up.cross(towardCamera).normalize();
        up = towardCamera.cross(right).normalize();

        return new Basis(right, up);
    }

    public static VertexBuffer createDepthsSkyBuffer(float scale) {
        VertexBuffer skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        BufferBuilder.RenderedBuffer renderedBuffer = DarkWorldDimensionEffects.buildDepthsSkyDisc(bufferBuilder, scale);

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

    private record Basis(Vec3 right, Vec3 up) {
    }

    private record Sprite(Vec3 center, float width, float height, boolean isTitan, int textureIndex) {
    }

    private record ActiveFlash(DepthsSkyLightning.FlashEvent event, float alpha) {
    }
}
