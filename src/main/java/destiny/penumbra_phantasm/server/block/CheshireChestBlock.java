package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.client.network.ClientBoundParticlePacket;
import destiny.penumbra_phantasm.client.render.menu.CheshireChestMenu;
import destiny.penumbra_phantasm.server.block.entity.CheshireChestBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class CheshireChestBlock extends BaseEntityBlock {
    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CheshireChestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!DarkWorldUtil.isDarkWorld(pLevel)) return;

        Vec3 position = pPos.getCenter();

        PacketHandlerRegistry.INSTANCE.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(position.x, position.y, position.z, 32.0, pLevel.dimension())),
                new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.FRIEND_DISAPPEAR.get()), position.x, position.y, position.z, 0, 0, 0, 1)
        );
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!DarkWorldUtil.isDarkWorld(level)) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.cheshire_chest_not_in_dark_world"), true);

            return InteractionResult.SUCCESS;
        }

        player.getCapability(CapabilityRegistry.CHESHIRE_CHEST).ifPresent(cheshireInv -> {
            MenuProvider provider = new SimpleMenuProvider(
                    (windowId, playerInventory, p) -> new CheshireChestMenu(windowId, playerInventory, cheshireInv, pos, p),
                    Component.translatable("container.cheshire_chest"));
            NetworkHooks.openScreen((ServerPlayer) player, provider, buf -> buf.writeBlockPos(pos));
        });

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CheshireChestBlockEntity chestBE) {
            chestBE.startOpen(player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CheshireChestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, BlockEntityRegistry.CHESHIRE_CHEST_BLOCK_ENTITY.get(), CheshireChestBlockEntity::clientTick) : null;
    }
}