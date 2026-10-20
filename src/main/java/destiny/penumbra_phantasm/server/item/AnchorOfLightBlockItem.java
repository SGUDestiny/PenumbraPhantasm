package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.block.entity.AnchorOfLightBlockEntity;
import destiny.penumbra_phantasm.server.block.entity.AnchorOfLightPartBlockEntity;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static destiny.penumbra_phantasm.server.block.GenericHorizontalOrientableBlock.HORIZONTAL_FACING;

public class AnchorOfLightBlockItem extends BlockItem {
    public AnchorOfLightBlockItem(Properties pProperties) {
        super(BlockRegistry.ANCHOR_OF_LIGHT.get(), pProperties);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();

        boolean hasFreeSpace = level.getBlockState(pos).isAir() && level.getBlockState(pos.offset(0, -1, 0)).isAir() &&
                level.getBlockState(pos.offset(0, -2, 0)).isAir() && level.getBlockState(pos.offset(0, -3, 0)).isAir() &&
                level.getBlockState(pos.offset(0, -4, 0)).isAir();

        return hasFreeSpace && pContext.getClickedFace() == Direction.DOWN;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext pContext, BlockState pState) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        Direction direction = pContext.getHorizontalDirection();

        for (int i = 0; i < 5; i++) {
            BlockPos offsetPos = pos.offset(0, -i, 0);
            BlockState state;

            if (i == 4) {
                state = BlockRegistry.ANCHOR_OF_LIGHT.get().defaultBlockState().setValue(HORIZONTAL_FACING, direction);

                level.setBlock(offsetPos, state, 11);

                if (pContext.getPlayer() != null) {
                    SoulCapability soulCap = pContext.getPlayer().getCapability(CapabilityRegistry.SOUL).resolve().orElse(null);

                    if (soulCap != null && level.getBlockEntity(offsetPos) instanceof AnchorOfLightBlockEntity anchor) {
                        anchor.soulType = soulCap.soulType;
                    }
                }
            } else {
                state = BlockRegistry.ANCHOR_OF_LIGHT_PART.get().defaultBlockState();

                level.setBlock(offsetPos, state, 11);

                if (level.getBlockEntity(offsetPos) instanceof AnchorOfLightPartBlockEntity anchorPart) {
                    anchorPart.originPos = pos.offset(0, -4, 0);
                }
            }
        }

        return true;
    }
}
