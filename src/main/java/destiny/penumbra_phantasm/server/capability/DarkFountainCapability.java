package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class DarkFountainCapability implements INBTSerializable<CompoundTag> {
    private static final String DARK_FOUNTAINS = "dark_fountains";
    private static final String PERSISTENT_DARK_WORLD_SITES = "persistent_dark_world_sites";

    public HashMap<BlockPos, DarkFountain> darkFountains = new HashMap<>();
    private final List<PersistentDarkWorldSite> persistentDarkWorldSites = new ArrayList<>();

    public static boolean roomContainsActiveFountainAnchor(DarkFountainCapability cap, Iterable<BlockPos> candidateRoom) {
        HashSet<Long> roomCells = new HashSet<>();
        for (BlockPos p : candidateRoom) {
            roomCells.add(p.asLong());
        }
        for (DarkFountain fountain : cap.darkFountains.values()) {
            if (roomCells.contains(fountain.getFountainPos().asLong())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDarkWorldAvailableForNewFountain(ServerLevel darkLevel) {
        return !DarkWorldUtil.levelHasDarkFountain(darkLevel);
    }

    public Optional<PersistentDarkWorldSite> findMatchingPersistentSite(MinecraftServer server, Iterable<BlockPos> roomPositions, ResourceLocation typeId) {
        for (PersistentDarkWorldSite site : persistentDarkWorldSites) {
            if (!site.worldTypeId.equals(typeId)) {
                continue;
            }
            boolean roomContainsSiteAnchor = false;
            for (BlockPos p : roomPositions) {
                if (site.fountainPos.equals(p)) {
                    roomContainsSiteAnchor = true;
                    break;
                }
            }
            if (!roomContainsSiteAnchor) {
                continue;
            }
            if (lightAlreadyLinksToDarkDimension(site.dimensionKey)) {
                continue;
            }
            ServerLevel candidate = server.getLevel(site.dimensionKey);
            if (candidate != null && !isDarkWorldAvailableForNewFountain(candidate)) {
                continue;
            }
            return Optional.of(site);
        }
        return Optional.empty();
    }

    private boolean lightAlreadyLinksToDarkDimension(ResourceKey<Level> darkDimension) {
        for (DarkFountain fountain : darkFountains.values()) {
            if (fountain.getDestinationDimension().equals(darkDimension)) {
                return true;
            }
        }
        return false;
    }

    public void registerPersistentSite(BlockPos fountainPos, ResourceLocation typeId, ResourceKey<Level> dimensionKey) {
        persistentDarkWorldSites.add(new PersistentDarkWorldSite(typeId, fountainPos, dimensionKey));
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
            siteTag.putLong("fountain", site.fountainPos.asLong());
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
            BlockPos fountainPos = BlockPos.of(siteTag.getLong("fountain"));
            persistentDarkWorldSites.add(new PersistentDarkWorldSite(typeId, fountainPos, dimKey));
        }
    }

    public static final class PersistentDarkWorldSite {
        public final ResourceLocation worldTypeId;
        public final BlockPos fountainPos;
        public ResourceKey<Level> dimensionKey;

        public PersistentDarkWorldSite(ResourceLocation worldTypeId, BlockPos fountainPos, ResourceKey<Level> dimensionKey) {
            this.worldTypeId = worldTypeId;
            this.fountainPos = fountainPos.immutable();
            this.dimensionKey = dimensionKey;
        }
    }
}