package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkRoom;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class DarkFountainCapability implements INBTSerializable<CompoundTag> {

    private static final String DARK_FOUNTAINS = "dark_fountains";

    public HashMap<BlockPos, DarkFountain> darkFountains = new HashMap<>();

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.put(DARK_FOUNTAINS, serializeDarkFountains());

        return tag;
    }

    private CompoundTag serializeDarkFountains() {
        CompoundTag objectsTag = new CompoundTag();
        ListTag fountainTag = new ListTag();

        this.darkFountains.forEach((pos, fountain) -> {
            fountainTag.add(fountain.save());
        });
        objectsTag.put("fountains", fountainTag);

        return objectsTag;
    }

    public void addDarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension,
                                BlockPos destinationPos, ResourceKey<Level> destinationDimension,
                                int openingTick, int frameTimer, int frame, int frameOptimized, HashSet<UUID> teleportedEntities, List<Integer> shockwaveTickers) {
        this.darkFountains.put(fountainPos, new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, openingTick, frameTimer, frame, frameOptimized, teleportedEntities, shockwaveTickers));
    }

    public void removeDarkFountain(Level level, BlockPos fountainPos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                this.darkFountains.remove(fountainPos);
            });
        }
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        deserializeDarkFountains(tag.getCompound(DARK_FOUNTAINS));
    }

    private void deserializeDarkFountains(CompoundTag tag)
    {
        ListTag fountainTags = tag.getList("fountains", ListTag.TAG_COMPOUND);
        for(Tag nbt : fountainTags)
        {
            CompoundTag fountainTag = ((CompoundTag) nbt);
            DarkFountain fountain = DarkFountain.load(fountainTag);

            this.darkFountains.put(fountain.fountainPos, fountain);
        }
    }
}
