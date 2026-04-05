package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class DarknessBlockEntity extends BlockEntity {
    public static final String FOUNTAIN_POS = "fountainPos";

    public BlockPos fountainPos = null;
    public boolean worldLoginCheck = false;

    public DarknessBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.DARKNESS_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DarknessBlockEntity darkness) {
        if (darkness.fountainPos == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }

        if (!darkness.worldLoginCheck) {
            darkness.worldLoginCheck = true;

            //Get light fountain capability
            DarkFountainCapability fountainCapability = null;
            LazyOptional<DarkFountainCapability> lazyOptional = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
            if(lazyOptional.isPresent() && lazyOptional.resolve().isPresent())
                fountainCapability = lazyOptional.resolve().get();

            if (fountainCapability == null){
                return;
            }

            DarkFountain darkFountain = fountainCapability.darkFountains.get(darkness.fountainPos);

            if (darkFountain == null) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
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
