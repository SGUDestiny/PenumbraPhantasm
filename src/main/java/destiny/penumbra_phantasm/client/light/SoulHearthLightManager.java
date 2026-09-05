package destiny.penumbra_phantasm.client.light;

import destiny.penumbra_phantasm.server.item.SoulHearthItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SoulHearthLightManager {
    public static final int LIGHT_RADIUS = 15;
    public static final int LIGHT_LEVEL = 15;

    private static final Map<BlockPos, Set<Player>> POS_TO_SOURCES = new HashMap<>();
    private static final Map<Player, SourceState> SOURCE_STATES = new HashMap<>();
    private static final Map<Player, Long> LAST_UPDATE_TICK = new HashMap<>();

    public static int UPDATE_INTERVAL_TICKS = 5;
    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();

    private SoulHearthLightManager() {}

    public static void register(Player player) {
        if (!player.level().isClientSide) return;
        update(player);
    }

    public static void unregister(Player player) {
        if (!player.level().isClientSide) return;

        SourceState previousState = SOURCE_STATES.remove(player);
        LAST_UPDATE_TICK.remove(player);

        if (previousState != null) {
            BlockPos pos = previousState.pos();

            removeFromPosMap(pos, player);
            checkLightRemoval(player.level(), pos);
        }
    }

    public static void update(Player player) {
        if (!player.level().isClientSide) return;

        long currentTick = player.level().getGameTime();
        int currentLight = getPlayerLightLevel(player);
        boolean turningOff = currentLight <= 0;

        if (!turningOff) {
            Long lastTick = LAST_UPDATE_TICK.get(player);
            if (lastTick != null && currentTick - lastTick < UPDATE_INTERVAL_TICKS) {
                return;
            }
        }
        LAST_UPDATE_TICK.put(player, currentTick);

        BlockPos currentPos = player.blockPosition().immutable();
        SourceState previousState = SOURCE_STATES.get(player);

        if (turningOff) {
            if (previousState != null) {
                SOURCE_STATES.remove(player);
                LAST_UPDATE_TICK.remove(player);

                removeFromPosMap(previousState.pos(), player);
                checkLightRemoval(player.level(), previousState.pos());
            }
            return;
        }

        SourceState currentState = new SourceState(currentPos, currentLight);

        if (previousState == null) {
            SOURCE_STATES.put(player, currentState);

            addToPosMap(currentPos, player);
            checkLightSource(player.level(), currentPos);

            return;
        }

        if (!previousState.pos().equals(currentPos)) {
            SOURCE_STATES.put(player, currentState);

            removeFromPosMap(previousState.pos(), player);
            addToPosMap(currentPos, player);
            checkLightRemoval(player.level(), previousState.pos());
            checkLightSource(player.level(), currentPos);

            return;
        }

        if (previousState.lightLevel() != currentLight) {
            SOURCE_STATES.put(player, currentState);

            checkLightSource(player.level(), currentPos);

            return;
        }

        checkLightSource(player.level(), currentPos);
    }

    public static int getBlockLightContribution(Level level, BlockPos pos) {
        if (!level.isClientSide) return 0;

        Set<Player> sources = POS_TO_SOURCES.get(pos);

        if (sources == null) return 0;

        int maxLight = 0;
        for (Player source : sources) {
            if (!source.isAlive() || source.level() != level || source.isSpectator()) continue;
            if (!SOURCE_STATES.containsKey(source)) continue;

            maxLight = Math.max(maxLight, getPlayerLightLevel(source));
        }

        return maxLight;
    }

    public static void scheduleRecheckSavedBlockLight(Level level, LevelChunk chunk) {
        if (!level.isClientSide) return;

        long chunkKey = chunk.getPos().toLong();
        Minecraft.getInstance().execute(() -> recheckSavedBlockLight(level, chunkKey));
    }

    public static void clearAll() {
        POS_TO_SOURCES.clear();
        SOURCE_STATES.clear();
        LAST_UPDATE_TICK.clear();
    }

    public static void purgeMissingPlayers(Level level) {
        if (!level.isClientSide) return;

        List<Player> toRemove = new ArrayList<>();
        for (Player player : SOURCE_STATES.keySet()) {
            if (!player.isAlive() || player.level() != level || !level.players().contains(player) || player.isSpectator()) {
                toRemove.add(player);
            }
        }

        for (Player player : toRemove) {
            unregister(player);
        }
    }

    private static int getPlayerLightLevel(Player player) {
        if (!player.isAlive() || player.isRemoved() || !SoulHearthItem.isHoldingOwn(player) || player.isSpectator()) {
            return 0;
        }

        return LIGHT_LEVEL;
    }

    private static void recheckSavedBlockLight(Level level, long chunkKey) {
        if (!level.isClientSide) return;

        int chunkX = ChunkPos.getX(chunkKey);
        int chunkZ = ChunkPos.getZ(chunkKey);
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);

        if (chunk == null || level != chunk.getLevel()) return;

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        boolean anySource = false;
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        for (Map.Entry<Player, SourceState> entry : SOURCE_STATES.entrySet()) {
            Player source = entry.getKey();

            if (!source.isAlive() || source.level() != level || source.isSpectator()) continue;

            BlockPos pos = entry.getValue().pos();
            if (pos.getX() < minX - LIGHT_RADIUS || pos.getX() > maxX + LIGHT_RADIUS || pos.getZ() < minZ - LIGHT_RADIUS || pos.getZ() > maxZ + LIGHT_RADIUS) {
                continue;
            }

            anySource = true;
            minY = Math.min(minY, pos.getY() - LIGHT_RADIUS);
            maxY = Math.max(maxY, pos.getY() + LIGHT_RADIUS);

            checkLightSource(level, pos);
        }

        if (!anySource) return;

        minY = Math.max(level.getMinBuildHeight(), minY);
        maxY = Math.min(level.getMaxBuildHeight() - 1, maxY);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(minX + x, y, minZ + z);

                    if (level.getBrightness(LightLayer.BLOCK, pos) <= 0) continue;
                    if (level.getBlockState(pos).getLightEmission(level, pos) > 0) continue;

                    level.getLightEngine().checkBlock(pos);
                }
            }
        }
    }

    private static void addToPosMap(BlockPos pos, Player player) {
        POS_TO_SOURCES.computeIfAbsent(pos.immutable(), k -> new HashSet<>()).add(player);
    }

    private static void removeFromPosMap(BlockPos pos, Player player) {
        Set<Player> set = POS_TO_SOURCES.get(pos);
        if (set != null) {
            set.remove(player);

            if (set.isEmpty()) POS_TO_SOURCES.remove(pos);
        }
    }

    private static void checkLightSource(Level level, BlockPos pos) {
        level.getLightEngine().checkBlock(pos);

        for (Direction direction : Direction.values()) {
            level.getLightEngine().checkBlock(MUTABLE_POS.set(pos).move(direction));
        }
    }

    private static void checkLightRemoval(Level level, BlockPos pos) {
        int radiusSqr = LIGHT_RADIUS * LIGHT_RADIUS;
        for (int x = -LIGHT_RADIUS; x <= LIGHT_RADIUS; x++) {
            for (int y = -LIGHT_RADIUS; y <= LIGHT_RADIUS; y++) {
                for (int z = -LIGHT_RADIUS; z <= LIGHT_RADIUS; z++) {
                    if (x * x + y * y + z * z > radiusSqr) continue;

                    MUTABLE_POS.set(pos).move(x, y, z);
                    level.getLightEngine().checkBlock(MUTABLE_POS);
                }
            }
        }
    }

    private record SourceState(BlockPos pos, int lightLevel) {}
}