package destiny.penumbra_phantasm.server.transformations.inventory;

import destiny.penumbra_phantasm.server.compatability.curios.CuriosCompat;
import destiny.penumbra_phantasm.server.event.custom.RegisterStorageHandlersEvent;
import destiny.penumbra_phantasm.server.transformations.inventory.handlers.IStorageHandler;
import destiny.penumbra_phantasm.server.transformations.inventory.handlers.VanillaInventoryHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;

public class StorageManager
{
	private static final List<IStorageHandler> HANDLERS = new ArrayList<>();

	public static void init()
	{
		HANDLERS.add(new VanillaInventoryHandler());
		if(ModList.get().isLoaded("curios"))
			HANDLERS.add(CuriosCompat.createCuriosHandler());

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.post(new RegisterStorageHandlersEvent(HANDLERS));
	}

	public static List<IStorageHandler> getHandlers()
	{
		return HANDLERS;
	}
}
