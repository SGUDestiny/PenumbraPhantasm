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

    private static final ForgeConfigSpec.IntValue FOUNTAIN_FIRST_TELEPORT_MIN_RADIUS = BUILDER
            .comment("Minimum distance from Dark Fountain to teleport the player when the fountain fills the rooms for the first time. Must be less than maximum")
            .comment("Default: 8")
            .defineInRange("fountain_first_teleport_min_radius", 8, 8, 1048576);
    private static final ForgeConfigSpec.IntValue FOUNTAIN_FIRST_TELEPORT_MAX_RADIUS = BUILDER
            .comment("Minimum distance from Dark Fountain to teleport the player when the fountain fills the rooms for the first time. Must be more than minimum")
            .comment("Default: 16")
            .defineInRange("fountain_first_teleport_max_radius", 16, 16, 1048576);

    private static final ForgeConfigSpec.IntValue FOUNTAIN_CONTACT_TELEPORT_MIN_RADIUS = BUILDER
            .comment("Minimum distance from Dark Fountain to teleport the player when they enter fountain's darkness volume. Must be less than maximum")
            .comment("Default: 512")
            .defineInRange("fountain_contact_teleport_min_radius", 512, 32, 1048576);
    private static final ForgeConfigSpec.IntValue FOUNTAIN_CONTACT_TELEPORT_MAX_RADIUS = BUILDER
            .comment("Minimum distance from Dark Fountain to teleport the player when they enter fountain's darkness volume. Must be more than minimum")
            .comment("Default: 1024")
            .defineInRange("fountain_contact_teleport_max_radius", 1024, 64, 1048576);

    private static final ForgeConfigSpec.IntValue GREAT_DOOR_PLACE_MIN_RADIUS = BUILDER
            .comment("Minimum distance from Dark Fountain to place a Great Door. Must be less than maximum")
            .comment("Default: 512")
            .defineInRange("great_door_place_min_radius", 512, 32, 1048576);
    private static final ForgeConfigSpec.IntValue GREAT_DOOR_PLACE_MAX_RADIUS = BUILDER
            .comment("Maximum distance from Dark Fountain to place a Great Door. Must be more than minimum")
            .comment("Default: 1024")
            .defineInRange("great_door_place_max_radius", 1024, 64, 1048576);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean realKnifeOP;
    public static int maxRoomVolume;
    public static int rescanInterval;
    public static int dissipationRate;
    public static int fountainFirstTeleportMinRadius;
    public static int fountainFirstTeleportMaxRadius;
    public static int fountainContactTeleportMinRadius;
    public static int fountainContactTeleportMaxRadius;
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
        fountainFirstTeleportMinRadius = FOUNTAIN_FIRST_TELEPORT_MIN_RADIUS.get();
        fountainFirstTeleportMaxRadius = FOUNTAIN_FIRST_TELEPORT_MAX_RADIUS.get();
        fountainContactTeleportMinRadius = FOUNTAIN_CONTACT_TELEPORT_MIN_RADIUS.get();
        fountainContactTeleportMaxRadius = FOUNTAIN_CONTACT_TELEPORT_MAX_RADIUS.get();
        greatDoorPlaceMinRadius = GREAT_DOOR_PLACE_MIN_RADIUS.get();
        greatDoorPlaceMaxRadius = GREAT_DOOR_PLACE_MAX_RADIUS.get();
    }
}
