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

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean realKnifeOP;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        realKnifeOP = REAL_KNIFE_OP.get();
    }
}
