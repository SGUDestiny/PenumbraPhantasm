package destiny.penumbra_phantasm.server.event.custom;

import destiny.penumbra_phantasm.server.transformations.inventory.handlers.IStorageHandler;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

/**
 * Allows users to register custom {@link IStorageHandler Storage Handlers}.
 * A Storage Handler is an object that handles the players inventory when swapping for Inventory Separation in Dark Worlds.
 *
 * <p>This event is not {@linkplain Cancelable cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus}
 */
public class RegisterStorageHandlersEvent extends Event implements IModBusEvent
{
	private final List<IStorageHandler> registryList;

	public RegisterStorageHandlersEvent(List<IStorageHandler> registryList) {
		this.registryList = registryList;
	}
	/**
	 * Adds the Storage Handler to the registry.
	 */
	public void register(IStorageHandler handler) {
		this.registryList.add(handler);
	}
}

