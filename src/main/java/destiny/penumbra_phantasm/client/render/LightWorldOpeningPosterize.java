package destiny.penumbra_phantasm.client.render;

import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class LightWorldOpeningPosterize {
    private LightWorldOpeningPosterize() {
    }

    private static float smoothStep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    public static float strength(float tick) {
        if (tick < 0f || tick >= FountainRenderUtil.OPENING_POSTERIZE_TICK_END) {
            return 0f;
        }

        float rampEnd = FountainRenderUtil.POSTERIZE_STRENGTH_RAMP_TICKS;
        if (tick < rampEnd) {
            return smoothStep(0f, rampEnd, tick);
        }

        if (tick < FountainRenderUtil.OPENING_SHADOW_FADE_START) {
            return 1f;
        }

        float fadeEnd = FountainRenderUtil.OPENING_SHADOW_FADE_START + FountainRenderUtil.OPENING_SHADOW_FADE_DURATION + FountainRenderUtil.OPENING_POSTERIZE_SHADOW_FADE_TAIL;
        if (tick <= fadeEnd) {
            return 1f - smoothStep(FountainRenderUtil.OPENING_SHADOW_FADE_START, fadeEnd, tick
            );
        }

        return 0f;
    }

    public static float whiteLevel(float tick) {
        if (tick < 0f || tick >= FountainRenderUtil.OPENING_POSTERIZE_TICK_END) {
            return 0f;
        }

        float rampEnd = FountainRenderUtil.POSTERIZE_STRENGTH_RAMP_TICKS;
        if (tick < rampEnd) {
            return 1f;
        }

        if (tick < FountainRenderUtil.OPENING_SHADOW_FADE_START) {
            return 0.95f + 0.05f * Mth.sin(tick * FountainRenderUtil.OPENING_PULSE_FREQ);
        }

        float fadeEnd = FountainRenderUtil.OPENING_SHADOW_FADE_START + FountainRenderUtil.OPENING_SHADOW_FADE_DURATION + FountainRenderUtil.OPENING_POSTERIZE_SHADOW_FADE_TAIL;
        if (tick <= fadeEnd) {
            return 1f - smoothStep(FountainRenderUtil.OPENING_SHADOW_FADE_START, fadeEnd, tick
            );
        }

        return 0f;
    }

    public static float distanceInBlocks(Vec3 cameraPos, BlockPos fountainPos) {
        double camX = cameraPos.x;
        double camY = cameraPos.y;
        double camZ = cameraPos.z;
        double fx = fountainPos.getX();
        double fy = fountainPos.getY();
        double fz = fountainPos.getZ();

        Vec2 flatCam = new Vec2((float) camX, (float) camZ);
        Vec2 flatFountain = new Vec2((float) fx, (float) fz);

        float distanceSquared = flatCam.distanceToSqr(flatFountain);
        if (camY < fy) {
            distanceSquared = (float) cameraPos.distanceToSqr(Vec3.atLowerCornerOf(fountainPos));
        }

        return Mth.sqrt(distanceSquared);
    }

    public static float distanceFade(float distanceInBlocks) {
        float outer = FountainRenderUtil.POSTERIZE_DISTANCE_OUTER + FountainRenderUtil.POSTERIZE_DISTANCE_OUTER_SOFT;
        if (distanceInBlocks >= outer) {
            return 0f;
        }

        if (distanceInBlocks <= FountainRenderUtil.POSTERIZE_DISTANCE_RAMP_START) {
            return 1f;
        }

        return 1f - smoothStep(
                FountainRenderUtil.POSTERIZE_DISTANCE_RAMP_START,
                outer,
                distanceInBlocks
        );
    }

    public static Optional<DarkFountain> findClosestOpeningFountain(ClientLevel level, Vec3 cameraPos, float partialTick) {
        if (DarkWorldUtil.isDarkWorld(level)) {
            return Optional.empty();
        }

        DarkFountain closestFountain = null;
        double bestDistSq = Double.POSITIVE_INFINITY;
        Optional<DarkFountainCapability> capOpt = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).resolve();

        if (capOpt.isEmpty()) {
            return Optional.empty();
        }

        DarkFountainCapability cap = capOpt.get();
        for (DarkFountain fountain : cap.darkFountains.values()) {
            float t = fountain.getOpeningTick(partialTick);
            if (t < 0f || t >= FountainRenderUtil.OPENING_POSTERIZE_TICK_END) {
                continue;
            }

            float d = distanceInBlocks(cameraPos, fountain.getFountainPos());
            double dsq = d * d;

            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                closestFountain = fountain;
            }
        }

        return Optional.ofNullable(closestFountain);
    }
}