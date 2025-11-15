package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.sounds.SoundAccess;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ClientBoundSoundPackets {
    public final DarkFountain fountain;
    public final boolean stop;

    public ClientBoundSoundPackets(DarkFountain fountain, boolean stop)
    {
        this.fountain = fountain;
        this.stop = stop;
    }

    public ClientBoundSoundPackets(FriendlyByteBuf buffer)
    {
        this(buffer.read, buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.stop);
    }

    public abstract boolean handle(Supplier<NetworkEvent.Context> ctx);

    public static class FountainMusic extends ClientBoundSoundPackets
    {
        public FountainMusic(DarkFountain fountain, boolean stop)
        {
            super(fountain, stop);
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
                SoundAccess.playFountainMusic(fountain, stop);
            });
            return true;
        }
    }

    public static class FountainWind extends ClientBoundSoundPackets
    {
        public FountainWind(DarkFountain fountain, boolean stop)
        {
            super(fountain, stop);
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
                SoundAccess.playFountainWind(fountain, stop);
            });
            return true;
        }
    }
}
