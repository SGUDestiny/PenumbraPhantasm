package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.client.network.ClientBoundFireDoorSyncPacket;
import destiny.penumbra_phantasm.server.block.FireDoorBlock;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class FireDoorBlockEntity extends BlockEntity implements Nameable {
    public static final String DOOR_DELAY = "DoorDelay";
    public static final String OPEN_COUNT = "OpenCount";
    public static final String CUSTOM_NAME = "CustomName";

    public int doorDelay = -1;
    private int openCount = 0;
    @Nullable
    private Component customName;

    public boolean droppedByPlayer = false;

    public FireDoorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.FIRE_DOOR_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FireDoorBlockEntity fireDoor) {
        if (level.isClientSide()) return;

        if (fireDoor.openCount > 0) {
            if (!state.getValue(BlockStateProperties.OPEN)) {
                fireDoor.setDoorState(level, pos, true);
            }
            if (fireDoor.doorDelay != -1) {
                fireDoor.doorDelay = -1;
            }
        } else if (fireDoor.doorDelay <= 0 && state.getValue(BlockStateProperties.OPEN)) {
            fireDoor.setDoorState(level, pos, false);
        }

        if (fireDoor.doorDelay > 0) {
            fireDoor.doorDelay--;
            if (fireDoor.doorDelay == 0) {
                fireDoor.setDoorState(level, pos, false);
                fireDoor.doorDelay = -1;
            }
            fireDoor.setChanged();
        }
    }

    public void setDoorState(Level level, BlockPos lowerPos, boolean open) {
        BlockPos upperPos = lowerPos.above();
        BlockState lower = level.getBlockState(lowerPos);
        BlockState upper = level.getBlockState(upperPos);

        if (!(lower.getBlock() instanceof FireDoorBlock)) return;
        if (lower.getValue(BlockStateProperties.OPEN) == open) return;

        level.playSound(null, lowerPos, open ? SoundRegistry.FIRE_DOOR_OPEN.get() : SoundRegistry.FIRE_DOOR_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);

        level.setBlock(lowerPos, lower.setValue(BlockStateProperties.OPEN, open), Block.UPDATE_ALL);

        level.setBlock(upperPos, upper.setValue(BlockStateProperties.OPEN, open), Block.UPDATE_NONE);

        if (!level.isClientSide) {
            ((ServerLevel) level).getServer().execute(() -> {
                ClientBoundFireDoorSyncPacket packet = new ClientBoundFireDoorSyncPacket(level.dimension(), upperPos, open);
                for (ServerPlayer player : ((ServerLevel) level).players()) {
                    if (player.distanceToSqr(lowerPos.getX() + 0.5, lowerPos.getY(), lowerPos.getZ() + 0.5) < 128 * 128) {
                        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
                    }
                }
            });
        }

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

    public int getOpenCount() {
        return openCount;
    }

    @Override
    public Component getName() {
        return customName != null ? customName : getDefaultName();
    }

    @Nullable
    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(@Nullable Component name) {
        this.customName = name;
        setChanged();
    }

    public boolean hasCustomName() {
        return customName != null;
    }

    protected Component getDefaultName() {
        return Component.translatable("block.penumbra_phantasm.fire_door");
    }

    @Override
    public void saveToItem(ItemStack stack)
    {
        BlockItem.setBlockEntityData(stack, this.getType(), saveItemData());
    }

    protected CompoundTag saveItemData()
    {
        CompoundTag tag = this.saveWithoutMetadata();
        tag.remove(DOOR_DELAY);
        tag.remove(OPEN_COUNT);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt(DOOR_DELAY, doorDelay);
        tag.putInt(OPEN_COUNT, openCount);
        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        this.doorDelay = tag.getInt(DOOR_DELAY);
        this.openCount = tag.getInt(OPEN_COUNT);
        if (tag.contains(CUSTOM_NAME)) {
            this.customName = Component.Serializer.fromJson(tag.getString(CUSTOM_NAME));
        }
        super.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(DOOR_DELAY, doorDelay);
        tag.putInt(OPEN_COUNT, openCount);
        if (customName != null) {
            tag.putString(CUSTOM_NAME, Component.Serializer.toJson(customName));
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.doorDelay = tag.getInt(DOOR_DELAY);
        this.openCount = tag.getInt(OPEN_COUNT);
        if (tag.contains(CUSTOM_NAME)) {
            this.customName = Component.Serializer.fromJson(tag.getString(CUSTOM_NAME));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level == null || level.isClientSide()) return;

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof FireDoorBlock)) return;

        boolean isOpen = state.getValue(BlockStateProperties.OPEN);

        if (isOpen && openCount == 0 && doorDelay <= 0) {
            setDoorState(level, worldPosition, false);
        }

        else if (!isOpen && openCount > 0) {
            openCount = 0;
            setChanged();
        }
    }
}