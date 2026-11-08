package destiny.penumbra_phantasm.server.entity;

import destiny.penumbra_phantasm.server.capability.AnchorOfLightCapability;
import destiny.penumbra_phantasm.server.registry.EntityRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CustomEntitySpawning {
    public static final int DEVICE_FRIEND_SPAWN_INTERVAL = 60 * 5;
    public static final float DEVICE_FRIEND_SPAWN_CHANCE = 0.3f;
    public static final int DEVICE_FRIEND_SPAWN_MIN_RADIUS = 64;
    public static final int DEVICE_FRIEND_SPAWN_MAX_RADIUS = 128;

    public static void trySpawnEntities(ServerLevel serverLevel) {
        for (ServerPlayer serverPlayer : serverLevel.players()) {
            if (DarkWorldUtil.isDepths(serverLevel)) {
                spawnDeviceFriend(serverLevel, serverPlayer);
            }
        }
    }

    public static void spawnDeviceFriend(ServerLevel serverLevel, ServerPlayer serverPlayer) {
        if (serverLevel.getGameTime() % DEVICE_FRIEND_SPAWN_INTERVAL != 0) return;
        if (serverLevel.getRandom().nextFloat() > DEVICE_FRIEND_SPAWN_CHANCE) return;
        if (AnchorOfLightCapability.isProtectedByAnchorStatic(serverLevel, serverPlayer.blockPosition())) return;

        BlockPos spawnPos = getSpawnPosition(serverLevel, serverPlayer);

        if (spawnPos == null) return;

        DEVICE_FRIEND friend = new DEVICE_FRIEND(EntityRegistry.DEVICE_FRIEND.get(), serverLevel);
        friend.setPos(spawnPos.getCenter());

        serverLevel.addFreshEntity(friend);
    }

    public static BlockPos getSpawnPosition(ServerLevel serverLevel, ServerPlayer serverPlayer) {
        int attempts = 0;

        while (attempts < 16) {
            BlockPos potentialPos = getRandomPositionInBoundRadius(serverLevel, serverPlayer.blockPosition(), DEVICE_FRIEND_SPAWN_MIN_RADIUS,
                    DEVICE_FRIEND_SPAWN_MAX_RADIUS);

            if (!serverLevel.isLoaded(potentialPos)) {
                attempts++;
                continue;
            }

            AABB box = new AABB(potentialPos).inflate(128);

            boolean shouldContinue = false;
            for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, box)) {
                if (isInPlayerView(player, potentialPos, 120f)) {
                    shouldContinue = true;
                    break;
                }
                if (!isUnobstructedByBlocks(serverLevel, player, potentialPos)) {
                    shouldContinue = true;
                    break;
                }
            }

            if (shouldContinue) {
                attempts++;
                continue;
            }

            return potentialPos;
        }

        return null;
    }

    public static BlockPos getRandomPositionInBoundRadius(ServerLevel serverLevel, BlockPos playerPos, int minRadius, int maxRadius) {
        double angle = serverLevel.getRandom().nextDouble() * 2 * Math.PI;
        double distance = minRadius + serverLevel.getRandom().nextDouble() * (maxRadius - minRadius);

        double x = playerPos.getX() + 0.5 + Math.cos(angle) * distance;
        double z = playerPos.getZ() + 0.5 + Math.sin(angle) * distance;
        double y = ModUtil.worldSurfaceYAtXZ(serverLevel, x, z);

        return new BlockPos((int) x, (int) y, (int) z);
    }

    public static boolean isUnobstructedByBlocks(ServerLevel serverLevel, ServerPlayer serverPlayer, BlockPos potentialPos) {
        ClipContext context = new ClipContext(serverPlayer.position(), potentialPos.getCenter(), ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, serverPlayer);
        BlockHitResult hitResult = serverLevel.clip(context);

        return hitResult.getType() == HitResult.Type.MISS;
    }

    public static boolean isInPlayerView(ServerPlayer serverPlayer, BlockPos testPos, float fovDegrees) {
        Vec3 viewVec = serverPlayer.getViewVector(1f).normalize();
        Vec3 toTestPos = serverPlayer.getEyePosition().vectorTo(testPos.getCenter()).normalize();

        double dot = viewVec.dot(toTestPos);
        double cosHalfFov = Math.cos(Math.toRadians(fovDegrees * 0.5f));

        return dot >= cosHalfFov;
    }
}
