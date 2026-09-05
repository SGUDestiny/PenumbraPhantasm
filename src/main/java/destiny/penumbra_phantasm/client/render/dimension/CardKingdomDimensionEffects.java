package destiny.penumbra_phantasm.client.render.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class CardKingdomDimensionEffects extends DarkWorldDimensionEffects {
    public static final ResourceLocation CARD_KINGDOM_DIMENSION_EFFECTS = new ResourceLocation(PenumbraPhantasm.MODID, "card_kingdom_dimension_effects");

    private static CardKingdomDimensionEffects instance;

    private static final ResourceLocation[] STAR_TEXTURES = new ResourceLocation[]{
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/star_1.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/star_2.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/star_3.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/star_4.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/star_5.png")
    };

    public static final float SKY_DISC_HEIGHT = 16f;
    private static final float TWO_PI = (float) (Math.PI * 2);

    private static final int[] STAR_TYPE_WEIGHTS = new int[]{48, 24, 12, 6, 3};
    private static final float STAR_DISTANCE = 96f;
    private static final int STAR_COUNT = 250;
    private static final float STAR_HUE = 220f / 360f;
    private static final float STAR_MAX_SATURATION = 0.75f;
    private static final float STAR_MIN_VALUE = 0.12f;
    private static final float STAR_TWINKLE_PERIOD_TICKS = 100f;

    private static final int SHOOTING_STAR_PERIOD = 90;
    private static final float SHOOTING_STAR_CHANCE = 0.75f;
    private static final int SHOOTING_STAR_MIN_DURATION = 25;
    private static final int SHOOTING_STAR_MAX_DURATION = 40;
    private static final float SHOOTING_STAR_MIN_ARC_ANGLE = 0.84f;
    private static final float SHOOTING_STAR_MAX_ARC_ANGLE = 1.56f;

    private final VertexBuffer lowerSkyBuffer;
    private final VertexBuffer[] staticStarBuffers = new VertexBuffer[STAR_TEXTURES.length];
    private final VertexBuffer dynamicTexturedBuffer;
    private final VertexBuffer dynamicColorBuffer;

    private long starSeed = Long.MIN_VALUE;

    private List<Star> staticStars = List.of();

    public CardKingdomDimensionEffects() {
        instance = this;
        this.lowerSkyBuffer = createSkyBuffer(-SKY_DISC_HEIGHT);

        for (int i = 0; i < this.staticStarBuffers.length; i++) {
            this.staticStarBuffers[i] = new VertexBuffer(VertexBuffer.Usage.STATIC);
        }

        this.dynamicTexturedBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.dynamicColorBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    }

    public static CardKingdomDimensionEffects getInstance() {
        return instance;
    }

    public static boolean isCardKingdomDarkWorld(ClientLevel level) {
        String path = level.dimension().location().getPath();
        return path.contains("card_kingdom") && !path.contains("egg_room");
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0, 0, 0, 1);

        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        this.lowerSkyBuffer.bind();
        this.lowerSkyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.depthMask(true);

        return true;
    }

    public void renderOverlay(ClientLevel level, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix) {
        this.createStars(level);

        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderFogStart(STAR_DISTANCE * 4.0F);
        RenderSystem.setShaderFogEnd(STAR_DISTANCE * 4.5F);

        this.renderStars(level, partialTick, poseStack, projectionMatrix);
        this.renderShootingStars(level, partialTick, poseStack, projectionMatrix);
        this.renderBiomeRing(level, camera, poseStack, projectionMatrix);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private void createStars(ClientLevel level) {
        long seed = this.getSkySeed(level);
        if (seed == this.starSeed) {
            return;
        }

        RandomSource random = RandomSource.create(seed);
        List<Star> stars = new ArrayList<>(STAR_COUNT);

        for (int i = 0; i < STAR_COUNT; i++) {
            float y = Mth.lerp(random.nextFloat(), 0.1f, 0.96f);
            float horizontal = Mth.sqrt(1 - y * y);
            float angle = random.nextFloat() * TWO_PI;

            Vec3 direction = new Vec3(Mth.cos(angle) * horizontal, y, Mth.sin(angle) * horizontal);

            int textureIndex = chooseWeightedTexture(random);
            float size = Mth.lerp(random.nextFloat(), 0.45f, 1) * (1.08f - textureIndex * 0.08f);
            float rotation = random.nextFloat() * TWO_PI;
            float alpha = Mth.lerp(random.nextFloat(), 0.45f, 0.95f);
            float twinkleOffset = random.nextFloat() * TWO_PI;

            stars.add(new Star(direction, size, rotation, textureIndex, alpha, twinkleOffset));
        }

        this.staticStars = stars;
        this.starSeed = seed;
    }

    private void rebuildStarBuffers(float tickTime) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float twinkleAngle = TWO_PI * tickTime / STAR_TWINKLE_PERIOD_TICKS;

        for (int textureIndex = 0; textureIndex < STAR_TEXTURES.length; textureIndex++) {
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            for (Star star : this.staticStars) {
                if (star.textureIndex != textureIndex) {
                    continue;
                }
                float colorProgress = 0.5F + 0.5F * Mth.sin(twinkleAngle + star.twinkleOffset);
                float saturation = STAR_MAX_SATURATION * colorProgress;
                float value = Mth.lerp(colorProgress, 1.0F, STAR_MIN_VALUE);
                int color = Mth.hsvToRgb(STAR_HUE, saturation, value);
                float red = (color >> 16 & 255) / 255.0F;
                float green = (color >> 8 & 255) / 255.0F;
                float blue = (color & 255) / 255.0F;

                this.addStarQuad(bufferBuilder, star.direction, STAR_DISTANCE, star.size, star.rotation, red, green, blue, star.alpha);
            }

            BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
            this.staticStarBuffers[textureIndex].bind();
            this.staticStarBuffers[textureIndex].upload(renderedBuffer);
            VertexBuffer.unbind();
        }
    }

    private void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
        this.rebuildStarBuffers(level.getGameTime() + partialTick);

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        for (int textureIndex = 0; textureIndex < STAR_TEXTURES.length; textureIndex++) {
            RenderSystem.setShaderTexture(0, STAR_TEXTURES[textureIndex]);
            this.staticStarBuffers[textureIndex].bind();
            this.staticStarBuffers[textureIndex].drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
            VertexBuffer.unbind();
        }
    }

    private void renderShootingStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
        double skyTime = level.getGameTime() + partialTick;
        long slot = (long) Math.floor(skyTime / SHOOTING_STAR_PERIOD);

        for (long currentSlot = slot - 1L; currentSlot <= slot; currentSlot++) {
            ShootingStarEvent event = this.createShootingStar(level, currentSlot);
            if (event == null) {
                continue;
            }

            double eventTime = (skyTime - event.startTick) / event.duration;
            if (eventTime < 0 || eventTime > 1) {
                continue;
            }

            this.renderShootingStar(event, (float) eventTime, poseStack, projectionMatrix);
        }
    }

    private ShootingStarEvent createShootingStar(ClientLevel level, long slot) {
        long seed = this.getSkySeed(level) ^ mixSeed(slot * 341873128712L + 132897987541L);
        RandomSource random = RandomSource.create(seed);
        if (random.nextFloat() > SHOOTING_STAR_CHANCE) {
            return null;
        }

        int duration = Mth.nextInt(random, SHOOTING_STAR_MIN_DURATION, SHOOTING_STAR_MAX_DURATION);
        long slotStart = slot * SHOOTING_STAR_PERIOD;
        long startTick = slotStart + random.nextInt(Math.max(1, SHOOTING_STAR_PERIOD - duration));
        float y = Mth.lerp(random.nextFloat(), 0.45f, 0.92f);
        float horizontal = Mth.sqrt(1 - y * y);
        float azimuth = random.nextFloat() * TWO_PI;

        Vec3 startDirection = new Vec3(Mth.cos(azimuth) * horizontal, y, Mth.sin(azimuth) * horizontal);

        Basis basis = getBasis(startDirection, 0);
        float pathAngle = random.nextFloat() * TWO_PI;

        Vec3 tangentDirection = basis.right.scale(Mth.cos(pathAngle)).add(basis.up.scale(Mth.sin(pathAngle))).normalize();

        float arcAngle = Mth.lerp(random.nextFloat(), SHOOTING_STAR_MIN_ARC_ANGLE, SHOOTING_STAR_MAX_ARC_ANGLE);
        float rotation = random.nextFloat() * TWO_PI;
        int textureIndex = chooseWeightedTexture(random);
        float size = Mth.lerp(random.nextFloat(), 1.6f, 2.2f) * (1.05f - textureIndex * 0.05f);
        float alpha = Mth.lerp(random.nextFloat(), 0.75f, 1f);
        float tailProgress = Mth.lerp(random.nextFloat(), 0.18f, 0.34f);

        return new ShootingStarEvent(startTick, duration, startDirection, tangentDirection, arcAngle, rotation, textureIndex, size, alpha, tailProgress);
    }

    private void renderShootingStar(ShootingStarEvent event, float progress, PoseStack poseStack, Matrix4f projectionMatrix) {
        float lifeScale = 1 - Math.abs(progress * 2 - 1);
        if (lifeScale <= 0) {
            return;
        }

        this.renderShootingTail(event, progress, lifeScale, poseStack, projectionMatrix);
        Vec3 direction = getGreatCircleDirection(event.startDirection, event.tangentDirection, event.arcAngle, progress);

        this.renderSingleStar(poseStack, projectionMatrix, direction, STAR_DISTANCE - 0.25f, event.size * lifeScale, event.rotation, event.textureIndex, event.alpha * lifeScale);
    }

    private void renderShootingTail(ShootingStarEvent event, float progress, float lifeScale, PoseStack poseStack, Matrix4f projectionMatrix) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        float tailWindow = Math.min(event.tailProgress, progress);

        if (tailWindow <= 0) {
            return;
        }

        int segments = 8;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            float fromProgress = progress - tailWindow * i / segments;
            float toProgress = progress - tailWindow * (i + 1) / segments;

            if (toProgress < 0) {
                toProgress = 0;
            }

            Vec3 fromDirection = getGreatCircleDirection(event.startDirection, event.tangentDirection, event.arcAngle, fromProgress);
            Vec3 toDirection = getGreatCircleDirection(event.startDirection, event.tangentDirection, event.arcAngle, toProgress);
            Vec3 fromMotion = getGreatCircleMotion(event.startDirection, event.tangentDirection, event.arcAngle, fromProgress);
            Vec3 toMotion = getGreatCircleMotion(event.startDirection, event.tangentDirection, event.arcAngle, toProgress);

            float fromFactor = 1 - (float) i / segments;
            float toFactor = 1 - (float) (i + 1) / segments;

            Vec3 fromSide = fromDirection.scale(-1).cross(fromMotion).normalize();
            Vec3 toSide = toDirection.scale(-1).cross(toMotion).normalize();

            double fromWidth = event.size * 0.12f * fromFactor;
            double toWidth = event.size * 0.12f * toFactor;

            Vec3 fromCenter = fromDirection.scale(STAR_DISTANCE - 0.1f);
            Vec3 toCenter = toDirection.scale(STAR_DISTANCE - 0.1f);
            Vec3 fromLeft = fromCenter.add(fromSide.scale(fromWidth));
            Vec3 fromRight = fromCenter.subtract(fromSide.scale(fromWidth));
            Vec3 toLeft = toCenter.add(toSide.scale(toWidth));
            Vec3 toRight = toCenter.subtract(toSide.scale(toWidth));

            float fromAlpha = event.alpha * lifeScale * 0.55f * fromFactor;
            float toAlpha = event.alpha * lifeScale * 0.55f * toFactor;

            addColorQuad(bufferBuilder, fromLeft, fromRight, toRight, toLeft, 1, 1, 1, fromAlpha, 1, 1, 1, toAlpha);
        }

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();

        this.dynamicColorBuffer.bind();
        this.dynamicColorBuffer.upload(renderedBuffer);
        this.dynamicColorBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
    }

    private void renderSingleStar(PoseStack poseStack, Matrix4f projectionMatrix, Vec3 direction, float distance, float size, float rotation, int textureIndex, float alpha) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        RenderSystem.setShaderTexture(0, STAR_TEXTURES[textureIndex]);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        this.addStarQuad(bufferBuilder, direction, distance, size, rotation, 1, 1, 1, alpha);

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();

        this.dynamicTexturedBuffer.bind();
        this.dynamicTexturedBuffer.upload(renderedBuffer);
        this.dynamicTexturedBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
    }

    private void renderBiomeRing(ClientLevel level, Camera camera, PoseStack poseStack, Matrix4f projectionMatrix) {
        int color = level.getBiome(BlockPos.containing(camera.getPosition())).value().getSkyColor();

        if (color == 0) {
            color = level.getBiome(BlockPos.containing(camera.getPosition())).value().getFogColor();
        }

        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        float radius = 120f;
        float lowerY = -18f;
        float horizonY = 0f;
        float upperY = 18f;
        float horizonAlpha = 0.5f;
        int segments = 64;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            float fromAngle = TWO_PI * i / segments;
            float toAngle = TWO_PI * (i + 1) / segments;

            Vec3 lowerFrom = new Vec3(Mth.cos(fromAngle) * radius, lowerY, Mth.sin(fromAngle) * radius);
            Vec3 lowerTo = new Vec3(Mth.cos(toAngle) * radius, lowerY, Mth.sin(toAngle) * radius);

            Vec3 horizonFrom = new Vec3(Mth.cos(fromAngle) * radius, horizonY, Mth.sin(fromAngle) * radius);
            Vec3 horizonTo = new Vec3(Mth.cos(toAngle) * radius, horizonY, Mth.sin(toAngle) * radius);

            Vec3 upperFrom = new Vec3(Mth.cos(fromAngle) * radius, upperY, Mth.sin(fromAngle) * radius);
            Vec3 upperTo = new Vec3(Mth.cos(toAngle) * radius, upperY, Mth.sin(toAngle) * radius);

            addColorQuad(bufferBuilder, lowerFrom, lowerTo, horizonTo, horizonFrom, 0, 0, 0, 0, red, green, blue, horizonAlpha);
            addColorQuad(bufferBuilder, horizonFrom, horizonTo, upperTo, upperFrom, red, green, blue, horizonAlpha, 0, 0, 0, 0);
        }

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();

        this.dynamicColorBuffer.bind();
        this.dynamicColorBuffer.upload(renderedBuffer);
        this.dynamicColorBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
    }

    private void addStarQuad(BufferBuilder bufferBuilder, Vec3 direction, float distance, float size, float rotation, float red, float green, float blue, float alpha) {
        Basis basis = getBasis(direction, rotation);
        Vec3 center = direction.scale(distance);
        Vec3 horizontal = basis.right.scale(size);
        Vec3 vertical = basis.up.scale(size);
        Vec3 topLeft = center.subtract(horizontal).add(vertical);
        Vec3 bottomLeft = center.subtract(horizontal).subtract(vertical);
        Vec3 bottomRight = center.add(horizontal).subtract(vertical);
        Vec3 topRight = center.add(horizontal).add(vertical);

        bufferBuilder.vertex((float) topLeft.x, (float) topLeft.y, (float) topLeft.z).color(red, green, blue, alpha).uv(0, 0).endVertex();
        bufferBuilder.vertex((float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).color(red, green, blue, alpha).uv(0, 1).endVertex();
        bufferBuilder.vertex((float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).color(red, green, blue, alpha).uv(1, 1).endVertex();
        bufferBuilder.vertex((float) topRight.x, (float) topRight.y, (float) topRight.z).color(red, green, blue, alpha).uv(1, 0).endVertex();
    }

    private static void addColorQuad(BufferBuilder bufferBuilder, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, float firstRed, float firstGreen, float firstBlue, float firstAlpha, float secondRed, float secondGreen, float secondBlue, float secondAlpha) {
        bufferBuilder.vertex((float) first.x, (float) first.y, (float) first.z).color(firstRed, firstGreen, firstBlue, firstAlpha).endVertex();
        bufferBuilder.vertex((float) second.x, (float) second.y, (float) second.z).color(firstRed, firstGreen, firstBlue, firstAlpha).endVertex();
        bufferBuilder.vertex((float) third.x, (float) third.y, (float) third.z).color(secondRed, secondGreen, secondBlue, secondAlpha).endVertex();
        bufferBuilder.vertex((float) fourth.x, (float) fourth.y, (float) fourth.z).color(secondRed, secondGreen, secondBlue, secondAlpha).endVertex();
    }

    private static Basis getBasis(Vec3 direction, float rotation) {
        Vec3 normalizedDirection = direction.normalize();
        Vec3 reference = Math.abs(normalizedDirection.y) > 0.98 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = reference.cross(normalizedDirection).normalize();
        Vec3 up = normalizedDirection.cross(right).normalize();

        if (rotation != 0) {
            double cos = Mth.cos(rotation);
            double sin = Mth.sin(rotation);

            Vec3 rotatedRight = right.scale(cos).add(up.scale(sin));
            Vec3 rotatedUp = up.scale(cos).subtract(right.scale(sin));

            return new Basis(rotatedRight, rotatedUp);
        }

        return new Basis(right, up);
    }

    private static Vec3 getGreatCircleDirection(Vec3 startDirection, Vec3 tangentDirection, float arcAngle, float progress) {
        float angle = arcAngle * progress;

        return startDirection.scale(Mth.cos(angle)).add(tangentDirection.scale(Mth.sin(angle))).normalize();
    }

    private static Vec3 getGreatCircleMotion(Vec3 startDirection, Vec3 tangentDirection, float arcAngle, float progress) {
        float angle = arcAngle * progress;

        return tangentDirection.scale(Mth.cos(angle)).subtract(startDirection.scale(Mth.sin(angle))).normalize();
    }

    private static int chooseWeightedTexture(RandomSource random) {
        int totalWeight = 0;

        for (int weight : STAR_TYPE_WEIGHTS) {
            totalWeight += weight;
        }

        int value = random.nextInt(totalWeight);
        for (int i = 0; i < STAR_TYPE_WEIGHTS.length; i++) {
            value -= STAR_TYPE_WEIGHTS[i];

            if (value < 0) {
                return i;
            }
        }

        return 0;
    }

    private long getSkySeed(ClientLevel level) {
        return mixSeed(level.dimension().location().toString().hashCode());
    }

    public static long mixSeed(long seed) {
        long mixed = seed;
        
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;

        return mixed;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float v) {
        return Vec3.ZERO;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
        return false;
    }

    private record Basis(Vec3 right, Vec3 up) {
    }

    private record Star(Vec3 direction, float size, float rotation, int textureIndex, float alpha, float twinkleOffset) {
    }

    private record ShootingStarEvent(long startTick, int duration, Vec3 startDirection, Vec3 tangentDirection, float arcAngle, float rotation, int textureIndex, float size, float alpha, float tailProgress) {
    }
}