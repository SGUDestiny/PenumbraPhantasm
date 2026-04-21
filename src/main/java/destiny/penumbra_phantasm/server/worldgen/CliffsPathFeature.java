package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.CliffrockSlideBlock;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.core.registries.Registries;

public class CliffsPathFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceKey<Biome> CLIFFS = ResourceKey.create(Registries.BIOME, new ResourceLocation(PenumbraPhantasm.MODID, "cliffs"));
    private static final int PATH_HALF_WIDTH = 1;
    private static final int PATH_CLEARANCE = 3;
    private static final int PATH_SPACING = 72;
    /** Include routes whose axis line is just outside the chunk so intersections still stamp. */
    private static final int ROUTE_CHUNK_MARGIN = PATH_HALF_WIDTH + 2;
    private static final int ALTITUDE_STEP = 5;
    private static final float ALTITUDE_NOISE_SCALE = 0.0125F;
    private static final int MIN_PATH_ABOVE_VOID = 6;
    private static final int MAX_PATH_EXTRA_HEIGHT = 22;

    public CliffsPathFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        int minX = chunkPos.getMinBlockX();
        int maxX = chunkPos.getMaxBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int maxZ = chunkPos.getMaxBlockZ();
        int minY = level.getMinBuildHeight();
        int voidHeight = level.getSeaLevel();
        long seed = level.getSeed();

        if (!chunkHasCliffs(level, minX, maxX, minZ, maxZ, voidHeight)) {
            return false;
        }

        stampXRoutes(level, seed, minX, maxX, minZ, maxZ, minY, voidHeight);
        stampZRoutes(level, seed, minX, maxX, minZ, maxZ, minY, voidHeight);
        return true;
    }

    private static boolean chunkHasCliffs(WorldGenLevel level, int minX, int maxX, int minZ, int maxZ, int sampleY) {
        for (int x = minX; x <= maxX; x += 4) {
            for (int z = minZ; z <= maxZ; z += 4) {
                if (level.getBiome(new BlockPos(x, sampleY + 8, z)).is(CLIFFS)) {
                    return true;
                }
            }
        }
        return level.getBiome(new BlockPos(minX + 8, sampleY + 8, minZ + 8)).is(CLIFFS);
    }

    private static void stampXRoutes(WorldGenLevel level, long seed, int minX, int maxX, int minZ, int maxZ, int minY, int voidHeight) {
        int minRoute = Mth.floor((minZ - ROUTE_CHUNK_MARGIN - PATH_SPACING) / (double) PATH_SPACING);
        int maxRoute = Mth.floor((maxZ + ROUTE_CHUNK_MARGIN + PATH_SPACING) / (double) PATH_SPACING);
        for (int route = minRoute; route <= maxRoute; route++) {
            long routeSeed = mix(seed ^ (route * 341873128712L) ^ 0x58bf67a7d64f1c1dL);
            int pathBaseHeight = basePathHeight(routeSeed, voidHeight);
            int previousCenter = Integer.MIN_VALUE;
            int previousY = Integer.MIN_VALUE;
            for (int x = minX; x <= maxX; x++) {
                int centerZ = fixedRouteOffset(routeSeed, route);
                if (centerZ < minZ - PATH_HALF_WIDTH - 2 || centerZ > maxZ + PATH_HALF_WIDTH + 2) {
                    previousCenter = centerZ;
                    previousY = sampleRouteHeight(routeSeed, x, pathBaseHeight, voidHeight);
                    continue;
                }

                int currentY = sampleRouteHeight(routeSeed, x, pathBaseHeight, voidHeight);
                if (previousY != Integer.MIN_VALUE) {
                    if (currentY > previousY) {
                        stampRiseX(level, minY, voidHeight, routeSeed, route, x, previousY, currentY);
                    } else if (currentY < previousY && currentY > voidHeight) {
                        stampDropX(level, minY, previousCenter, x - 1, previousY, currentY, Direction.EAST);
                    }
                }
                stampFlatX(level, minY, x, centerZ, currentY);
                previousCenter = centerZ;
                previousY = currentY;
            }
        }
    }

    private static void stampZRoutes(WorldGenLevel level, long seed, int minX, int maxX, int minZ, int maxZ, int minY, int voidHeight) {
        int minRoute = Mth.floor((minX - ROUTE_CHUNK_MARGIN - PATH_SPACING) / (double) PATH_SPACING);
        int maxRoute = Mth.floor((maxX + ROUTE_CHUNK_MARGIN + PATH_SPACING) / (double) PATH_SPACING);
        for (int route = minRoute; route <= maxRoute; route++) {
            long routeSeed = mix(seed ^ (route * 132897987541L) ^ 0x1f123bb5a4d3c25eL);
            int pathBaseHeight = basePathHeight(routeSeed, voidHeight);
            int previousCenter = Integer.MIN_VALUE;
            int previousY = Integer.MIN_VALUE;
            for (int z = minZ; z <= maxZ; z++) {
                int centerX = fixedRouteOffset(routeSeed, route);
                if (centerX < minX - PATH_HALF_WIDTH - 2 || centerX > maxX + PATH_HALF_WIDTH + 2) {
                    previousCenter = centerX;
                    previousY = sampleRouteHeight(routeSeed, z, pathBaseHeight, voidHeight);
                    continue;
                }

                int currentY = sampleRouteHeight(routeSeed, z, pathBaseHeight, voidHeight);
                if (previousY != Integer.MIN_VALUE) {
                    if (currentY > previousY) {
                        stampRiseZ(level, minY, voidHeight, routeSeed, route, z, previousY, currentY);
                    } else if (currentY < previousY && currentY > voidHeight) {
                        stampDropZ(level, minY, previousCenter, z - 1, previousY, currentY, Direction.SOUTH);
                    }
                }
                stampFlatZ(level, minY, centerX, z, currentY);
                previousCenter = centerX;
                previousY = currentY;
            }
        }
    }

    private static void stampFlatX(WorldGenLevel level, int minY, int x, int centerZ, int y) {
        for (int dz = -PATH_HALF_WIDTH; dz <= PATH_HALF_WIDTH; dz++) {
            stampPathColumn(level, x, centerZ + dz, minY, y);
        }
    }

    private static void stampFlatZ(WorldGenLevel level, int minY, int centerX, int z, int y) {
        for (int dx = -PATH_HALF_WIDTH; dx <= PATH_HALF_WIDTH; dx++) {
            stampPathColumn(level, centerX + dx, z, minY, y);
        }
    }

    private static void stampRiseX(WorldGenLevel level, int minY, int voidHeight, long routeSeed, int route, int x, int previousY, int currentY) {
        int delta = currentY - previousY;
        int startX = x - delta;
        for (int step = 0; step < delta; step++) {
            int stairX = startX + step;
            int height = previousY + step + 1;
            int routeCenterZ = fixedRouteOffset(routeSeed, route);
            for (int dz = -PATH_HALF_WIDTH; dz <= PATH_HALF_WIDTH; dz++) {
                stampPathColumn(level, stairX, routeCenterZ + dz, minY, Math.max(voidHeight + 1, height));
            }
        }
    }

    private static void stampRiseZ(WorldGenLevel level, int minY, int voidHeight, long routeSeed, int route, int z, int previousY, int currentY) {
        int delta = currentY - previousY;
        int startZ = z - delta;
        for (int step = 0; step < delta; step++) {
            int stairZ = startZ + step;
            int height = previousY + step + 1;
            int routeCenterX = fixedRouteOffset(routeSeed, route);
            for (int dx = -PATH_HALF_WIDTH; dx <= PATH_HALF_WIDTH; dx++) {
                stampPathColumn(level, routeCenterX + dx, stairZ, minY, Math.max(voidHeight + 1, height));
            }
        }
    }

    private static void stampDropX(WorldGenLevel level, int minY, int centerZ, int upperX, int upperY, int lowerY, Direction facing) {
        BlockState wingNormal = BlockRegistry.CLIFFROCK.get().defaultBlockState();
        BlockState wingTop = BlockRegistry.CLIFFROCK_PATH.get().defaultBlockState();
        for (int y = lowerY + 1; y <= upperY; y++) {
            setBlock(level, upperX, y, centerZ, BlockRegistry.CLIFFROCK_SLIDE.get().defaultBlockState().setValue(CliffrockSlideBlock.FACING, facing));
            boolean wingIsPathCap = y > lowerY && y >= upperY - 1;
            BlockState wing = wingIsPathCap ? wingTop : wingNormal;
            setBlock(level, upperX, y, centerZ - 1, wing);
            setBlock(level, upperX, y, centerZ + 1, wing);
        }
    }

    private static void stampDropZ(WorldGenLevel level, int minY, int centerX, int upperZ, int upperY, int lowerY, Direction facing) {
        BlockState wingNormal = BlockRegistry.CLIFFROCK.get().defaultBlockState();
        BlockState wingTop = BlockRegistry.CLIFFROCK_PATH.get().defaultBlockState();
        for (int y = lowerY + 1; y <= upperY; y++) {
            setBlock(level, centerX, y, upperZ, BlockRegistry.CLIFFROCK_SLIDE.get().defaultBlockState().setValue(CliffrockSlideBlock.FACING, facing));
            boolean wingIsPathCap = y > lowerY && y >= upperY - 1;
            BlockState wing = wingIsPathCap ? wingTop : wingNormal;
            setBlock(level, centerX - 1, y, upperZ, wing);
            setBlock(level, centerX + 1, y, upperZ, wing);
        }
    }

    private static void stampPathColumn(WorldGenLevel level, int x, int z, int minY, int surfaceY) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        if (surfaceY <= minY) {
            return;
        }
        for (int fillY = minY; fillY < surfaceY; fillY++) {
            cursor.set(x, fillY, z);
            setBlock(level, cursor, BlockRegistry.CLIFFROCK.get().defaultBlockState());
        }
        cursor.set(x, surfaceY, z);
        setBlock(level, cursor, BlockRegistry.CLIFFROCK_PATH.get().defaultBlockState());
        for (int airY = surfaceY + 1; airY <= surfaceY + PATH_CLEARANCE; airY++) {
            cursor.set(x, airY, z);
            setBlock(level, cursor, Blocks.AIR.defaultBlockState());
        }
    }

    /** Constant Z (for E–W segments) or X (for N–S segments); routes are axis-aligned with 90° crossings only. */
    private static int fixedRouteOffset(long routeSeed, int route) {
        return route * PATH_SPACING + offsetFromSeed(routeSeed, 18);
    }

    private static int basePathHeight(long routeSeed, int voidHeight) {
        return voidHeight + MIN_PATH_ABOVE_VOID + (int) Math.floorMod(routeSeed >>> 8, MAX_PATH_EXTRA_HEIGHT);
    }

    private static int sampleRouteHeight(long routeSeed, int axis, int baseHeight, int voidHeight) {
        double raw = Math.sin((axis + (routeSeed & 1023L)) * ALTITUDE_NOISE_SCALE) + 0.55D * Math.sin((axis - (routeSeed >>> 10 & 1023L)) * ALTITUDE_NOISE_SCALE * 0.5D);
        int snapped = Math.round((float) raw);
        return Math.max(voidHeight + MIN_PATH_ABOVE_VOID, baseHeight + snapped * ALTITUDE_STEP);
    }

    private static int offsetFromSeed(long seed, int bound) {
        return (int) Math.floorMod(seed, bound) - bound / 2;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static void setBlock(WorldGenLevel level, int x, int y, int z, BlockState state) {
        setBlock(level, new BlockPos.MutableBlockPos(x, y, z), state);
    }

    private static void setBlock(WorldGenLevel level, BlockPos.MutableBlockPos pos, BlockState state) {
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
            return;
        }
        level.setBlock(pos, state, 2);
    }
}
