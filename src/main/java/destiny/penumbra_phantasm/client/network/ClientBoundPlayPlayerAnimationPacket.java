package destiny.penumbra_phantasm.client.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundPlayPlayerAnimationPacket {
    private final int entityId;
    private final ResourceLocation animationId;

    public ClientBoundPlayPlayerAnimationPacket(int entityId, ResourceLocation animationId) {
        this.entityId = entityId;
        this.animationId = animationId;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(animationId);
    }

    public static ClientBoundPlayPlayerAnimationPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        ResourceLocation animationId = buffer.readResourceLocation();
        return new ClientBoundPlayPlayerAnimationPacket(entityId, animationId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBoundPacketHandler.playPlayerAnimation(entityId, animationId));
        return true;
    }
}

