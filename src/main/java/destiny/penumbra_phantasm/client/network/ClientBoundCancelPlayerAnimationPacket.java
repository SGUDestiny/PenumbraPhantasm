package destiny.penumbra_phantasm.client.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundCancelPlayerAnimationPacket {
    private final int entityId;
    private final ResourceLocation animationId;

    public ClientBoundCancelPlayerAnimationPacket(int entityId, ResourceLocation animationId) {
        this.entityId = entityId;
        this.animationId = animationId;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(animationId);
    }

    public static ClientBoundCancelPlayerAnimationPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        ResourceLocation animationId = buffer.readResourceLocation();
        return new ClientBoundCancelPlayerAnimationPacket(entityId, animationId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBoundPacketHandler.cancelPlayerAnimation(entityId, animationId));
        return true;
    }
}

