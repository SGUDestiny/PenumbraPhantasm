package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.block.FireDoorBlock;
import destiny.penumbra_phantasm.server.block.entity.FireDoorBlockEntity;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
                if (originLevel.getBlockEntity(originPos) instanceof FireDoorBlockEntity originBE) {
                    originBE.setDoorState(originLevel, originPos, false);
                    originBE.decrementOpenCount();
                }
            }

            ServerLevel targetLevel = player.getServer().getLevel(darkWorld);
            if (targetLevel == null) return;

            double x = doorPos.getX() + 0.5;
            double y = doorPos.getY();
            double z = doorPos.getZ() + 0.5;

            player.teleportTo(targetLevel, x, y, z, facingAngle, player.getXRot());

            targetLevel.getServer().tell(new TickTask(targetLevel.getServer().getTickCount() + 2, () -> {
                if (targetLevel.isLoaded(doorPos)) {
                    BlockState destState = targetLevel.getBlockState(doorPos);

                    if (destState.getBlock() instanceof FireDoorBlock && !destState.getValue(BlockStateProperties.OPEN)) {
                        if (targetLevel.getBlockEntity(doorPos) instanceof FireDoorBlockEntity fireDoor) {
                            fireDoor.setDoorState(targetLevel, doorPos, true);
                            fireDoor.doorDelay = 40;
                            fireDoor.setChanged();
                        }
                    }
                }
            }));
        });

        return true;
    }
}