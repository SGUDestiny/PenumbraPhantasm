package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;

public class GenericPressurePlateBlock extends PressurePlateBlock {
    public final SoundEvent pressSound;
    public final SoundEvent unpressSound;

    public GenericPressurePlateBlock(Sensitivity pSensitivity, Properties pProperties, SoundType soundType, SoundEvent pressSound, SoundEvent unpressSound) {
        super(pSensitivity, pProperties, new BlockSetType("generic_pressure_plate", true, soundType, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
        this.pressSound = pressSound;
        this.unpressSound = unpressSound;
    }

    @Override
    protected void checkPressed(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, BlockState pState, int pCurrentSignal) {
        int signalStrength = this.getSignalStrength(pLevel, pPos);
        boolean wasPressed = pCurrentSignal > 0;
        boolean pressed = signalStrength > 0;
        if (pCurrentSignal != signalStrength) {
            BlockState newState = this.setSignalForState(pState, signalStrength);
            pLevel.setBlock(pPos, newState, 2);
            this.updateNeighbours(pLevel, pPos);
            pLevel.setBlocksDirty(pPos, pState, newState);
        }

        if (!pressed && wasPressed) {
            pLevel.playSound(null, pPos, unpressSound, SoundSource.BLOCKS, 0.5F, 1.0F);
            pLevel.gameEvent(pEntity, GameEvent.BLOCK_DEACTIVATE, pPos);
        } else if (pressed && !wasPressed) {
            pLevel.playSound(null, pPos, pressSound, SoundSource.BLOCKS, 0.5F, 1.0F);
            pLevel.gameEvent(pEntity, GameEvent.BLOCK_ACTIVATE, pPos);
        }

        if (pressed) {
            pLevel.scheduleTick(pPos, this, this.getPressedTime());
        }
    }
}