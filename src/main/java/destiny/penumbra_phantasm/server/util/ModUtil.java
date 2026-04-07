package destiny.penumbra_phantasm.server.util;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public class ModUtil {
    public static Entity teleportEntity(Entity entity, ServerLevel destinationLevel, Vec3 targetPos) {
        return entity.changeDimension(destinationLevel, new DarkFountain.DarkFountainTeleporter(targetPos, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()));
    }

    public static ResourceKey<Level> stringToDimension(String dimensionString) {
        String[] split = dimensionString.split(":");
        if (split.length > 1)
            return ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(split[0], split[1]));
        return null;
    }

    public static VoxelShape buildShape(VoxelShape... from) {
        return Stream.of(from).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    public float getBoundRandomFloat(Level level, float origin, float limit) {
        return origin + (limit - origin) * level.getRandom().nextFloat();
    }

    public static float getBoundRandomFloatStatic(Level level, float origin, float limit) {
        return origin + (limit - origin) * level.getRandom().nextFloat();
    }

    public double getBoundRandomDouble(Level level, double origin, double limit) {
        return origin + (limit - origin) * level.getRandom().nextDouble();
    }

    public static double getBoundRandomDoubleStatic(Level level, double origin, double limit) {
        return origin + (limit - origin) * level.getRandom().nextDouble();
    }

    public static float wrapRad(float pValue) {
        float p = (float) (Math.PI * 2);
        float d0 = pValue % p;
        if (d0 >= Math.PI) {
            d0 -= p;
        }

        if (d0 < -Math.PI) {
            d0 += p;
        }

        return d0;
    }
}
