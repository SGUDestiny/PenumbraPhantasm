package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

import static destiny.penumbra_phantasm.server.block.GenericHorizontalOrientableBlock.HORIZONTAL_FACING;

public class CliffrockSlideFeature extends Feature<NoneFeatureConfiguration> {
    public CliffrockSlideFeature(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos originPos = context.origin(); // from WORLD_SURFACE_WG
        List<BlockPos> toPlace = new ArrayList<>();
        boolean foundCliffStart = false;

        // 1. Build the exposed column (skip possible dirt cap)
        for (int i = 0; i < 64; i++) {
            BlockPos currentPos = originPos.below(i);
            BlockState state = level.getBlockState(currentPos);

            boolean isCliffrock = state.is(BlockRegistry.CLIFFROCK.get()) ||
                    state.is(BlockRegistry.CLIFFROCK_PATH.get());

            if (!isCliffrock) {
                if (foundCliffStart) break;   // end of the cliff layer
                else continue;                // still above, ignore
            }

            foundCliffStart = true;
            Direction facing = null;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(currentPos.relative(dir)).isAir()) {
                    facing = dir;
                    break;
                }
            }

            if (facing != null) {
                toPlace.add(currentPos);
            } else {
                break; // no air exposure → cliff face ends
            }
        }

        // 2. Minimum length of 3
        if (toPlace.size() < 3) return false;

        // 3. Landing validation
        BlockPos lastPos = toPlace.get(toPlace.size() - 1);
        Direction lastFacing = null;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(lastPos.relative(dir)).isAir()) {
                lastFacing = dir;
                break;
            }
        }
        if (lastFacing == null) return false; // should never happen

        BlockPos landingPos = lastPos.relative(lastFacing).below();
        if (level.getBlockState(landingPos).isAir()) {
            return false; // no ground – player would fall
        }

        // 4. Place the slide blocks
        for (BlockPos pos : toPlace) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(pos.relative(dir)).isAir()) {
                    level.setBlock(pos,
                            BlockRegistry.CLIFFROCK_SLIDE.get().defaultBlockState()
                                    .setValue(HORIZONTAL_FACING, dir), 2);
                    break;
                }
            }
        }
        return true;
    }
}