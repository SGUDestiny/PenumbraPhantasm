package destiny.penumbra_phantasm.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(
        modid = PenumbraPhantasm.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModShaders {

    public static ShaderInstance FOUNTAIN_MASKED;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {

        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation("penumbra_phantasm", "fountain_masked"),
                        DefaultVertexFormat.POSITION_COLOR_TEX
                ),
                s -> FOUNTAIN_MASKED = s
        );


    }
}
