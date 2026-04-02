package destiny.penumbra_phantasm.server.datagen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BlocksetDatagen {

    private BlocksetDatagen() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        Path packOutput = event.getGenerator().getPackOutput().getOutputFolder();
        Path mainResources = packOutput.resolve("../../main/resources").normalize();
        try {
            if (event.includeClient()) {
                BlocksetClientProvider.run(mainResources);
            }
            if (event.includeServer()) {
                BlocksetServerProvider.run(mainResources);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
