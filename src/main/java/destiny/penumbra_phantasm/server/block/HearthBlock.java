package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundCancelPlayerAnimationPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundPlayPlayerAnimationPacket;
import destiny.penumbra_phantasm.server.block.entity.DarknessBlockEntity;
import destiny.penumbra_phantasm.server.block.entity.HearthBlockEntity;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.item.HearthSoulItem;
import destiny.penumbra_phantasm.server.item.SoulHearthItem;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import static destiny.penumbra_phantasm.server.item.HearthSoulItem.HEARTH_POS;

public class HearthBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = ModUtil.buildShape(
            Block.box(3.5, 0, 3.5, 12.5, 1, 12.5),
            Block.box(4.5, 1, 4.5, 11.5, 2, 11.5),
            Block.box(3.5, 2, 3.5, 12.5, 4, 12.5),
            Block.box(3.5, 11, 3.5, 12.5, 12, 12.5),
            Block.box(2.5, 12, 2.5, 13.5, 14, 13.5),
            Block.box(4.5, 14, 4.5, 11.5, 17, 11.5),
            Block.box(5.5, 17, 5.5, 10.5, 19, 10.5),
            Block.box(4.5, 4, 4.5, 11.5, 11, 11.5),
            Block.box(4.5, 1, 4.5, 11.5, 2, 11.5),
            Block.box(3.5, 0, 3.5, 12.5, 1, 12.5),
            Block.box(4.5, 14, 4.5, 11.5, 17, 11.5),
            Block.box(5.5, 17, 5.5, 10.5, 19, 10.5),
            Block.box(4.25, 3.75, 4.25, 11.75, 11.25, 11.75)
    );

    public HearthBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) return InteractionResult.FAIL;

        if (pHand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        if (!(pLevel.getBlockEntity(pPos) instanceof HearthBlockEntity hearthBlockEntity)) return InteractionResult.FAIL;

        boolean hasSoulItem = false;

        for (ItemStack stack : pPlayer.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof HearthSoulItem) {
                hasSoulItem = true;
            }
        }

        if (!hasSoulItem && hearthBlockEntity.playerUuid == null) {
            hearthBlockEntity.soulRipTicker = 0;
            hearthBlockEntity.playerUuid = pPlayer.getUUID();

            PacketHandlerRegistry.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> pPlayer),
                    new ClientBoundPlayPlayerAnimationPacket(pPlayer.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "soul_rip"))
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.HEARTH_ENTITY.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityRegistry.HEARTH_ENTITY.get(), HearthBlockEntity::tick);
    }
}
