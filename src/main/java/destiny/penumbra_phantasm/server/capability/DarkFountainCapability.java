package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkRoom;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class DarkFountainCapability implements INBTSerializable<CompoundTag> {
    private static final String DARK_FOUNTAINS = "dark_fountains";
    private static final String PERSISTENT_DARK_WORLD_SITES = "persistent_dark_world_sites";

    public HashMap<BlockPos, DarkFountain> darkFountains = new HashMap<>();
    private final List<PersistentDarkWorldSite> persistentDarkWorldSites = new ArrayList<>();

    public static boolean roomIntersectsActiveFountain(DarkFountainCapability cap, Iterable<BlockPos> candidateRoom) {
        for (DarkFountain fountain : cap.darkFountains.values()) {
            HashSet<Long> occupied = new HashSet<>();
            for (DarkRoom room : fountain.rooms) {
                for (BlockPos p : room.getPositions()) {
                    occupied.add(p.asLong());
                }
            }
            for (BlockPos p : candidateRoom) {
                if (occupied.contains(p.asLong())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Optional<PersistentDarkWorldSite> findMatchingPersistentSite(Iterable<BlockPos> roomPositions, ResourceLocation typeId) {
        for (PersistentDarkWorldSite site : persistentDarkWorldSites) {
            if (!site.worldTypeId.equals(typeId)) {
                continue;
            }
            for (BlockPos p : roomPositions) {
                if (site.roomPositionsPacked.contains(p.asLong())) {
                    return Optional.of(site);
                }
            }
        }
        return Optional.empty();
    }

    public void registerPersistentSite(Collection<BlockPos> roomPositions, ResourceLocation typeId, ResourceKey<Level> dimensionKey) {
        HashSet<Long> packed = new HashSet<>();
        for (BlockPos p : roomPositions) {
            packed.add(p.asLong());
        }
        persistentDarkWorldSites.add(new PersistentDarkWorldSite(typeId, packed, dimensionKey));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.put(DARK_FOUNTAINS, serializeDarkFountains());
        tag.put(PERSISTENT_DARK_WORLD_SITES, serializePersistentSites());

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

    private ListTag serializePersistentSites() {
        ListTag list = new ListTag();
        for (PersistentDarkWorldSite site : persistentDarkWorldSites) {
            CompoundTag siteTag = new CompoundTag();
            siteTag.putString("type", site.worldTypeId.toString());
            siteTag.putString("dimension", site.dimensionKey.location().toString());
            long[] packed = site.roomPositionsPacked.stream().mapToLong(Long::longValue).toArray();
            siteTag.putLongArray("positions", packed);
            list.add(siteTag);
        }
        return list;
    }

    public void addDarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension,
                                BlockPos destinationPos, ResourceKey<Level> destinationDimension,
                                int openingTick, int frameTick, int frame, int frameOptimized, HashSet<UUID> teleportedEntities, List<Integer> shockwaveTickers, int sealingTick, int sealingFrameTick, float sealingFrameTickProgress) {
        this.darkFountains.put(fountainPos, new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, openingTick, frameTick, frame, frameOptimized, teleportedEntities, shockwaveTickers, sealingTick, sealingFrameTick, sealingFrameTickProgress));
    }

    public void removeDarkFountain(Level level, BlockPos fountainPos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap ->
                    cap.darkFountains.remove(fountainPos));
        }
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        deserializeDarkFountains(tag.getCompound(DARK_FOUNTAINS));
        persistentDarkWorldSites.clear();
        if (tag.contains(PERSISTENT_DARK_WORLD_SITES, Tag.TAG_LIST)) {
            deserializePersistentSites(tag.getList(PERSISTENT_DARK_WORLD_SITES, Tag.TAG_COMPOUND));
        }
    }

    private void deserializeDarkFountains(CompoundTag tag) {
        ListTag fountainTags = tag.getList("fountains", ListTag.TAG_COMPOUND);
        for (Tag nbt : fountainTags) {
            CompoundTag fountainTag = ((CompoundTag) nbt);
            DarkFountain fountain = DarkFountain.load(fountainTag);

            this.darkFountains.put(fountain.fountainPos, fountain);
        }
    }

    private void deserializePersistentSites(ListTag list) {
        for (Tag nbt : list) {
            CompoundTag siteTag = (CompoundTag) nbt;
            ResourceLocation typeId = new ResourceLocation(siteTag.getString("type"));
            ResourceLocation dimLoc = new ResourceLocation(siteTag.getString("dimension"));
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);
            long[] packedArr = siteTag.getLongArray("positions");
            HashSet<Long> packed = new HashSet<>();
            for (long v : packedArr) {
                packed.add(v);
            }
            persistentDarkWorldSites.add(new PersistentDarkWorldSite(typeId, packed, dimKey));
        }
    }

    public static final class PersistentDarkWorldSite {
        public final ResourceLocation worldTypeId;
        public final HashSet<Long> roomPositionsPacked;
        public ResourceKey<Level> dimensionKey;

        public PersistentDarkWorldSite(ResourceLocation worldTypeId, HashSet<Long> roomPositionsPacked, ResourceKey<Level> dimensionKey) {
            this.worldTypeId = worldTypeId;
            this.roomPositionsPacked = roomPositionsPacked;
            this.dimensionKey = dimensionKey;
        }
    }
}
