package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.registry.SoundTypeRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class CliffrockButtonBlock extends ButtonBlock {
    public static final BlockSetType CLIFFROCK_BLOCKSET = new BlockSetType("cliffrock", true, SoundTypeRegistry.CLIFF, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

    public CliffrockButtonBlock(Properties pProperties, int pTicksToStayPressed, boolean pArrowsCanPress) {
        super(pProperties, CLIFFROCK_BLOCKSET, pTicksToStayPressed, pArrowsCanPress);
    }

    protected SoundEvent getSound(boolean pIsOn) {
        return pIsOn ? SoundRegistry.CLIFFROCK_PRESS.get() : SoundRegistry.CLIFFROCK_UNPRESS.get();
    }
}
