package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.sounds.SoundAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class ClientBoundSoundPackets {
    public final BlockPos fountainPos;
    public final boolean stop;

    public ClientBoundSoundPackets(BlockPos fountainPos, boolean stop)
    {
        this.fountainPos = fountainPos;
        this.stop = stop;
    }

    public ClientBoundSoundPackets(FriendlyByteBuf buffer)
    {
        this(buffer.readBlockPos(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(this.fountainPos);
        buffer.writeBoolean(this.stop);
    }

    public abstract boolean handle(Supplier<NetworkEvent.Context> ctx);

    public static class FountainMusic extends ClientBoundSoundPackets
    {
        public FountainMusic(BlockPos fountainPos, boolean stop)
        {
            super(fountainPos, stop);
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
                SoundAccess.playFountainMusic(fountainPos, stop);
            });
            return true;
        }
    }

    public static class FountainDarkWind extends ClientBoundSoundPackets
    {
        public FountainDarkWind(BlockPos fountainPos, boolean stop)
        {
            super(fountainPos, stop);
        }
        public FountainDarkWind(FriendlyByteBuf buffer)
        {
            super(buffer);
        }

        @Override
        public boolean handle(Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
            {
                SoundAccess.playFountainDarkWind(fountainPos, stop);
            });
            return true;
        }
    }

    public static class FountainLightWind extends ClientBoundSoundPackets
    {
        public FountainLightWind(BlockPos fountainPos, boolean stop)
        {
            super(fountainPos, stop);
        }
        public FountainLightWind(FriendlyByteBuf buffer)
        {
            super(buffer);
        }

        @Override
        public boolean handle(Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
                {
                    SoundAccess.playFountainLightWind(fountainPos, stop);
                });
            return true;
        }
    }
}
