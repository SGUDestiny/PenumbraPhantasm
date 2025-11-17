package destiny.penumbra_phantasm.server.fountain;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
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

        this.darkFountains.forEach((pos, fountain) -> {
            objectsTag.put(NbtUtils.writeBlockPos(pos).toString(), fountain.save());
        });

        return objectsTag;
    }

    public void addDarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension, BlockPos destinationPos, ResourceKey<Level> destinationDimension, int animationTimer, int frameTimer, int frame) {
        this.darkFountains.put(fountainPos, new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, animationTimer, frameTimer, frame));
    }

    public void removeDarkFountain(BlockPos fountainPos) {
        this.darkFountains.remove(fountainPos);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        deserializeDarkFountains(tag.getCompound(DARK_FOUNTAINS));
    }

    private void deserializeDarkFountains(CompoundTag tag) {
        for(String key : tag.getAllKeys()) {
            this.darkFountains.put(NbtUtils.readBlockPos(tag.getCompound(key)), DarkFountain.load(tag.getCompound(key)));
        }
    }
}
