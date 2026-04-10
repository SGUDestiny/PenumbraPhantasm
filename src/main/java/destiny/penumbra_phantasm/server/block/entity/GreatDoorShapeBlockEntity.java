package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class GreatDoorShapeBlockEntity extends BlockEntity {
    public static final String GREAT_DOOR_POS = "greatDoorPos";

    public BlockPos greatDoorPos = null;
    private long removalEarliestGameTime = -1L;

    public GreatDoorShapeBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.GREAT_DOOR_SHAPE_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GreatDoorShapeBlockEntity greatDoorShape) {
        if (level.isClientSide()) {
            return;
        }
        if (greatDoorShape.greatDoorPos == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (greatDoorShape.removalEarliestGameTime < 0L) {
            greatDoorShape.removalEarliestGameTime = level.getGameTime() + 200L;
        }
        if (level.getGameTime() < greatDoorShape.removalEarliestGameTime) {
            return;
        }
        if (level.random.nextDouble() <= 0.8) {
            return;
        }
        if (getGreatDoor(level, greatDoorShape) == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    public InteractionResult cycleGreatDoorState(Level level, BlockPos pos, GreatDoorShapeBlockEntity greatDoorShape, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        GreatDoor greatDoor = getGreatDoor(level, greatDoorShape);

        if (greatDoor == null) {
            return InteractionResult.FAIL;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        if (greatDoor.isUnlinkedForAutoBinding()) {
            DarkWorldUtil.tryBindUnlinkedGreatDoor(serverLevel, greatDoor);
        }
        if (!DarkWorldUtil.levelHasDarkFountain(serverLevel)) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.great_door_cant_open_no_fountain"), true);
            return InteractionResult.FAIL;
        }
        if (greatDoor.isDestinationDarkWorld && greatDoor.destinationGreatDoorDimension != null) {
            ServerLevel destLevel = serverLevel.getServer().getLevel(greatDoor.destinationGreatDoorDimension);
            if (destLevel == null || !DarkWorldUtil.levelHasDarkFountain(destLevel)) {
                player.displayClientMessage(Component.translatable("message.penumbra_phantasm.great_door_cant_open_no_fountain_destination"), true);
                return InteractionResult.FAIL;
            }
        }
        if (greatDoor.lightDoorPos == null || greatDoor.lightDoorDimension == null) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.great_door_cant_open_no_light_door"), true);
            return InteractionResult.FAIL;
        }

        if (!GreatDoor.toggleLinkedLightDoor(serverLevel, greatDoor, player, true)) {
            return InteractionResult.FAIL;
        }

        serverLevel.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundRegistry.GREAT_DOOR.get(), SoundSource.BLOCKS, 1f, 1f);

        greatDoor.broadcastSync(serverLevel);
        markUpdated();

        return InteractionResult.SUCCESS;
    }

    public static GreatDoor getGreatDoor(Level level, GreatDoorShapeBlockEntity greatDoorShape) {
        if (greatDoorShape.greatDoorPos == null) {
            return null;
        }

        GreatDoorCapability greatDoorCapability = null;
        LazyOptional<GreatDoorCapability> lazyOptional = level.getCapability(CapabilityRegistry.GREAT_DOOR);
        if(lazyOptional.isPresent() && lazyOptional.resolve().isPresent())
            greatDoorCapability = lazyOptional.resolve().get();

        if (greatDoorCapability == null){
            return null;
        }

        return greatDoorCapability.greatDoors.get(greatDoorShape.greatDoorPos);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.greatDoorPos = NbtUtils.readBlockPos(tag.getCompound(GREAT_DOOR_POS));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (greatDoorPos != null) {
            tag.put(GREAT_DOOR_POS, NbtUtils.writeBlockPos(greatDoorPos));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    private void markUpdated() {
        super.setChanged();
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
