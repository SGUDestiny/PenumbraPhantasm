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

public class LargeTenebralithSpikeFeature extends Feature<NoneFeatureConfiguration> {
    public LargeTenebralithSpikeFeature(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos originPos = context.origin();
        RandomSource random = context.random();

        double baseRadius = random.nextInt(8, 16);
        double tipRadius = random.nextInt(12, 24);
        double tipHeight = random.nextInt(16, 32);

        //Create vertex 1
        double vertexAngle1 = random.nextDouble() * 2 * Math.PI;
        double vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle1) * baseRadius;
        double vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle1) * baseRadius;
        BlockPos vertexPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos((int) vertexX, originPos.getY(), (int) vertexZ));
        Vec3 vertexBase1 = vertexPos.getCenter();

        //Create vertex 2
        double vertexAngle2 = vertexAngle1 + (2 * Math.PI / 3);
        vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle2) * baseRadius;
        vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle2) * baseRadius;
        vertexPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos((int) vertexX, originPos.getY(), (int) vertexZ));
        Vec3 vertexBase2 = vertexPos.getCenter();

        //Create vertex 3
        double vertexAngle3 = vertexAngle1 + (4 * Math.PI / 3);
        vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle3) * baseRadius;
        vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle3) * baseRadius;
        vertexPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos((int) vertexX, originPos.getY(), (int) vertexZ));
        Vec3 vertexBase3 = vertexPos.getCenter();

        //Create tip vertex
        double vertexAngle4 = random.nextDouble() * 2 * Math.PI;
        vertexX = originPos.getX() + 0.5 + Math.cos(vertexAngle4) * tipRadius;
        vertexZ = originPos.getZ() + 0.5 + Math.sin(vertexAngle4) * tipRadius;
        Vec3 vertexTip = new Vec3(vertexX, originPos.getY() + tipHeight, vertexZ);

        //Create general bounding box
        int minX = (int) Math.floor(Math.min(vertexBase1.x, Math.min(vertexBase2.x, Math.min(vertexBase3.x, vertexTip.x))));
        int minY = (int) Math.floor(Math.min(vertexBase1.y, Math.min(vertexBase2.y, Math.min(vertexBase3.y, vertexTip.y))));
        int minZ = (int) Math.floor(Math.min(vertexBase1.z, Math.min(vertexBase2.z, Math.min(vertexBase3.z, vertexTip.z))));

        int maxX = (int) Math.ceil(Math.max(vertexBase1.x, Math.max(vertexBase2.x, Math.max(vertexBase3.x, vertexTip.x))));
        int maxY = (int) Math.ceil(Math.max(vertexBase1.y, Math.max(vertexBase2.y, Math.max(vertexBase3.y, vertexTip.y))));
        int maxZ = (int) Math.ceil(Math.max(vertexBase1.z, Math.max(vertexBase2.z, Math.max(vertexBase3.z, vertexTip.z))));

        //Check if given block in the bounding box intersects with the tetrahedron
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos potentialPos = new BlockPos(x, y, z);

                    //If it is, place a block in its spot
                    if (isInsideTetrahedron(potentialPos.getCenter(), vertexBase1, vertexBase2, vertexBase3, vertexTip)) {
                        level.setBlock(potentialPos, BlockRegistry.TENEBRALITH.get().defaultBlockState(), 2);
                    }
                }
            }
        }

        return true;
    }

    public static boolean isInsideTetrahedron(Vec3 point, Vec3 vertexBase1, Vec3 vertexBase2, Vec3 vertexBase3, Vec3 vertexTip) {
        return isSameSide(point, vertexBase1, vertexBase2, vertexBase3, vertexTip) && isSameSide(point, vertexBase2, vertexBase3, vertexTip, vertexBase1) &&
                isSameSide(point, vertexBase3, vertexTip, vertexBase1, vertexBase2) && isSameSide(point, vertexTip, vertexBase1, vertexBase2, vertexBase3);
    }

    public static boolean isSameSide(Vec3 point, Vec3 vertex1, Vec3 vertex2, Vec3 vertex3, Vec3 vertexOpposite) {
        Vec3 normal = vertex2.subtract(vertex1).cross(vertex3.subtract(vertex1));
        double pointSide = normal.dot(point.subtract(vertex1));
        double oppositeSide = normal.dot(vertexOpposite.subtract(vertex1));

        return pointSide * oppositeSide >= 0;
    }
}
