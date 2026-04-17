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

    private static final int[] STAR_WEIGHTS = new int[]{48, 24, 12, 6, 3};
    private static final float SKY_DISC_HEIGHT = 16.0F;
    private static final float STAR_DISTANCE = 96.0F;
    private static final int STATIC_STAR_COUNT = 250;
    private static final int SHOOTING_STAR_PERIOD = 90;
    private static final float SHOOTING_STAR_CHANCE = 0.75F;
    private static final int SHOOTING_STAR_MIN_DURATION = 24;
    private static final int SHOOTING_STAR_MAX_DURATION = 40;
    private static final float TWO_PI = (float) (Math.PI * 2.0D);
    private static final float STATIC_STAR_HUE = 220.0F / 360.0F;
    private static final float STATIC_STAR_MAX_SATURATION = 0.75F;
    private static final float STATIC_STAR_MIN_VALUE = 0.12F;
    private static final float STATIC_STAR_TWINKLE_PERIOD_TICKS = 100.0F;
    private static final float SHOOTING_STAR_MIN_ARC_ANGLE = 0.84F;
    private static final float SHOOTING_STAR_MAX_ARC_ANGLE = 1.56F;

    private final VertexBuffer lowerSkyBuffer;
    private final VertexBuffer[] staticStarBuffers = new VertexBuffer[STAR_TEXTURES.length];
    private final VertexBuffer dynamicTexturedBuffer;
    private final VertexBuffer dynamicColorBuffer;

    private long starSeed = Long.MIN_VALUE;

    private List<StaticStar> staticStars = List.of();

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
        return level.dimension().location().getPath().contains("card_kingdom");
    }

    private static VertexBuffer createSkyBuffer(float scale) {
        VertexBuffer skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        BufferBuilder.RenderedBuffer renderedBuffer = DarkWorldDimensionEffects.buildSkyDisc(bufferBuilder, scale);
        skyBuffer.bind();
        skyBuffer.upload(renderedBuffer);
        VertexBuffer.unbind();

        return skyBuffer;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);

        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        this.lowerSkyBuffer.bind();
        this.lowerSkyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);

        return true;
    }

    public void renderOverlay(ClientLevel level, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix) {
        this.ensureStaticStars(level);
        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderFogStart(STAR_DISTANCE * 4.0F);
        RenderSystem.setShaderFogEnd(STAR_DISTANCE * 4.5F);

        this.renderStaticStars(level, partialTick, poseStack, projectionMatrix);
        this.renderShootingStars(level, partialTick, poseStack, projectionMatrix);
        this.renderHorizonRing(level, camera, poseStack, projectionMatrix);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private void ensureStaticStars(ClientLevel level) {
        long seed = this.getSkySeed(level);
        if (seed == this.starSeed) {
            return;
        }

        RandomSource random = RandomSource.create(seed);
        List<StaticStar> stars = new ArrayList<>(STATIC_STAR_COUNT);

        for (int i = 0; i < STATIC_STAR_COUNT; i++) {
            float y = Mth.lerp(random.nextFloat(), 0.1F, 0.96F);
            float horizontal = Mth.sqrt(1.0F - y * y);
            float angle = random.nextFloat() * TWO_PI;

            Vec3 direction = new Vec3(Mth.cos(angle) * horizontal, y, Mth.sin(angle) * horizontal);

            int textureIndex = chooseWeightedTexture(random);
            float size = Mth.lerp(random.nextFloat(), 0.45F, 1.0F) * (1.08F - textureIndex * 0.08F);
            float rotation = random.nextFloat() * TWO_PI;
            float alpha = Mth.lerp(random.nextFloat(), 0.45F, 0.95F);
            float twinkleOffset = random.nextFloat() * TWO_PI;

            stars.add(new StaticStar(direction, size, rotation, textureIndex, alpha, twinkleOffset));
        }

        this.staticStars = stars;
        this.starSeed = seed;
    }

    private void rebuildStaticStarBuffers(float tickTime) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float twinkleAngle = TWO_PI * tickTime / STATIC_STAR_TWINKLE_PERIOD_TICKS;

        for (int textureIndex = 0; textureIndex < STAR_TEXTURES.length; textureIndex++) {
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            for (StaticStar star : this.staticStars) {
                if (star.textureIndex != textureIndex) {
                    continue;
                }
                float colorProgress = 0.5F + 0.5F * Mth.sin(twinkleAngle + star.twinkleOffset);
                float saturation = STATIC_STAR_MAX_SATURATION * colorProgress;
                float value = Mth.lerp(colorProgress, 1.0F, STATIC_STAR_MIN_VALUE);
                int color = Mth.hsvToRgb(STATIC_STAR_HUE, saturation, value);
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

    private void renderStaticStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
        this.rebuildStaticStarBuffers(level.getGameTime() + partialTick);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
            if (eventTime < 0.0D || eventTime > 1.0D) {
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
        float y = Mth.lerp(random.nextFloat(), 0.45F, 0.92F);
        float horizontal = Mth.sqrt(1.0F - y * y);
        float azimuth = random.nextFloat() * TWO_PI;

        Vec3 startDirection = new Vec3(Mth.cos(azimuth) * horizontal, y, Mth.sin(azimuth) * horizontal);

        Basis basis = getBasis(startDirection, 0.0F);
        float pathAngle = random.nextFloat() * TWO_PI;

        Vec3 tangentDirection = basis.right.scale(Mth.cos(pathAngle)).add(basis.up.scale(Mth.sin(pathAngle))).normalize();

        float arcAngle = Mth.lerp(random.nextFloat(), SHOOTING_STAR_MIN_ARC_ANGLE, SHOOTING_STAR_MAX_ARC_ANGLE);
        float rotation = random.nextFloat() * TWO_PI;
        int textureIndex = chooseWeightedTexture(random);
        float size = Mth.lerp(random.nextFloat(), 1.6F, 2.2F) * (1.05F - textureIndex * 0.05F);
        float alpha = Mth.lerp(random.nextFloat(), 0.75F, 1.0F);
        float tailProgress = Mth.lerp(random.nextFloat(), 0.18F, 0.34F);

        return new ShootingStarEvent(startTick, duration, startDirection, tangentDirection, arcAngle, rotation, textureIndex, size, alpha, tailProgress);
    }

    private void renderShootingStar(ShootingStarEvent event, float progress, PoseStack poseStack, Matrix4f projectionMatrix) {
        float lifeScale = 1.0F - Math.abs(progress * 2.0F - 1.0F);
        if (lifeScale <= 0.0F) {
            return;
        }

        this.renderShootingTail(event, progress, lifeScale, poseStack, projectionMatrix);
        Vec3 direction = getGreatCircleDirection(event.startDirection, event.tangentDirection, event.arcAngle, progress);

        this.renderSingleStar(poseStack, projectionMatrix, direction, STAR_DISTANCE - 0.25F, event.size * lifeScale, event.rotation, event.textureIndex, event.alpha * lifeScale);
    }

    private void renderShootingTail(ShootingStarEvent event, float progress, float lifeScale, PoseStack poseStack, Matrix4f projectionMatrix) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        float tailWindow = Math.min(event.tailProgress, progress);

        if (tailWindow <= 0.0F) {
            return;
        }

        int segments = 8;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            float fromProgress = progress - tailWindow * i / segments;
            float toProgress = progress - tailWindow * (i + 1) / segments;
            if (toProgress < 0.0F) {
                toProgress = 0.0F;
            }

            Vec3 fromDirection = getGreatCircleDirection(event.startDirection, event.tangentDirection, event.arcAngle, fromProgress);
            Vec3 toDirection = getGreatCircleDirection(event.startDirection, event.tangentDirection, event.arcAngle, toProgress);
            Vec3 fromMotion = getGreatCircleMotion(event.startDirection, event.tangentDirection, event.arcAngle, fromProgress);
            Vec3 toMotion = getGreatCircleMotion(event.startDirection, event.tangentDirection, event.arcAngle, toProgress);

            float fromFactor = 1.0F - (float) i / segments;
            float toFactor = 1.0F - (float) (i + 1) / segments;

            Vec3 fromSide = fromDirection.scale(-1.0D).cross(fromMotion).normalize();
            Vec3 toSide = toDirection.scale(-1.0D).cross(toMotion).normalize();

            double fromWidth = event.size * 0.12F * fromFactor;
            double toWidth = event.size * 0.12F * toFactor;

            Vec3 fromCenter = fromDirection.scale(STAR_DISTANCE - 0.1F);
            Vec3 toCenter = toDirection.scale(STAR_DISTANCE - 0.1F);
            Vec3 fromLeft = fromCenter.add(fromSide.scale(fromWidth));
            Vec3 fromRight = fromCenter.subtract(fromSide.scale(fromWidth));
            Vec3 toLeft = toCenter.add(toSide.scale(toWidth));
            Vec3 toRight = toCenter.subtract(toSide.scale(toWidth));

            float fromAlpha = event.alpha * lifeScale * 0.55F * fromFactor;
            float toAlpha = event.alpha * lifeScale * 0.55F * toFactor;

            addColorQuad(bufferBuilder, fromLeft, fromRight, toRight, toLeft, 1.0F, 1.0F, 1.0F, fromAlpha, 1.0F, 1.0F, 1.0F, toAlpha);
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
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        this.addStarQuad(bufferBuilder, direction, distance, size, rotation, 1.0F, 1.0F, 1.0F, alpha);

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();

        this.dynamicTexturedBuffer.bind();
        this.dynamicTexturedBuffer.upload(renderedBuffer);
        this.dynamicTexturedBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
    }

    private void renderHorizonRing(ClientLevel level, Camera camera, PoseStack poseStack, Matrix4f projectionMatrix) {
        int color = level.getBiome(BlockPos.containing(camera.getPosition())).value().getSkyColor();
        if (color == 0) {
            color = level.getBiome(BlockPos.containing(camera.getPosition())).value().getFogColor();
        }

        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        float radius = 120.0F;
        float lowerY = -18.0F;
        float horizonY = 0.0F;
        float upperY = 18.0F;
        float horizonAlpha = 0.5F;
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

            addColorQuad(bufferBuilder, lowerFrom, lowerTo, horizonTo, horizonFrom, 0.0F, 0.0F, 0.0F, 0.0F, red, green, blue, horizonAlpha);
            addColorQuad(bufferBuilder, horizonFrom, horizonTo, upperTo, upperFrom, red, green, blue, horizonAlpha, 0.0F, 0.0F, 0.0F, 0.0F);
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

        bufferBuilder.vertex((float) topLeft.x, (float) topLeft.y, (float) topLeft.z).color(red, green, blue, alpha).uv(0.0F, 0.0F).endVertex();
        bufferBuilder.vertex((float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).color(red, green, blue, alpha).uv(0.0F, 1.0F).endVertex();
        bufferBuilder.vertex((float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).color(red, green, blue, alpha).uv(1.0F, 1.0F).endVertex();
        bufferBuilder.vertex((float) topRight.x, (float) topRight.y, (float) topRight.z).color(red, green, blue, alpha).uv(1.0F, 0.0F).endVertex();
    }

    private static void addColorQuad(BufferBuilder bufferBuilder, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, float firstRed, float firstGreen, float firstBlue, float firstAlpha, float secondRed, float secondGreen, float secondBlue, float secondAlpha) {
        bufferBuilder.vertex((float) first.x, (float) first.y, (float) first.z).color(firstRed, firstGreen, firstBlue, firstAlpha).endVertex();
        bufferBuilder.vertex((float) second.x, (float) second.y, (float) second.z).color(firstRed, firstGreen, firstBlue, firstAlpha).endVertex();
        bufferBuilder.vertex((float) third.x, (float) third.y, (float) third.z).color(secondRed, secondGreen, secondBlue, secondAlpha).endVertex();
        bufferBuilder.vertex((float) fourth.x, (float) fourth.y, (float) fourth.z).color(secondRed, secondGreen, secondBlue, secondAlpha).endVertex();
    }

    private static Basis getBasis(Vec3 direction, float rotation) {
        Vec3 normalizedDirection = direction.normalize();
        Vec3 reference = Math.abs(normalizedDirection.y) > 0.98D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = reference.cross(normalizedDirection).normalize();
        Vec3 up = normalizedDirection.cross(right).normalize();

        if (rotation != 0.0F) {
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
        for (int weight : STAR_WEIGHTS) {
            totalWeight += weight;
        }

        int value = random.nextInt(totalWeight);
        for (int i = 0; i < STAR_WEIGHTS.length; i++) {
            value -= STAR_WEIGHTS[i];
            if (value < 0) {
                return i;
            }
        }

        return 0;
    }

    private long getSkySeed(ClientLevel level) {
        return mixSeed(level.dimension().location().toString().hashCode());
    }

    private static long mixSeed(long seed) {
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

    private record StaticStar(Vec3 direction, float size, float rotation, int textureIndex, float alpha, float twinkleOffset) {
    }

    private record ShootingStarEvent(long startTick, int duration, Vec3 startDirection, Vec3 tangentDirection, float arcAngle, float rotation, int textureIndex, float size, float alpha, float tailProgress) {
    }
}
