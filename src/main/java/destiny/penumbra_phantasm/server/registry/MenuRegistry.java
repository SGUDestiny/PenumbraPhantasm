package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.menu.CheshireChestMenu;
import destiny.penumbra_phantasm.client.render.menu.UmbrastoneFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PenumbraPhantasm.MODID);

    public static final RegistryObject<MenuType<UmbrastoneFurnaceMenu>> UMBRASTONE_FURNACE_MENU = MENUS.register("umbrastone_furnace",
                    () -> IForgeMenuType.create((windowId, inv, data) -> new UmbrastoneFurnaceMenu(windowId, inv)));
    public static final RegistryObject<MenuType<CheshireChestMenu>> CHESHIRE_CHEST_MENU =
            MENUS.register("cheshire_chest",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        return new CheshireChestMenu(windowId, inv, pos);
                    }));
}