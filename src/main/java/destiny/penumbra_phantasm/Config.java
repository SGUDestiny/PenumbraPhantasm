package destiny.penumbra_phantasm;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue REAL_KNIFE_OP = BUILDER
            .comment("Should Real Knife have canonical damage")
            .define("real_knife_op", false);

    private static final ForgeConfigSpec.BooleanValue DARK_FOUNTAIN_MUSIC = BUILDER
            .comment("Should music be played in the vicinity of Dark Fountains")
            .define("dark_fountain_music", true);

    private static final ForgeConfigSpec.BooleanValue ALTERNATE_DARK_FOUNTAIN_MUSIC = BUILDER
            .comment("Should an alternate version of THE HOLY be played instead? (Has extended Penumbra Phantasm motif)")
            .define("alternate_dark_fountain_music", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean realKnifeOP;
    public static boolean darkFountainMusic;
    public static boolean alternateDarkFountainMusic;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        realKnifeOP = REAL_KNIFE_OP.get();
        darkFountainMusic = DARK_FOUNTAIN_MUSIC.get();
        alternateDarkFountainMusic = ALTERNATE_DARK_FOUNTAIN_MUSIC.get();
    }
}
