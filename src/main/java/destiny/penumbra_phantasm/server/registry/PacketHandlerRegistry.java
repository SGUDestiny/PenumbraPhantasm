package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundIntroPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundSingleFountainData;
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

        INSTANCE.messageBuilder(ClientBoundSoundPackets.FountainWind.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSoundPackets.FountainWind::encode)
                .decoder(ClientBoundSoundPackets.FountainWind::new)
                .consumerMainThread(ClientBoundSoundPackets.FountainWind::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundSoundPackets.FountainDarkness.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSoundPackets.FountainDarkness::encode)
                .decoder(ClientBoundSoundPackets.FountainDarkness::new)
                .consumerMainThread(ClientBoundSoundPackets.FountainDarkness::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundSingleFountainData.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSingleFountainData::encode)
                .decoder(ClientBoundSingleFountainData::decode)
                .consumerMainThread(ClientBoundSingleFountainData::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundIntroPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundIntroPacket::encode)
                .decoder(ClientBoundIntroPacket::decode)
                .consumerMainThread(ClientBoundIntroPacket::handle)
                .add();
    }
}
