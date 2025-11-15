package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.sounds.SoundAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class ClientBoundSoundPackets {
    public final UUID fountainUuid;
    public final boolean stop;

    public ClientBoundSoundPackets(UUID fountainUuid, boolean stop)
    {
        this.fountainUuid = fountainUuid;
        this.stop = stop;
    }

    public ClientBoundSoundPackets(FriendlyByteBuf buffer)
    {
        this(buffer.readUUID(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUUID(this.fountainUuid);
        buffer.writeBoolean(this.stop);
    }

    public abstract boolean handle(Supplier<NetworkEvent.Context> ctx);

    public static class FountainMusic extends ClientBoundSoundPackets
    {
        public FountainMusic(UUID fountainUuid, boolean stop)
        {
            super(fountainUuid, stop);
        }
        public FountainMusic(FriendlyByteBuf buffer)
        {
            super(buffer);
        }

        @Override
        public boolean handle(Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
            {
                SoundAccess.playFountainMusic(fountainUuid, stop);
            });
            return true;
        }
    }

    public static class FountainWind extends ClientBoundSoundPackets
    {
        public FountainWind(UUID fountainUuid, boolean stop)
        {
            super(fountainUuid, stop);
        }
        public FountainWind(FriendlyByteBuf buffer)
        {
            super(buffer);
        }

        @Override
        public boolean handle(Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
            {
                SoundAccess.playFountainWind(fountainUuid, stop);
            });
            return true;
        }
    }
}
