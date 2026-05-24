package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.block.entity.FireDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundFireDoorPacket {
    public BlockPos doorPos;
    public ResourceKey<Level> darkWorld;
    public float facingAngle;
    public ResourceKey<Level> originDarkWorld;
    public BlockPos originPos;

    public ServerBoundFireDoorPacket(ResourceKey<Level> darkWorld, BlockPos doorPos, float facingAngle, ResourceKey<Level> originDarkWorld, BlockPos originPos) {
        this.darkWorld = darkWorld;
        this.doorPos = doorPos;
        this.facingAngle = facingAngle;
        this.originDarkWorld = originDarkWorld;
        this.originPos = originPos;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(darkWorld);
        buffer.writeBlockPos(doorPos);
        buffer.writeFloat(facingAngle);
        buffer.writeResourceKey(originDarkWorld);
        buffer.writeBlockPos(originPos);
    }

    public static ServerBoundFireDoorPacket decode(FriendlyByteBuf buffer) {
        ResourceKey<Level> darkWorld = buffer.readResourceKey(Registries.DIMENSION);
        BlockPos doorPos = buffer.readBlockPos();
        float facingAngle = buffer.readFloat();
        ResourceKey<Level> originDarkWorld = buffer.readResourceKey(Registries.DIMENSION);
        BlockPos originPos = buffer.readBlockPos();

        return new ServerBoundFireDoorPacket(darkWorld, doorPos, facingAngle, originDarkWorld, originPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel originLevel = player.getServer().getLevel(originDarkWorld);
            if (originLevel != null && originLevel.isLoaded(originPos)) {
                if (originLevel.getBlockEntity(originPos) instanceof FireDoorBlockEntity originBe) {
                    originBe.closeDoor(originLevel, originPos);
                    originBe.decrementOpenCount();
                }
            }

            ServerLevel targetLevel = player.getServer().getLevel(darkWorld);
            if (targetLevel != null && targetLevel.isLoaded(doorPos)) {
                if (targetLevel.getBlockEntity(doorPos) instanceof FireDoorBlockEntity targetBe) {
                    targetBe.openDoor(targetLevel, doorPos);
                    targetBe.doorDelay = 20;
                    targetBe.setChanged();
                }
            }

            double x = doorPos.getX() + 0.5;
            double y = doorPos.getY();
            double z = doorPos.getZ() + 0.5;
            float yaw = facingAngle;

            player.teleportTo(targetLevel, x, y, z, yaw, player.getXRot());
        });
        return true;
    }
}