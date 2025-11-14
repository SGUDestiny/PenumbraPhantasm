package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.sounds.SoundAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ClientBoundSoundPackets {
    public final BlockPos pos;
    public final boolean stop;

    public ClientBoundSoundPackets(BlockPos pos, boolean stop)
    {
        this.pos = pos;
        this.stop = stop;
    }

    public ClientBoundSoundPackets(FriendlyByteBuf buffer)
    {
        this(buffer.readBlockPos(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.stop);
    }

    public abstract boolean handle(Supplier<NetworkEvent.Context> ctx);

    public static class FountainMusic extends ClientBoundSoundPackets
    {
        public FountainMusic(BlockPos pos, boolean stop)
        {
            super(pos, stop);
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
                SoundAccess.playFountainMusic(pos, stop);
            });
            return true;
        }
    }

    public static class FountainWind extends ClientBoundSoundPackets
    {
        public FountainWind(BlockPos pos, boolean stop)
        {
            super(pos, stop);
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
                SoundAccess.playFountainWind(pos, stop);
            });
            return true;
        }
    }



    public static class FountainFullMusic extends ClientBoundSoundPackets
    {
        public FountainFullMusic(BlockPos pos, boolean stop)
        {
            super(pos, stop);
        }
        public FountainFullMusic(FriendlyByteBuf buffer)
        {
            super(buffer);
        }

        @Override
        public boolean handle(Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
            {
                SoundAccess.playFountainFullMusic(pos, stop);
            });
            return true;
        }
    }

    public static class FountainFullWind extends ClientBoundSoundPackets
    {
        public FountainFullWind(BlockPos pos, boolean stop)
        {
            super(pos, stop);
        }
        public FountainFullWind(FriendlyByteBuf buffer)
        {
            super(buffer);
        }

        @Override
        public boolean handle(Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
            {
                SoundAccess.playFountainFullWind(pos, stop);
            });
            return true;
        }
    }
}
