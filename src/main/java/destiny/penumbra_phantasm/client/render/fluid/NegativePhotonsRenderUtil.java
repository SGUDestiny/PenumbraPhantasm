package destiny.penumbra_phantasm.client.render.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.ModShaders;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.server.registry.FluidTypeRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Map;
import java.util.Set;

public class NegativePhotonsRenderUtil {
    public static final ResourceLocation IMAGE_DEPTH = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth.png");
    public static final ResourceLocation WHITE_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/white_screen.png");

    private static final float MAX_FLUID_HEIGHT = 0.8888889F;

    public NegativePhotonsRenderUtil() {}

    public static void renderNegativePhotonsBlocks(ClientLevel level, MultiBufferSource buffer, Camera camera, PoseStack pose, Map<ChunkPos, Set<BlockPos>> negativePhotons) {
        Color middleColor = Color.getHSBColor(0f, 0f, 0.05f);
        ShaderInstance shaderInstance = ModShaders.FOUNTAIN_MASKED;

        if (shaderInstance != null) {
            float shadertime = (level.getGameTime()) * 0.01f;
            shaderInstance.safeGetUniform("Time").set(shadertime);
            Minecraft mc = Minecraft.getInstance();
            float aspect = (float) mc.getWindow().getWidth() /
                    (float) mc.getWindow().getHeight();

            shaderInstance.safeGetUniform("AspectRatio").set(aspect);

        }

        LocalPlayer player = Minecraft.getInstance().player;

        if(player != null) {
            float middleRed = middleColor.getRed() / 255f;
            float middleGreen = middleColor.getGreen() / 255f;
            float middleBlue = middleColor.getBlue() / 255f;

            float tintRed = 1f + (middleRed - 1f);
            float tintGreen = 1f + (middleGreen - 1f);
            float tintBlue = 1f + (middleBlue - 1f);

            if (shaderInstance != null) {
                shaderInstance.safeGetUniform("TintColor").set(
                        tintRed,
                        tintGreen,
                        tintBlue,
                        1f
                );
            }
        }

        VertexConsumer consumer = buffer.getBuffer(RenderTypes.negativePhotons(WHITE_SCREEN, IMAGE_DEPTH, true));
        BlockPos cameraPos = BlockPos.containing(camera.getPosition());
        ChunkPos cameraChunk = new ChunkPos(cameraPos);
        int renderDistance = (int) Minecraft.getInstance().gameRenderer.getRenderDistance();

        for (Map.Entry<ChunkPos, Set<BlockPos>> entry : negativePhotons.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            Set<BlockPos> posSet = entry.getValue();

            if (Math.max(Math.abs(chunkPos.x - cameraChunk.x), Math.abs(chunkPos.z - cameraChunk.z)) > renderDistance) continue;

            for (BlockPos pos : posSet) {
                FluidState fluidState = level.getFluidState(pos);

                if (fluidState.getFluidType() != FluidTypeRegistry.NEGATIVE_PHOTONS.get()) continue;

                BlockState blockState = level.getBlockState(pos);

                renderNegativePhotonsBlock(level, pose, consumer, pos, fluidState, blockState, camera);
            }
        }
    }

    public static void renderNegativePhotonsBlock(ClientLevel level, PoseStack pose, VertexConsumer consumer, BlockPos pos, FluidState fluidState, BlockState state, Camera camera) {
        pose.pushPose();

        Vec3 cameraPos = camera.getPosition();
        pose.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
        Matrix4f matrix = pose.last().pose();

        TextureAtlasSprite[] atextureatlassprite = ForgeHooksClient.getFluidSprites(level, pos, fluidState);

        BlockState downState = level.getBlockState(pos.relative(Direction.DOWN));
        FluidState downFluid = downState.getFluidState();
        BlockState upState = level.getBlockState(pos.relative(Direction.UP));
        FluidState upFluid = upState.getFluidState();
        BlockState northState = level.getBlockState(pos.relative(Direction.NORTH));
        FluidState northFluid = northState.getFluidState();
        BlockState southState = level.getBlockState(pos.relative(Direction.SOUTH));
        FluidState southFluid = southState.getFluidState();
        BlockState westState = level.getBlockState(pos.relative(Direction.WEST));
        FluidState westFluid = westState.getFluidState();
        BlockState eastState = level.getBlockState(pos.relative(Direction.EAST));
        FluidState eastFluid = eastState.getFluidState();

        boolean flag1 = !isNeighborSameFluid(fluidState, upFluid);
        boolean flag2 = shouldRenderFace(level, pos, fluidState, state, Direction.DOWN, downFluid) && !isFaceOccludedByNeighbor(level, pos, Direction.DOWN, MAX_FLUID_HEIGHT, downState);
        boolean flag3 = shouldRenderFace(level, pos, fluidState, state, Direction.NORTH, northFluid);
        boolean flag4 = shouldRenderFace(level, pos, fluidState, state, Direction.SOUTH, southFluid);
        boolean flag5 = shouldRenderFace(level, pos, fluidState, state, Direction.WEST, westFluid);
        boolean flag6 = shouldRenderFace(level, pos, fluidState, state, Direction.EAST, eastFluid);

        if (flag1 || flag2 || flag6 || flag5 || flag3 || flag4) {
            float f3 = level.getShade(Direction.DOWN, true);
            float f4 = level.getShade(Direction.UP, true);
            float f5 = level.getShade(Direction.NORTH, true);
            float f6 = level.getShade(Direction.WEST, true);

            Fluid fluid = fluidState.getType();

            float f11 = getHeight(level, fluid, pos, state, fluidState);
            float f7;
            float f8;
            float f9;
            float f10;

            if (f11 >= 1.0F) {
                f7 = 1.0F;
                f8 = 1.0F;
                f9 = 1.0F;
                f10 = 1.0F;
            } else {
                float f12 = getHeight(level, fluid, pos.north(), northState, northFluid);
                float f13 = getHeight(level, fluid, pos.south(), southState, southFluid);
                float f14 = getHeight(level, fluid, pos.east(), eastState, eastFluid);
                float f15 = getHeight(level, fluid, pos.west(), westState, westFluid);

                f7 = calculateAverageHeight(level, fluid, f11, f12, f14, pos.relative(Direction.NORTH).relative(Direction.EAST));
                f8 = calculateAverageHeight(level, fluid, f11, f12, f15, pos.relative(Direction.NORTH).relative(Direction.WEST));
                f9 = calculateAverageHeight(level, fluid, f11, f13, f14, pos.relative(Direction.SOUTH).relative(Direction.EAST));
                f10 = calculateAverageHeight(level, fluid, f11, f13, f15, pos.relative(Direction.SOUTH).relative(Direction.WEST));
            }

            double d1 = 0;
            double d2 = 0;
            double d0 = 0;

            float f16 = 0.001F;
            float f17 = flag2 ? 0.001F : 0.0F;

            if (flag1 && !isFaceOccludedByNeighbor(level, pos, Direction.UP, Math.min(Math.min(f8, f10), Math.min(f9, f7)), upState)) {
                f8 -= 0.001F;
                f10 -= 0.001F;
                f9 -= 0.001F;
                f7 -= 0.001F;

                Vec3 vec3 = fluidState.getFlow(level, pos);

                float f18;
                float f19;
                float f20;
                float f21;
                float f22;
                float f23;
                float f24;
                float f25;

                if (vec3.x == (double)0.0F && vec3.z == (double)0.0F) {
                    TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
                    f18 = textureatlassprite1.getU(0.0F);
                    f22 = textureatlassprite1.getV(0.0F);
                    f19 = f18;
                    f23 = textureatlassprite1.getV(16.0F);
                    f20 = textureatlassprite1.getU(16.0F);
                    f24 = f23;
                    f21 = f20;
                    f25 = f22;
                } else {
                    TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
                    float f26 = (float) Mth.atan2(vec3.z, vec3.x) - ((float)Math.PI / 2F);
                    float f27 = Mth.sin(f26) * 0.25F;
                    float f28 = Mth.cos(f26) * 0.25F;

                    f18 = textureatlassprite.getU(8.0F + (-f28 - f27) * 16.0F);
                    f22 = textureatlassprite.getV(8.0F + (-f28 + f27) * 16.0F);
                    f19 = textureatlassprite.getU(8.0F + (-f28 + f27) * 16.0F);
                    f23 = textureatlassprite.getV(8.0F + (f28 + f27) * 16.0F);
                    f20 = textureatlassprite.getU(8.0F + (f28 + f27) * 16.0F);
                    f24 = textureatlassprite.getV(8.0F + (f28 - f27) * 16.0F);
                    f21 = textureatlassprite.getU(8.0F + (f28 - f27) * 16.0F);
                    f25 = textureatlassprite.getV(8.0F + (-f28 - f27) * 16.0F);
                }

                vertex(consumer, matrix, d1 + (double)0.0F, d2 + (double)f8, d0 + (double)0.0F, f18, f22);
                vertex(consumer, matrix, d1 + (double)0.0F, d2 + (double)f10, d0 + (double)1.0F, f19, f23);
                vertex(consumer, matrix, d1 + (double)1.0F, d2 + (double)f9, d0 + (double)1.0F, f20, f24);
                vertex(consumer, matrix, d1 + (double)1.0F, d2 + (double)f7, d0 + (double)0.0F, f21, f25);

                if (fluidState.shouldRenderBackwardUpFace(level, pos.above())) {
                    vertex(consumer, matrix, d1 + (double)0.0F, d2 + (double)f8, d0 + (double)0.0F, f18, f22);
                    vertex(consumer, matrix, d1 + (double)1.0F, d2 + (double)f7, d0 + (double)0.0F, f21, f25);
                    vertex(consumer, matrix, d1 + (double)1.0F, d2 + (double)f9, d0 + (double)1.0F, f20, f24);
                    vertex(consumer, matrix, d1 + (double)0.0F, d2 + (double)f10, d0 + (double)1.0F, f19, f23);
                }
            }

            if (flag2) {
                float f40 = atextureatlassprite[0].getU0();
                float f41 = atextureatlassprite[0].getU1();
                float f42 = atextureatlassprite[0].getV0();
                float f43 = atextureatlassprite[0].getV1();

                vertex(consumer, matrix, d1, d2 + (double)f17, d0 + (double)1.0F, f40, f43);
                vertex(consumer, matrix, d1, d2 + (double)f17, d0, f40, f42);
                vertex(consumer, matrix, d1 + (double)1.0F, d2 + (double)f17, d0, f41, f42);
                vertex(consumer, matrix, d1 + (double)1.0F, d2 + (double)f17, d0 + (double)1.0F, f41, f43);
            }

            for(Direction direction : Direction.Plane.HORIZONTAL) {
                float f44;
                float f45;

                double d3;
                double d4;
                double d5;
                double d6;

                boolean flag7;

                switch (direction) {
                    case NORTH:
                        f44 = f8;
                        f45 = f7;
                        d3 = d1;

                        d5 = d1 + (double)1.0F;
                        d4 = d0 + (double)0.001F;
                        d6 = d0 + (double)0.001F;
                        flag7 = flag3;

                        break;
                    case SOUTH:
                        f44 = f9;
                        f45 = f10;
                        d3 = d1 + (double)1.0F;
                        d5 = d1;
                        d4 = d0 + (double)1.0F - (double)0.001F;
                        d6 = d0 + (double)1.0F - (double)0.001F;
                        flag7 = flag4;

                        break;
                    case WEST:
                        f44 = f10;
                        f45 = f8;
                        d3 = d1 + (double)0.001F;
                        d5 = d1 + (double)0.001F;
                        d4 = d0 + (double)1.0F;
                        d6 = d0;
                        flag7 = flag5;

                        break;
                    default:
                        f44 = f7;
                        f45 = f9;
                        d3 = d1 + (double)1.0F - (double)0.001F;
                        d5 = d1 + (double)1.0F - (double)0.001F;
                        d4 = d0;
                        d6 = d0 + (double)1.0F;
                        flag7 = flag6;
                }

                if (flag7 && !isFaceOccludedByNeighbor(level, pos, direction, Math.max(f44, f45), level.getBlockState(pos.relative(direction)))) {
                    BlockPos blockpos = pos.relative(direction);
                    TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];

                    if (atextureatlassprite[2] != null && level.getBlockState(blockpos).shouldDisplayFluidOverlay(level, blockpos, fluidState)) {
                        textureatlassprite2 = atextureatlassprite[2];
                    }

                    float f53 = textureatlassprite2.getU(0.0F);
                    float f32 = textureatlassprite2.getU(8.0F);
                    float f33 = textureatlassprite2.getV((1.0F - f44) * 16.0F * 0.5F);
                    float f34 = textureatlassprite2.getV((1.0F - f45) * 16.0F * 0.5F);
                    float f35 = textureatlassprite2.getV(8.0F);

                    vertex(consumer, matrix, d3, d2 + (double)f44, d4, f53, f33);
                    vertex(consumer, matrix, d5, d2 + (double)f45, d6, f32, f34);
                    vertex(consumer, matrix, d5, d2 + (double)f17, d6, f32, f35);
                    vertex(consumer, matrix, d3, d2 + (double)f17, d4, f53, f35);
                }
            }
        }

        pose.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, double x, double y, double z, float u, float v) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z).color(1f, 1f, 1f, 1f).uv(u, v).endVertex();
    }

    private static boolean isNeighborSameFluid(FluidState pFirstState, FluidState pSecondState) {
        return pSecondState.getType().isSame(pFirstState.getType());
    }

    private static boolean isFaceOccludedByState(BlockGetter pLevel, Direction pFace, float pHeight, BlockPos pPos, BlockState pState) {
        if (pState.canOcclude()) {
            VoxelShape voxelshape = Shapes.box(0.0F, 0.0F, 0.0F, 1.0F, pHeight, 1.0F);
            VoxelShape voxelshape1 = pState.getOcclusionShape(pLevel, pPos);
            return Shapes.blockOccudes(voxelshape, voxelshape1, pFace);
        } else {
            return false;
        }
    }

    private static boolean isFaceOccludedByNeighbor(BlockGetter pLevel, BlockPos pPos, Direction pSide, float pHeight, BlockState pBlockState) {
        return isFaceOccludedByState(pLevel, pSide, pHeight, pPos.relative(pSide), pBlockState);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter pLevel, BlockPos pPos, BlockState pState, Direction pFace) {
        return isFaceOccludedByState(pLevel, pFace.getOpposite(), 1.0F, pPos, pState);
    }

    public static boolean shouldRenderFace(BlockAndTintGetter pLevel, BlockPos pPos, FluidState pFluidState, BlockState pBlockState, Direction pSide, FluidState pNeighborFluid) {
        return !isFaceOccludedBySelf(pLevel, pPos, pBlockState, pSide) && !isNeighborSameFluid(pFluidState, pNeighborFluid);
    }

    private static float getHeight(BlockAndTintGetter pLevel, Fluid pFluid, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos);
        return getHeight(pLevel, pFluid, pPos, blockstate, blockstate.getFluidState());
    }

    private static float getHeight(BlockAndTintGetter pLevel, Fluid pFluid, BlockPos pPos, BlockState pBlockState, FluidState pFluidState) {
        if (pFluid.isSame(pFluidState.getType())) {
            BlockState blockstate = pLevel.getBlockState(pPos.above());
            return pFluid.isSame(blockstate.getFluidState().getType()) ? 1.0F : pFluidState.getOwnHeight();
        } else {
            return !pBlockState.isSolid() ? 0.0F : -1.0F;
        }
    }

    private static float calculateAverageHeight(BlockAndTintGetter pLevel, Fluid pFluid, float pCurrentHeight, float pHeight1, float pHeight2, BlockPos pPos) {
        if (!(pHeight2 >= 1.0F) && !(pHeight1 >= 1.0F)) {
            float[] afloat = new float[2];
            if (pHeight2 > 0.0F || pHeight1 > 0.0F) {
                float f = getHeight(pLevel, pFluid, pPos);
                if (f >= 1.0F) {
                    return 1.0F;
                }

                addWeightedHeight(afloat, f);
            }

            addWeightedHeight(afloat, pCurrentHeight);
            addWeightedHeight(afloat, pHeight2);
            addWeightedHeight(afloat, pHeight1);
            return afloat[0] / afloat[1];
        } else {
            return 1.0F;
        }
    }

    private static void addWeightedHeight(float[] pOutput, float pHeight) {
        if (pHeight >= 0.8F) {
            pOutput[0] += pHeight * 10.0F;
            pOutput[1] += 10.0F;
        } else if (pHeight >= 0.0F) {
            pOutput[0] += pHeight;
            pOutput[1]++;
        }
    }
}
