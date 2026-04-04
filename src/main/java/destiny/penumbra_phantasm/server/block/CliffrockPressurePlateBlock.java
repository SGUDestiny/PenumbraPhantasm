package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.registry.SoundTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;

public class CliffrockPressurePlateBlock extends PressurePlateBlock {
    public static final BlockSetType CLIFFROCK_BLOCKSET = new BlockSetType("cliffrock", true, SoundTypeRegistry.CLIFF, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

    public CliffrockPressurePlateBlock(Sensitivity pSensitivity, Properties pProperties) {
        super(pSensitivity, pProperties, CLIFFROCK_BLOCKSET);
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!pLevel.isClientSide) {
            int $$4 = this.getSignalForState(pState);
            if ($$4 == 0) {
                checkPressed(pEntity, pLevel, pPos, pState, $$4);
            }

        }
    }

    private void checkPressed(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, BlockState pState, int pCurrentSignal) {
        int $$5 = this.getSignalStrength(pLevel, pPos);
        boolean $$6 = pCurrentSignal > 0;
        boolean $$7 = $$5 > 0;
        if (pCurrentSignal != $$5) {
            BlockState $$8 = this.setSignalForState(pState, $$5);
            pLevel.setBlock(pPos, $$8, 2);
            this.updateNeighbours(pLevel, pPos);
            pLevel.setBlocksDirty(pPos, pState, $$8);
        }

        if (!$$7 && $$6) {
            pLevel.playSound(null, pPos, SoundRegistry.CLIFFROCK_UNPRESS.get(), SoundSource.BLOCKS);
            pLevel.gameEvent(pEntity, GameEvent.BLOCK_DEACTIVATE, pPos);
        } else if ($$7 && !$$6) {
            pLevel.playSound(null, pPos, SoundRegistry.CLIFFROCK_PRESS.get(), SoundSource.BLOCKS);
            pLevel.gameEvent(pEntity, GameEvent.BLOCK_ACTIVATE, pPos);
        }

        if ($$7) {
            pLevel.scheduleTick(new BlockPos(pPos), this, this.getPressedTime());
        }

    }
}
