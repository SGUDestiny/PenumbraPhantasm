package destiny.penumbra_phantasm.client;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue FOUNTAIN_MAKING_POSTERIZATION = BUILDER
            .comment("When a fountain is made, should the screen be posterized to black and white?")
            .comment("Default: true")
            .define("fountain_making_posterization", true);

    private static final ForgeConfigSpec.BooleanValue FOUNTAIN_PROXIMITY_RAINBOW = BUILDER
            .comment("Should dark world fountains apply rainbow tint when nearby?")
            .comment("Default: true")
            .define("fountain_proximity_rainbow", true);

    private static final ForgeConfigSpec.BooleanValue ALWAYS_SHOW_LOCATION_TITLES = BUILDER
            .comment("Should the location titles appear every time the location is changed and not only once per world login?")
            .comment("Default: false")
            .define("always_show_location_titles", false);

    private static final ForgeConfigSpec.DoubleValue FOUNTAIN_LOD_DISTANCE = BUILDER
            .comment("Distance at which the dark world fountain beings to turn into the low detail purple version")
            .comment("Default: 64")
            .defineInRange("fountain_lod_distance", 64d, 8d, 256d);

    private static final ForgeConfigSpec.BooleanValue BIOME_MUSIC_LOOP = BUILDER
            .comment("Is biome music played on repeat?")
            .comment("Default: true")
            .define("biome_music_loop", true);

    private static final ForgeConfigSpec.IntValue BIOME_MUSIC_MIN_DELAY = BUILDER
            .comment("Min delay of biome music in seconds, should be less than max")
            .comment("Default: 60 (1 minute)")
            .defineInRange("biome_music_min_delay", 60, 0, 60 * 60);

    private static final ForgeConfigSpec.IntValue BIOME_MUSIC_MAX_DELAY = BUILDER
            .comment("Max delay of biome music in seconds, should be more than min")
            .comment("Default: 900 (15 minutes)")
            .defineInRange("biome_music_max_delay", 15 * 60, 0, 60 * 60);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean fountainMakingPosterization;
    public static boolean fountainProximityRainbow;
    public static boolean always_show_location_titles;
    public static double fountainLodDistance;
    public static boolean biomeMusicLoop;
    public static int biomeMusicMinDelay;
    public static int biomeMusicMaxDelay;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        fountainMakingPosterization = FOUNTAIN_MAKING_POSTERIZATION.get();
        fountainProximityRainbow = FOUNTAIN_PROXIMITY_RAINBOW.get();
        always_show_location_titles = ALWAYS_SHOW_LOCATION_TITLES.get();
        fountainLodDistance = FOUNTAIN_LOD_DISTANCE.get();
        biomeMusicLoop = BIOME_MUSIC_LOOP.get();
        biomeMusicMinDelay = BIOME_MUSIC_MIN_DELAY.get();
        biomeMusicMaxDelay = BIOME_MUSIC_MAX_DELAY.get();
    }
}
