package destiny.penumbra_phantasm.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShaderRegistry {
    public static ShaderInstance FOUNTAIN_MASKED;
    public static ShaderInstance OPENING_POSTERIZE;
    public static ShaderInstance HUE_SHIFT;
    public static ShaderInstance POSITION_COLOR_TEX_ALPHA;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(PenumbraPhantasm.MODID, "fountain_masked"),
                        DefaultVertexFormat.POSITION_COLOR_TEX), shader -> FOUNTAIN_MASKED = shader
        );

        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(PenumbraPhantasm.MODID, "opening_posterize"),
                        DefaultVertexFormat.POSITION_TEX), shader -> OPENING_POSTERIZE = shader
        );

        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(PenumbraPhantasm.MODID, "hue_shift"),
                        DefaultVertexFormat.POSITION_TEX), shader -> HUE_SHIFT = shader
        );

        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(PenumbraPhantasm.MODID, "position_color_tex_alpha"),
                DefaultVertexFormat.POSITION_COLOR_TEX), shader -> POSITION_COLOR_TEX_ALPHA = shader
        );
    }
}