package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import static destiny.penumbra_phantasm.server.block.DarknessBlock.isDoorVisuallyOpenFromSide;

public class DarknessBlockEntity extends BlockEntity {
    public static final String FOUNTAIN_POS = "fountainPos";

    public BlockPos fountainPos = null;
    private long removalEarliestGameTime = -1L;

    public DarknessBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.DARKNESS_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DarknessBlockEntity darkness) {
        if (level.isClientSide()) {
            if (level.random.nextDouble() >= 0.8) {
                spawnParticles(level, pos, level.getRandom());
            }
        } else {
            if (darkness.fountainPos == null) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }
            if (darkness.removalEarliestGameTime < 0L) {
                darkness.removalEarliestGameTime = level.getGameTime() + 200L;
            }
            if (level.getGameTime() < darkness.removalEarliestGameTime) {
                //Get light fountain capability
                DarkFountainCapability fountainCapability = null;
                LazyOptional<DarkFountainCapability> lazyOptional = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
                if (lazyOptional.isPresent() && lazyOptional.resolve().isPresent())
                    fountainCapability = lazyOptional.resolve().get();

                if (fountainCapability == null) {
                    return;
                }

                if (fountainCapability.darkFountains.get(darkness.fountainPos) != null) {
                    return;
                }
                return;
            }

            if (level.random.nextDouble() >= 0.2) {
                spawnParticles(level, pos, level.getRandom());
            }

            if (level.random.nextDouble() <= 0.8) {
                return;
            }
            DarkFountainCapability fountainCapability = null;
            LazyOptional<DarkFountainCapability> lazyOptional = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
            if (lazyOptional.isPresent() && lazyOptional.resolve().isPresent())
                fountainCapability = lazyOptional.resolve().get();

            if (fountainCapability == null) {
                return;
            }

            if (fountainCapability.darkFountains.get(darkness.fountainPos) != null) {
                return;
            }

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    public static void spawnParticles(Level level, BlockPos pos, RandomSource random) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighbor = level.getBlockState(neighborPos);

            boolean shouldSpawn = false;
            Direction particleDirection = dir;

            if (neighbor.is(Blocks.AIR) || neighbor.is(Blocks.CAVE_AIR) || neighbor.is(Blocks.VOID_AIR)) {
                shouldSpawn = true;
            } else if (neighbor.getBlock() instanceof DoorBlock) {
                Direction fromDoorToRoom = dir.getOpposite();
                if (isDoorVisuallyOpenFromSide(level, neighborPos, neighbor, fromDoorToRoom)) {
                    BlockPos beyondDoor = neighborPos.relative(dir);
                    if (!(level.getBlockState(beyondDoor).getBlock() instanceof DarknessBlock)) {
                        shouldSpawn = true;
                        particleDirection = dir;
                    }
                }
            }

            if (!shouldSpawn) continue;

            double px = pos.getX() + 0.5 + dir.getStepX() * 0.4;
            double py = pos.getY() + 0.5 + dir.getStepY() * 0.4;
            double pz = pos.getZ() + 0.5 + dir.getStepZ() * 0.4;

            double baseSpeed = 0.15;
            double vx = particleDirection.getStepX() * baseSpeed + (random.nextDouble() - 0.5) * 0.02;
            double vy = particleDirection.getStepY() * baseSpeed + random.nextDouble() * 0.02;
            double vz = particleDirection.getStepZ() * baseSpeed + (random.nextDouble() - 0.5) * 0.02;

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), px, py, pz, 1, vx, vy, vz, 0);
            } else {
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), px, py, pz, vx, vy, vz);
            }
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.fountainPos = NbtUtils.readBlockPos(tag.getCompound(FOUNTAIN_POS));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (fountainPos != null) {
            tag.put(FOUNTAIN_POS, NbtUtils.writeBlockPos(fountainPos));
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
