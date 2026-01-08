package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundFountainData;
import destiny.penumbra_phantasm.client.network.ClientBoundSoundPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandlerRegistry {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(PenumbraPhantasm.MODID, "main_network"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private PacketHandlerRegistry(){}

    public static void register()
    {
        int index = 0;

        INSTANCE.messageBuilder(ClientBoundSoundPackets.FountainMusic.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSoundPackets.FountainMusic::encode)
                .decoder(ClientBoundSoundPackets.FountainMusic::new)
                .consumerMainThread(ClientBoundSoundPackets.FountainMusic::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundSoundPackets.FountainDarkWind.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSoundPackets.FountainDarkWind::encode)
                .decoder(ClientBoundSoundPackets.FountainDarkWind::new)
                .consumerMainThread(ClientBoundSoundPackets.FountainDarkWind::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundSoundPackets.FountainLightWind.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSoundPackets.FountainLightWind::encode)
                .decoder(ClientBoundSoundPackets.FountainLightWind::new)
                .consumerMainThread(ClientBoundSoundPackets.FountainLightWind::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundFountainData.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundFountainData::encode)
                .decoder(ClientBoundFountainData::decode)
                .consumerMainThread(ClientBoundFountainData::handle)
                .add();
    }
}
