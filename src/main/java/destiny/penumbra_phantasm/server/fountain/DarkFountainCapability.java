package destiny.penumbra_phantasm.server.fountain;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

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

    public void addDarkFountain(Level level, BlockPos fountainPos, ResourceKey<Level> fountainDimension,
                                BlockPos destinationPos, ResourceKey<Level> destinationDimension,
                                int animationTimer, int frameTimer, int frame, HashSet<UUID> teleportedEntities) {
        this.darkFountains.put(fountainPos, new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, animationTimer, frameTimer, frame, teleportedEntities));
    }

    public void removeDarkFountain(Level level, BlockPos fountainPos)
    {
        this.darkFountains.remove(fountainPos);
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
