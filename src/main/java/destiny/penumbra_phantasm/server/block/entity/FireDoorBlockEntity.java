package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.block.FireDoorBlock;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FireDoorBlockEntity extends BlockEntity {
    public static final String DOOR_DELAY = "DoorDelay";
    public static final String OPEN_COUNT = "OpenCount";

    public int doorDelay = -1;
    private int openCount = 0;

    public FireDoorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.FIRE_DOOR_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FireDoorBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.openCount > 0) {
            if (!state.getValue(BlockStateProperties.OPEN)) {
                be.setDoorState(level, pos, true);
            }
        }
        else if (be.doorDelay <= 0 && state.getValue(BlockStateProperties.OPEN)) {
            be.setDoorState(level, pos, false);
        }

        if (be.doorDelay > 0) {
            be.doorDelay--;
            if (be.doorDelay == 0) {
                be.setDoorState(level, pos, false);
                be.doorDelay = -1;
            }
            be.setChanged();
        }
    }

    public void openDoor(Level level, BlockPos lowerPos) {
        setDoorState(level, lowerPos, true);
        this.doorDelay = -1;
        setChanged();
    }

    public void closeDoor(Level level, BlockPos lowerPos) {
        setDoorState(level, lowerPos, false);
        setChanged();
    }

    private void setDoorState(Level level, BlockPos lowerPos, boolean open) {
        BlockPos upperPos = lowerPos.above();
        BlockState lower = level.getBlockState(lowerPos);
        BlockState upper = level.getBlockState(upperPos);

        if (!(lower.getBlock() instanceof FireDoorBlock)) return;
        if (lower.getValue(BlockStateProperties.OPEN) == open) return;

        level.playSound(null, lowerPos, open ? SoundRegistry.FIRE_DOOR_OPEN.get() : SoundRegistry.FIRE_DOOR_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);

        int flags = Block.UPDATE_IMMEDIATE;

        if (upper.getBlock() instanceof FireDoorBlock) {
            level.setBlockAndUpdate(upperPos, upper.setValue(BlockStateProperties.OPEN, open));
        }

        level.setBlockAndUpdate(lowerPos, lower.setValue(BlockStateProperties.OPEN, open));

        level.updateNeighborsAt(lowerPos, lower.getBlock());
    }

    public void incrementOpenCount() {
        this.openCount++;
        setChanged();
    }

    public void decrementOpenCount() {
        this.openCount = Math.max(0, this.openCount - 1);
        setChanged();
    }

    public int getOpenCount() { return openCount; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt(DOOR_DELAY, doorDelay);
        tag.putInt(OPEN_COUNT, openCount);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        this.doorDelay = tag.getInt(DOOR_DELAY);
        this.openCount = tag.getInt(OPEN_COUNT);
        super.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(DOOR_DELAY, doorDelay);
        tag.putInt(OPEN_COUNT, openCount);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.doorDelay = tag.getInt(DOOR_DELAY);
        this.openCount = tag.getInt(OPEN_COUNT);
    }
}