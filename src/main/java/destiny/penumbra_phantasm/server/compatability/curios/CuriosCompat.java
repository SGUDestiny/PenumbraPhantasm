package destiny.penumbra_phantasm.server.compatability.curios;

import destiny.penumbra_phantasm.server.transformations.inventory.handlers.IStorageHandler;

public class CuriosCompat
{
	public static IStorageHandler createCuriosHandler()
	{
		return new CuriosStorageHandler();
	}
}
