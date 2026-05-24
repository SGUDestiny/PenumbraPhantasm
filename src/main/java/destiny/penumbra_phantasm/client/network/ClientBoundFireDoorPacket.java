package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.fountain.FireDoor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ClientBoundFireDoorPacket(List<FireDoor> fireDoors, ResourceKey<Level> originDarkWorld, BlockPos originPos) {
    public void encode(FriendlyByteBuf buffer) {
        CompoundTag wrapper = new CompoundTag();
        ListTag list = new ListTag();

        for (FireDoor door : fireDoors) {
            list.add(door.saveDoor());
        }

        wrapper.put("doors", list);
        buffer.writeNbt(wrapper);

        buffer.writeResourceKey(originDarkWorld);
        buffer.writeBlockPos(originPos);
    }

    public static ClientBoundFireDoorPacket decode(FriendlyByteBuf buffer) {
        CompoundTag wrapper = buffer.readNbt();
        ListTag list = wrapper.getList("doors", Tag.TAG_COMPOUND);
        List<FireDoor> doors = new ArrayList<>();

        for (Tag tag : list) {
            doors.add(FireDoor.loadDoor((CompoundTag) tag));
        }

        ResourceKey<Level> originDarkWorld = buffer.readResourceKey(Registries.DIMENSION);
        BlockPos originPos = buffer.readBlockPos();

        return new ClientBoundFireDoorPacket(doors, originDarkWorld, originPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientBoundPacketHandler.openFireDoorScreen(fireDoors, originDarkWorld, originPos);
        });
        return true;
    }
}