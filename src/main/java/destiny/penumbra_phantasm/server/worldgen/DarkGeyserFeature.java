package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;

public class DarkGeyserFeature extends Feature<NoneFeatureConfiguration> {
    public DarkGeyserFeature(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos originPos = context.origin();
        RandomSource random = context.random();

        double baseRadius = random.nextInt(6, 12);
        double tipHeight = random.nextInt(4, 12);

        //Create vertex 1
        double vertexAngle1 = random.nextDouble() * 2 * Math.PI;
        double vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle1) * baseRadius;
        double vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle1) * baseRadius;
        BlockPos vertexPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, new BlockPos((int) vertexX, originPos.getY(), (int) vertexZ));
        Vec3 vertexBase1 = vertexPos.getCenter();

        //Create vertex 2
        double vertexAngle2 = vertexAngle1 + (2 * Math.PI / 4);
        vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle2) * baseRadius;
        vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle2) * baseRadius;
        vertexPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, new BlockPos((int) vertexX, originPos.getY(), (int) vertexZ));
        Vec3 vertexBase2 = vertexPos.getCenter();

        //Create vertex 3
        double vertexAngle3 = vertexAngle1 + (2 * Math.PI / 4) * 2;
        vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle3) * baseRadius;
        vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle3) * baseRadius;
        vertexPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, new BlockPos((int) vertexX, originPos.getY(), (int) vertexZ));
        Vec3 vertexBase3 = vertexPos.getCenter();

        //Create vertex 4
        double vertexAngle4 = vertexAngle1 + (2 * Math.PI / 4) * 3;
        vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle4) * baseRadius;
        vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle4) * baseRadius;
        vertexPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, new BlockPos((int) vertexX, originPos.getY(), (int) vertexZ));
        Vec3 vertexBase4 = vertexPos.getCenter();

        if (!level.getBlockState(new BlockPos((int) vertexBase1.x, (int) vertexBase1.y, (int) vertexBase1.z)).is(BlockRegistry.NEGATIVE_PHOTONS.get()) &&
                !level.getBlockState(new BlockPos((int) vertexBase2.x, (int) vertexBase2.y, (int) vertexBase2.z)).is(BlockRegistry.NEGATIVE_PHOTONS.get()) &&
                !level.getBlockState(new BlockPos((int) vertexBase3.x, (int) vertexBase3.y, (int) vertexBase3.z)).is(BlockRegistry.NEGATIVE_PHOTONS.get()) &&
                !level.getBlockState(new BlockPos((int) vertexBase4.x, (int) vertexBase4.y, (int) vertexBase4.z)).is(BlockRegistry.NEGATIVE_PHOTONS.get())) {
            return false;
        } else {
            vertexBase1.add(0, -2, 0);
            vertexBase2.add(0, -2, 0);
            vertexBase3.add(0, -2, 0);
            vertexBase4.add(0, -2, 0);
        }

        //Create tip vertex
        double vertexAngle5 = random.nextDouble() * 2 * Math.PI;
        vertexX = originPos.getX() + 0.5;
        vertexZ = originPos.getZ() + 0.5;
        BlockPos tipPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, originPos);

        int liquidDepth = 0;
        for (int i = 0; i < 64; i++) {
            if (level.getBlockState(tipPos.offset(0, i, 0)).is(BlockRegistry.NEGATIVE_PHOTONS.get())) {
                liquidDepth++;
            } else {
                break;
            }
        }

        if (liquidDepth <= 1 || liquidDepth >= 64) {
            return false;
        }

        Vec3 vertexTip = new Vec3(vertexX, tipPos.offset(0, liquidDepth, 0).getY() + tipHeight, vertexZ);

        //Create general bounding box
        int minX = (int) Math.floor(Math.min(vertexBase1.x, Math.min(vertexBase2.x, Math.min(vertexBase3.x, Math.min(vertexBase4.x, vertexTip.x)))));
        int minY = (int) Math.floor(Math.min(vertexBase1.y, Math.min(vertexBase2.y, Math.min(vertexBase3.y, Math.min(vertexBase4.y, vertexTip.y)))));
        int minZ = (int) Math.floor(Math.min(vertexBase1.z, Math.min(vertexBase2.z, Math.min(vertexBase3.z, Math.min(vertexBase4.z, vertexTip.z)))));

        int maxX = (int) Math.ceil(Math.max(vertexBase1.x, Math.max(vertexBase2.x, Math.max(vertexBase3.x, Math.max(vertexBase4.x, vertexTip.x)))));
        int maxY = (int) Math.ceil(Math.max(vertexBase1.y, Math.max(vertexBase2.y, Math.max(vertexBase3.y, Math.max(vertexBase4.y, vertexTip.y)))));
        int maxZ = (int) Math.ceil(Math.max(vertexBase1.z, Math.max(vertexBase2.z, Math.max(vertexBase3.z, Math.max(vertexBase4.z, vertexTip.z)))));

        //Check if given block in the bounding box intersects with the tetrahedron
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos potentialPos = new BlockPos(x, y, z);

                    //If it is, place a block in its spot
                    if (isInsidePyramid(potentialPos.getCenter(), vertexBase1, vertexBase2, vertexBase3, vertexBase4, vertexTip)) {
                        level.setBlock(potentialPos, BlockRegistry.TENEBRALITH.get().defaultBlockState(), 2);
                    }
                }
            }
        }

        level.setBlock(new BlockPos((int) (vertexTip.x - 0.5), (int) vertexTip.y, (int) vertexTip.z), BlockRegistry.DARK_GEYSER.get().defaultBlockState(), 2);

        return true;
    }

    public static boolean isInsidePyramid(Vec3 point, Vec3 vertexBase1, Vec3 vertexBase2, Vec3 vertexBase3, Vec3 vertexBase4, Vec3 vertexTip) {
        return isSameSide(point, vertexBase1, vertexBase2, vertexBase3, vertexTip)
                && isSameSide(point, vertexBase1, vertexBase3, vertexBase4, vertexTip)
                && isSameSide(point, vertexBase2, vertexBase3, vertexTip, vertexBase1.lerp(vertexBase4, 0.5))
                && isSameSide(point, vertexBase3, vertexBase4, vertexTip, vertexBase1.lerp(vertexBase2, 0.5))
                && isSameSide(point, vertexBase4, vertexBase1, vertexTip, vertexBase3.lerp(vertexBase2, 0.5))
                && isSameSide(point, vertexBase1, vertexBase2, vertexTip, vertexBase3.lerp(vertexBase4, 0.5));
    }

    public static boolean isSameSide(Vec3 point, Vec3 vertex1, Vec3 vertex2, Vec3 vertex3, Vec3 vertexOpposite) {
        Vec3 normal = vertex2.subtract(vertex1).cross(vertex3.subtract(vertex1));
        double pointSide = normal.dot(point.subtract(vertex1));
        double oppositeSide = normal.dot(vertexOpposite.subtract(vertex1));

        return pointSide * oppositeSide >= 0;
    }
}
