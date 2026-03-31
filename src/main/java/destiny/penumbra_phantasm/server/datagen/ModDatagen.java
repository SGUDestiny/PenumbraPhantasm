package destiny.penumbra_phantasm.server.datagen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDatagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        event.getGenerator().addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        event.getGenerator().addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        event.getGenerator().addProvider(event.includeServer(), ModLootTableProvider.create(packOutput));
    }
}
