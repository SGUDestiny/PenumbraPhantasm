package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.*;
import destiny.penumbra_phantasm.server.network.*;
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

        INSTANCE.messageBuilder(ServerBoundIntroPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundIntroPacket::encode)
                .decoder(ServerBoundIntroPacket::decode)
                .consumerMainThread(ServerBoundIntroPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundSoulPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundSoulPacket::encode)
                .decoder(ServerBoundSoulPacket::decode)
                .consumerMainThread(ServerBoundSoulPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundSoulBreakPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSoulBreakPacket::encode)
                .decoder(ClientBoundSoulBreakPacket::decode)
                .consumerMainThread(ClientBoundSoulBreakPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundTransportTickerPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundTransportTickerPacket::encode)
                .decoder(ClientBoundTransportTickerPacket::decode)
                .consumerMainThread(ClientBoundTransportTickerPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundDarknessFallPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundDarknessFallPacket::encode)
                .decoder(ClientBoundDarknessFallPacket::decode)
                .consumerMainThread(ClientBoundDarknessFallPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundDarknessFallPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundDarknessFallPacket::encode)
                .decoder(ServerBoundDarknessFallPacket::decode)
                .consumerMainThread(ServerBoundDarknessFallPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundSealShinePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundSealShinePacket::encode)
                .decoder(ClientBoundSealShinePacket::decode)
                .consumerMainThread(ClientBoundSealShinePacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundParticlePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundParticlePacket::encode)
                .decoder(ClientBoundParticlePacket::decode)
                .consumerMainThread(ClientBoundParticlePacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundPlayPlayerAnimationPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundPlayPlayerAnimationPacket::encode)
                .decoder(ClientBoundPlayPlayerAnimationPacket::decode)
                .consumerMainThread(ClientBoundPlayPlayerAnimationPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundCancelPlayerAnimationPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundCancelPlayerAnimationPacket::encode)
                .decoder(ClientBoundCancelPlayerAnimationPacket::decode)
                .consumerMainThread(ClientBoundCancelPlayerAnimationPacket::handle)
                .add();
    }
}