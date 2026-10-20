package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.capability.AnchorOfLightCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static destiny.penumbra_phantasm.server.capability.AnchorOfLightCapability.ANCHORS;

public record ClientBoundAnchorOfLightSyncPacket(Set<BlockPos> anchors, ResourceKey<Level> levelKey) {
    public void encode(FriendlyByteBuf buffer) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();

        for (BlockPos pos : anchors) {
            listTag.add(NbtUtils.writeBlockPos(pos));
        }

        tag.put(ANCHORS, listTag);
        buffer.writeNbt(tag);
        buffer.writeResourceKey(levelKey);
    }

    public static ClientBoundAnchorOfLightSyncPacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        ListTag listTag = tag.getList(ANCHORS, Tag.TAG_COMPOUND);
        Set<BlockPos> anchors = new HashSet<>();

        for (Tag tag1 : listTag) {
            anchors.add(NbtUtils.readBlockPos((CompoundTag) tag1));
        }

        ResourceKey<Level> levelKey = buffer.readResourceKey(Registries.DIMENSION);

        return new ClientBoundAnchorOfLightSyncPacket(anchors, levelKey);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.level == null || !minecraft.level.dimension().equals(levelKey)) return;

            AnchorOfLightCapability capability = minecraft.level.getCapability(CapabilityRegistry.ANCHOR_OF_LIGHT).resolve().orElse(null);

            if (capability == null) return;

            capability.sync(capability);
        });
        return true;
    }
}
