package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.SoundTypeRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class LuciramicButtonBlock extends ButtonBlock {
    public static final BlockSetType LUCIRAMIC_BLOCKSET = new BlockSetType("luciramic", true, SoundTypeRegistry.LUCIRAMIC, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

    public LuciramicButtonBlock(Properties pProperties, int pTicksToStayPressed, boolean pArrowsCanPress) {
        super(pProperties, LUCIRAMIC_BLOCKSET, pTicksToStayPressed, pArrowsCanPress);
    }
}
