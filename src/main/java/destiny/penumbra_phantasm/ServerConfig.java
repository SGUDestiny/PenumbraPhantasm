package destiny.penumbra_phantasm;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue REAL_KNIFE_OP = BUILDER
            .comment("Should Real Knife have canonical damage")
            .comment("Default: false")
            .define("real_knife_op", false);

    private static final ForgeConfigSpec.IntValue MAX_ROOM_VOLUME = BUILDER
            .comment("Maximum total darkness blocks across all connected rooms for a Dark Fountain")
            .comment("Default: 2048")
            .defineInRange("max_room_volume", 2048, 1, 8000);

    private static final ForgeConfigSpec.IntValue RESCAN_INTERVAL = BUILDER
            .comment("Ticks between room integrity re-scan cycles")
            .comment("Default: 20")
            .defineInRange("rescan_interval", 20, 1, 200);

    private static final ForgeConfigSpec.IntValue DISSIPATION_RATE = BUILDER
            .comment("Darkness blocks removed per tick during dissipation")
            .comment("Default: 5")
            .defineInRange("dissipation_rate", 5, 1, 50);

    private static final ForgeConfigSpec.IntValue GREAT_DOOR_PLACE_MIN_RADIUS = BUILDER
            .comment("Minimum horizontal distance (blocks) from dark fountain anchor to try placing great_door structure")
            .comment("Default: 16")
            .defineInRange("great_door_place_min_radius", 16, 1, 512);

    private static final ForgeConfigSpec.IntValue GREAT_DOOR_PLACE_MAX_RADIUS = BUILDER
            .comment("Maximum horizontal distance (blocks) from dark fountain anchor to try placing great_door structure")
            .comment("Default: 48")
            .defineInRange("great_door_place_max_radius", 48, 1, 512);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean realKnifeOP;
    public static int maxRoomVolume;
    public static int rescanInterval;
    public static int dissipationRate;
    public static int greatDoorPlaceMinRadius;
    public static int greatDoorPlaceMaxRadius;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        realKnifeOP = REAL_KNIFE_OP.get();
        maxRoomVolume = MAX_ROOM_VOLUME.get();
        rescanInterval = RESCAN_INTERVAL.get();
        dissipationRate = DISSIPATION_RATE.get();
        greatDoorPlaceMinRadius = GREAT_DOOR_PLACE_MIN_RADIUS.get();
        greatDoorPlaceMaxRadius = GREAT_DOOR_PLACE_MAX_RADIUS.get();
    }
}
