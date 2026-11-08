package destiny.penumbra_phantasm.server.egg_room;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CardKingdomEggRoomUtil {
	public static final ResourceKey<Level> CARD_KINGDOM_EGG_ROOM = ResourceKey.create(Registries.DIMENSION,
			new ResourceLocation(PenumbraPhantasm.MODID, "egg_room_card_kingdom"));

	public static final int CARD_KINGDOM_BIT = 1;

	public static final int PLACE_Y = 64;
	public static final int FLOOR_Y = 64;
	public static final int MIN_X = -10;
	public static final int MAX_X = 10;
	public static final int MIN_Z = -10;
	public static final int MAX_Z = 10;
	public static final int CEILING_Y = PLACE_Y + 9;

	public static final double SPAWN_X = 0.5;
	public static final double SPAWN_Y = FLOOR_Y + 1;
	public static final double SPAWN_Z = MIN_Z + 2.0;
	public static final float SPAWN_YAW = 0f;

	public static final double CAMERA_X = 0.5;
	public static final double CAMERA_Y = FLOOR_Y + 8;
	public static final double CAMERA_Z = MIN_Z + 1.5;
	public static final float LOOK_SMOOTH = 0.35f;

	public static final double ENTRANCE_Z = MIN_Z + 2;
	public static final double LEFT_ENTRANCE_Z = SPAWN_Z;

	private CardKingdomEggRoomUtil() {
	}

	public static boolean isEggRoom(Level level) {
		return isEggRoomKey(level.dimension());
	}

	public static boolean isEggRoomKey(ResourceKey<Level> key) {
		return key.location().getPath().contains("egg_room");
	}

	public static boolean isCardKingdomPlayable(Level level) {
		String path = level.dimension().location().getPath();
		return path.contains("card_kingdom") && !path.contains("egg_room");
	}

	public static boolean insideRoom(double x, double y, double z) {
		return x >= MIN_X && x < MAX_X + 1 && z >= MIN_Z && z < MAX_Z + 1 && y >= FLOOR_Y && y < CEILING_Y + 1;
	}

	public static boolean inEntranceZone(double x, double z) {
		return Math.abs(x - SPAWN_X) <= 1.5 && z >= MIN_Z + 1 && z < MIN_Z + 2;
	}

	public static boolean northOfRoom(double z) {
		return z < MIN_Z - 1;
	}

	public static boolean inTreeFront(double x, double z, int treeX, int treeZ) {
		return Math.abs(x - (treeX + 0.5)) <= 1.5 && z < treeZ && z >= treeZ - 2.5;
	}

	public static boolean inTreeBehind(double x, double z, int treeX, int treeZ) {
		return Math.abs(x - (treeX + 0.5)) <= 1.5 && z > treeZ && z <= treeZ + 2.5;
	}

	public static Vec3 spawnPos() {
		return new Vec3(SPAWN_X, SPAWN_Y, SPAWN_Z);
	}

	public static Vec2 cameraLook(double camX, double camY, double camZ, double targetX, double targetY, double targetZ) {
		double dx = targetX - camX;
		double dy = targetY - camY;
		double dz = targetZ - camZ;
		float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
		float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI));

		return new Vec2(yaw, pitch);
	}

	public static Vec2 cameraAwayFlat(double playerX, double playerZ) {
		double ax = playerX - CAMERA_X;
		double az = playerZ - CAMERA_Z;
		double len = Math.sqrt(ax * ax + az * az);

		if (len < 1.0E-4) {
			return new Vec2(0f, 1f);
		}

		return new Vec2((float) (ax / len), (float) (az / len));
	}

	public static Vec2 worldWish(Vec2 away, float forwardImpulse, float leftImpulse) {
		float rightX = away.y;
		float rightZ = -away.x;

		return new Vec2(away.x * forwardImpulse + rightX * leftImpulse,
				away.y * forwardImpulse + rightZ * leftImpulse);
	}

	public static Vec2 worldToLocal(Vec2 world, float yRot) {
		float sin = Mth.sin(yRot * ((float) Math.PI / 180f));
		float cos = Mth.cos(yRot * ((float) Math.PI / 180f));

		return new Vec2(world.x * cos + world.y * sin, world.y * cos - world.x * sin);
	}

	public static float yawFromWish(float wishX, float wishZ) {
		return (float) (Mth.atan2(-wishX, wishZ) * (180.0 / Math.PI));
	}
}
