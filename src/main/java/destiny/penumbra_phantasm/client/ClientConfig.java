package destiny.penumbra_phantasm.client;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ALTERNATE_DARK_FOUNTAIN_MUSIC = BUILDER
            .comment("Should an alternate version of THE HOLY be played instead? (Has extended Penumbra Phantasm motif)")
            .comment("Default: true")
            .define("alternate_dark_fountain_music", true);

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
            .comment("Default: false")
            .define("biome_music_loop", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean alternateDarkFountainMusic;
    public static boolean always_show_location_titles;
    public static double fountainLodDistance;
    public static boolean biomeMusicLoop;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        alternateDarkFountainMusic = ALTERNATE_DARK_FOUNTAIN_MUSIC.get();
        always_show_location_titles = ALWAYS_SHOW_LOCATION_TITLES.get();
        fountainLodDistance = FOUNTAIN_LOD_DISTANCE.get();
        biomeMusicLoop = BIOME_MUSIC_LOOP.get();
    }
}
