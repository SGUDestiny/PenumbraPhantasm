package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundParticlePacket;
import destiny.penumbra_phantasm.client.network.ClientBoundPlayPlayerAnimationPacket;
import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.block.entity.HearthBlockEntity;
import destiny.penumbra_phantasm.server.item.HearthSoulItem;
import destiny.penumbra_phantasm.server.item.SoulHearthItem;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static destiny.penumbra_phantasm.server.item.SoulHearthItem.SOUL_TYPE;
import static net.minecraft.world.level.block.CampfireBlock.LIT;

public class HearthBlock extends BaseEntityBlock {
    public static final IntegerProperty SOUL_TYPE_HEARTH = IntegerProperty.create("soul_type", 0, 7);

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
        this.registerDefaultState(this.defaultBlockState().setValue(SOUL_TYPE_HEARTH, 0));
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        ResourceLocation resourcelocation = this.getLootTable();
        if (resourcelocation == BuiltInLootTables.EMPTY) {
            return Collections.emptyList();
        } else {
            LootParams lootparams = pParams.withParameter(LootContextParams.BLOCK_STATE, pState).create(LootContextParamSets.BLOCK);
            ServerLevel serverlevel = lootparams.getLevel();
            LootTable loottable = serverlevel.getServer().getLootData().getLootTable(resourcelocation);

            List<ItemStack> list = loottable.getRandomItems(lootparams);
            for (ItemStack stack : list) {
                if (stack.getItem() instanceof SoulHearthItem) {
                    stack.getOrCreateTag().putInt(SOUL_TYPE, pState.getValue(SOUL_TYPE_HEARTH));
                }
            }

            return list;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(SOUL_TYPE_HEARTH);
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

        if (pLevel.getBiome(pPos).is(Biomes.SOUL_SAND_VALLEY) && pLevel.getBlockState(pPos.below()).is(Blocks.SOUL_CAMPFIRE)) {
            ItemStack stack = pPlayer.getItemInHand(pHand);

            if (stack.getItem() instanceof HearthSoulItem && pState.getValue(SOUL_TYPE_HEARTH) == 0) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(SOUL_TYPE_HEARTH, stack.getTag().getInt(SOUL_TYPE)));

                pLevel.playSound(null, pPos, SoundRegistry.SOUL_GRAB.get(), SoundSource.BLOCKS, 0.5f, 1);

                stack.shrink(1);

                int particleAmount = pLevel.random.nextInt(3, 6);

                for (int i = 0; i < particleAmount; i++) {
                    double x = pPos.getX() + (pLevel.random.nextDouble() - 0.5);
                    double y = pPos.getY() + 1.5;
                    double z = pPos.getZ() + (pLevel.random.nextDouble() - 0.5);

                    PacketHandlerRegistry.INSTANCE.send(
                            PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 32.0, pLevel.dimension())),
                            new ClientBoundParticlePacket(
                                    ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypes.SOUL),
                                    x, y, z, 0, 0, 0, 1
                            )
                    );
                }

                pLevel.setBlockAndUpdate(pPos.below(), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(LIT, false));
                pLevel.playSound(null, pPos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1f, 1f);

                TriggerCriterions.SOUL_HEARTH.trigger((ServerPlayer) pPlayer);

                return InteractionResult.SUCCESS;
            }

            if (hearthBlockEntity.playerUuid == null) {
                boolean hasSoulItem = false;
                for (ItemStack stack1 : pPlayer.getInventory().items) {
                    if (!stack1.isEmpty() && stack1.getItem() instanceof HearthSoulItem) {
                        hasSoulItem = true;
                    }
                }

                if (!hasSoulItem) {
                    hearthBlockEntity.soulRipTicker = 0;
                    hearthBlockEntity.playerUuid = pPlayer.getUUID();

                    PacketHandlerRegistry.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> pPlayer),
                            new ClientBoundPlayPlayerAnimationPacket(pPlayer.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "soul_rip"))
                    );
                }

                return InteractionResult.SUCCESS;
            }
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
