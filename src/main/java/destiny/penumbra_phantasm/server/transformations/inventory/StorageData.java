package destiny.penumbra_phantasm.server.transformations.inventory;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class StorageData extends SavedData
{
	private static final String FILE_NAME = PenumbraPhantasm.MODID + "-stored_inventories";

	private static final String STORED_INVENTORIES = "stored_inventories";

	private final HashMap<UUID, StoredInventory> storedInventories = new HashMap<>();

	private MinecraftServer server;

	private CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();

		tag.put(STORED_INVENTORIES, serializeInventoryData());

		return tag;
	}

	private CompoundTag serializeInventoryData()
	{
		CompoundTag objectsTag = new CompoundTag();

		this.storedInventories.forEach((uuid, inv) ->
			{
				objectsTag.put(uuid.toString(), inv.save());
			});

		return objectsTag;
	}

	private void deserialize(CompoundTag tag)
	{
		deserializePortalLinkData(tag.getCompound(STORED_INVENTORIES));
	}

	private void deserializePortalLinkData(CompoundTag tag)
	{
		for(String key : tag.getAllKeys())
		{
			this.storedInventories.put(UUID.fromString(key),
					StoredInventory.load(tag.getCompound(key)));
		}
	}

	public StoredInventory createInventory()
	{
		UUID uuid = UUID.randomUUID();
		this.storedInventories.put(uuid, new StoredInventory());

		return this.storedInventories.get(uuid);
	}

	public void putInventory(UUID id, StoredInventory inv)
	{
		this.storedInventories.put(id, inv);
	}

	public StoredInventory getInventory(UUID id)
	{
		return storedInventories.computeIfAbsent(id, (key) -> new StoredInventory());
	}

	public void remove(UUID id)
	{
		storedInventories.remove(id);
	}

	@Override
	public void setDirty()
	{
		super.setDirty();
	}

	public StorageData(MinecraftServer server)
	{
		this.server = server;
	}

	public static StorageData create(MinecraftServer server)
	{
		return new StorageData(server);
	}

	public static StorageData load(MinecraftServer server, CompoundTag tag)
	{
		StorageData data = create(server);

		data.server = server;
		data.deserialize(tag);

		return data;
	}

	public CompoundTag save(CompoundTag tag)
	{
		tag = serialize();

		return tag;
	}

	@Nonnull
	public static StorageData get(Level level)
	{
		if(level.isClientSide())
			throw new RuntimeException("Don't access this client-side!");

		return StorageData.get(level.getServer());
	}

	@Nonnull
	public static StorageData get(MinecraftServer server)
	{
		DimensionDataStorage storage = server.overworld().getDataStorage();

		return storage.computeIfAbsent((tag) -> load(server, tag), () -> create(server), FILE_NAME);
	}
}
