package destiny.penumbra_phantasm.server.worldgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SeededNoiseBasedChunkGenerator extends NoiseBasedChunkGenerator {
   public static final Codec<SeededNoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> {
         return generator.biomeSource;
      }), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((generator) -> {
         return generator.settings;
      })).apply(instance, instance.stable(SeededNoiseBasedChunkGenerator::new));
   });
   private static final BlockState AIR = Blocks.AIR.defaultBlockState();
   private final Holder<NoiseGeneratorSettings> settings;
   private final Supplier<Aquifer.FluidPicker> globalFluidPicker;
   private RandomState customState = null;

   public SeededNoiseBasedChunkGenerator(BiomeSource source, Holder<NoiseGeneratorSettings> noise) {
      super(source, noise);
      this.settings = noise;
      this.globalFluidPicker = Suppliers.memoize(() -> {
         return createFluidPicker(noise.value());
      });
   }

   public SeededNoiseBasedChunkGenerator(BiomeSource source, Holder<NoiseGeneratorSettings> noise,
                                         RandomState randomState) {
      super(source, noise);
      this.settings = noise;
      this.globalFluidPicker = Suppliers.memoize(() -> {
         return createFluidPicker(noise.value());
      });
      this.customState = randomState;
   }

   private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings pSettings) {
      Aquifer.FluidStatus aquifer$fluidstatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
      int i = pSettings.seaLevel();
      Aquifer.FluidStatus aquifer$fluidstatus1 = new Aquifer.FluidStatus(i, pSettings.defaultFluid());
      Aquifer.FluidStatus aquifer$fluidstatus2 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
      return (p_224274_, p_224275_, p_224276_) -> {
         return p_224275_ < Math.min(-54, i) ? aquifer$fluidstatus : aquifer$fluidstatus1;
      };
   }

   public CompletableFuture<ChunkAccess> createBiomes(Executor pExecutor, RandomState pRandomState, Blender pBlender, StructureManager pStructureManager, ChunkAccess pChunk) {
      return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
         this.doCreateBiomes(pBlender, customState, pStructureManager, pChunk);
         return pChunk;
      }), Util.backgroundExecutor());
   }

   private void doCreateBiomes(Blender pBlender, RandomState pRandom, StructureManager pStructureManager, ChunkAccess pChunk) {
      NoiseChunk noisechunk = pChunk.getOrCreateNoiseChunk((p_224340_) -> {
         return this.createNoiseChunk(p_224340_, pStructureManager, pBlender, customState);
      });
      BiomeResolver biomeresolver = BelowZeroRetrogen.getBiomeResolver(pBlender.getBiomeResolver(this.biomeSource), pChunk);
      pChunk.fillBiomesFromNoise(biomeresolver, noisechunk.cachedClimateSampler(customState.router(), this.settings.value().spawnTarget()));
   }

   private NoiseChunk createNoiseChunk(ChunkAccess pChunk, StructureManager pStructureManager, Blender pBlender, RandomState pRandom) {
      return NoiseChunk.forChunk(pChunk, customState, Beardifier.forStructuresInChunk(pStructureManager, pChunk.getPos()), this.settings.value(), this.globalFluidPicker.get(), pBlender);
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public Holder<NoiseGeneratorSettings> generatorSettings() {
      return this.settings;
   }

   public boolean stable(ResourceKey<NoiseGeneratorSettings> pSettings) {
      return this.settings.is(pSettings);
   }

   public int getBaseHeight(int pX, int pZ, Heightmap.Types pType, LevelHeightAccessor pLevel, RandomState pRandom) {
      return this.iterateNoiseColumn(pLevel, customState, pX, pZ, (MutableObject<NoiseColumn>)null, pType.isOpaque()).orElse(pLevel.getMinBuildHeight());
   }

   public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor pHeight, RandomState pRandom) {
      MutableObject<NoiseColumn> mutableobject = new MutableObject<>();
      this.iterateNoiseColumn(pHeight, customState, pX, pZ, mutableobject, (Predicate<BlockState>)null);
      return mutableobject.getValue();
   }

   public void addDebugScreenInfo(List<String> pInfo, RandomState pRandom, BlockPos pPos) {
      DecimalFormat decimalformat = new DecimalFormat("0.000");
      NoiseRouter noiserouter = customState.router();
      DensityFunction.SinglePointContext densityfunction$singlepointcontext = new DensityFunction.SinglePointContext(pPos.getX(), pPos.getY(), pPos.getZ());
      double d0 = noiserouter.ridges().compute(densityfunction$singlepointcontext);
      pInfo.add("NoiseRouter T: " + decimalformat.format(noiserouter.temperature().compute(densityfunction$singlepointcontext)) + " V: " + decimalformat.format(noiserouter.vegetation().compute(densityfunction$singlepointcontext)) + " C: " + decimalformat.format(noiserouter.continents().compute(densityfunction$singlepointcontext)) + " E: " + decimalformat.format(noiserouter.erosion().compute(densityfunction$singlepointcontext)) + " D: " + decimalformat.format(noiserouter.depth().compute(densityfunction$singlepointcontext)) + " W: " + decimalformat.format(d0) + " PV: " + decimalformat.format((double)NoiseRouterData.peaksAndValleys((float)d0)) + " AS: " + decimalformat.format(noiserouter.initialDensityWithoutJaggedness().compute(densityfunction$singlepointcontext)) + " N: " + decimalformat.format(noiserouter.finalDensity().compute(densityfunction$singlepointcontext)));
   }

   protected OptionalInt iterateNoiseColumn(LevelHeightAccessor pLevel, RandomState pRandom, int pX, int pZ, @Nullable MutableObject<NoiseColumn> pColumn, @Nullable Predicate<BlockState> pStoppingState) {
      NoiseSettings noisesettings = this.settings.value().noiseSettings().clampToHeightAccessor(pLevel);
      int i = noisesettings.getCellHeight();
      int j = noisesettings.minY();
      int k = Mth.floorDiv(j, i);
      int l = Mth.floorDiv(noisesettings.height(), i);
      if (l <= 0) {
         return OptionalInt.empty();
      } else {
         BlockState[] ablockstate;
         if (pColumn == null) {
            ablockstate = null;
         } else {
            ablockstate = new BlockState[noisesettings.height()];
            pColumn.setValue(new NoiseColumn(j, ablockstate));
         }

         int i1 = noisesettings.getCellWidth();
         int j1 = Math.floorDiv(pX, i1);
         int k1 = Math.floorDiv(pZ, i1);
         int l1 = Math.floorMod(pX, i1);
         int i2 = Math.floorMod(pZ, i1);
         int j2 = j1 * i1;
         int k2 = k1 * i1;
         double d0 = (double)l1 / (double)i1;
         double d1 = (double)i2 / (double)i1;
         NoiseChunk noisechunk = new NoiseChunk(1, customState, j2, k2, noisesettings, DensityFunctions.BeardifierMarker.INSTANCE, this.settings.value(), this.globalFluidPicker.get(), Blender.empty());
         noisechunk.initializeForFirstCellX();
         noisechunk.advanceCellX(0);

         for(int l2 = l - 1; l2 >= 0; --l2) {
            noisechunk.selectCellYZ(l2, 0);

            for(int i3 = i - 1; i3 >= 0; --i3) {
               int j3 = (k + l2) * i + i3;
               double d2 = (double)i3 / (double)i;
               noisechunk.updateForY(j3, d2);
               noisechunk.updateForX(pX, d0);
               noisechunk.updateForZ(pZ, d1);
               BlockState blockstate = noisechunk.getInterpolatedState();
               BlockState blockstate1 = blockstate == null ? this.settings.value().defaultBlock() : blockstate;
               if (ablockstate != null) {
                  int k3 = l2 * i + i3;
                  ablockstate[k3] = blockstate1;
               }

               if (pStoppingState != null && pStoppingState.test(blockstate1)) {
                  noisechunk.stopInterpolation();
                  return OptionalInt.of(j3 + 1);
               }
            }
         }

         noisechunk.stopInterpolation();
         return OptionalInt.empty();
      }
   }

   public void buildSurface(WorldGenRegion pLevel, StructureManager pStructureManager, RandomState pRandom, ChunkAccess pChunk) {
      if (!SharedConstants.debugVoidTerrain(pChunk.getPos())) {
         WorldGenerationContext worldgenerationcontext = new WorldGenerationContext(this, pLevel);
         this.buildSurface(pChunk, worldgenerationcontext, customState, pStructureManager, pLevel.getBiomeManager(), pLevel.registryAccess().registryOrThrow(Registries.BIOME), Blender.of(pLevel));
      }
   }

   @VisibleForTesting
   public void buildSurface(ChunkAccess pChunk, WorldGenerationContext pContext, RandomState pRandom, StructureManager pStructureManager, BiomeManager pBiomeManager, Registry<Biome> pBiomes, Blender pBlender) {
      NoiseChunk noisechunk = pChunk.getOrCreateNoiseChunk((p_224321_) -> {
         return this.createNoiseChunk(p_224321_, pStructureManager, pBlender, customState);
      });
      NoiseGeneratorSettings noisegeneratorsettings = this.settings.value();
      customState.surfaceSystem().buildSurface(customState, pBiomeManager, pBiomes, noisegeneratorsettings.useLegacyRandomSource(), pContext, pChunk, noisechunk, noisegeneratorsettings.surfaceRule());
   }

   public void applyCarvers(WorldGenRegion pLevel, long pSeed, RandomState pRandom, BiomeManager pBiomeManager, StructureManager pStructureManager, ChunkAccess pChunk, GenerationStep.Carving pStep) {
      BiomeManager biomemanager = pBiomeManager.withDifferentSource((p_255581_, p_255582_, p_255583_) -> {
         return this.biomeSource.getNoiseBiome(p_255581_, p_255582_, p_255583_, customState.sampler());
      });
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
      int i = 8;
      ChunkPos chunkpos = pChunk.getPos();
      NoiseChunk noisechunk = pChunk.getOrCreateNoiseChunk((p_224250_) -> {
         return this.createNoiseChunk(p_224250_, pStructureManager, Blender.of(pLevel), customState);
      });
      Aquifer aquifer = noisechunk.aquifer();
      CarvingContext carvingcontext = new CarvingContext(this, pLevel.registryAccess(), pChunk.getHeightAccessorForGeneration(), noisechunk, customState, this.settings.value().surfaceRule());
      CarvingMask carvingmask = ((ProtoChunk)pChunk).getOrCreateCarvingMask(pStep);

      for(int j = -8; j <= 8; ++j) {
         for(int k = -8; k <= 8; ++k) {
            ChunkPos chunkpos1 = new ChunkPos(chunkpos.x + j, chunkpos.z + k);
            ChunkAccess chunkaccess = pLevel.getChunk(chunkpos1.x, chunkpos1.z);
            BiomeGenerationSettings biomegenerationsettings = chunkaccess.carverBiome(() -> {
               return this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkpos1.getMinBlockX()), 0, QuartPos.fromBlock(chunkpos1.getMinBlockZ()), customState.sampler()));
            });
            Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomegenerationsettings.getCarvers(pStep);
            int l = 0;

            for(Holder<ConfiguredWorldCarver<?>> holder : iterable) {
               ConfiguredWorldCarver<?> configuredworldcarver = holder.value();
               worldgenrandom.setLargeFeatureSeed(pSeed + (long)l, chunkpos1.x, chunkpos1.z);
               if (configuredworldcarver.isStartChunk(worldgenrandom)) {
                  configuredworldcarver.carve(carvingcontext, pChunk, biomemanager::getBiome, worldgenrandom, aquifer, chunkpos1, carvingmask);
               }

               ++l;
            }
         }
      }

   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor pExecutor, Blender pBlender, RandomState pRandom, StructureManager pStructureManager, ChunkAccess pChunk) {
      NoiseSettings noisesettings = this.settings.value().noiseSettings().clampToHeightAccessor(pChunk.getHeightAccessorForGeneration());
      int i = noisesettings.minY();
      int j = Mth.floorDiv(i, noisesettings.getCellHeight());
      int k = Mth.floorDiv(noisesettings.height(), noisesettings.getCellHeight());
      if (k <= 0) {
         return CompletableFuture.completedFuture(pChunk);
      } else {
         int l = pChunk.getSectionIndex(k * noisesettings.getCellHeight() - 1 + i);
         int i1 = pChunk.getSectionIndex(i);
         Set<LevelChunkSection> set = Sets.newHashSet();

         for(int j1 = l; j1 >= i1; --j1) {
            LevelChunkSection levelchunksection = pChunk.getSection(j1);
            levelchunksection.acquire();
            set.add(levelchunksection);
         }

         return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {
            return this.doFill(pBlender, pStructureManager, customState, pChunk, j, k);
         }), Util.backgroundExecutor()).whenCompleteAsync((p_224309_, p_224310_) -> {
            for(LevelChunkSection levelchunksection1 : set) {
               levelchunksection1.release();
            }

         }, pExecutor);
      }
   }

   private ChunkAccess doFill(Blender pBlender, StructureManager pStructureManager, RandomState pRandom, ChunkAccess pChunk, int pMinCellY, int pCellCountY) {
      NoiseChunk noisechunk = pChunk.getOrCreateNoiseChunk((p_224255_) -> {
         return this.createNoiseChunk(p_224255_, pStructureManager, pBlender, customState);
      });
      Heightmap heightmap = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap heightmap1 = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
      ChunkPos chunkpos = pChunk.getPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      Aquifer aquifer = noisechunk.aquifer();
      noisechunk.initializeForFirstCellX();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int k = noisechunk.cellWidth();
      int l = noisechunk.cellHeight();
      int i1 = 16 / k;
      int j1 = 16 / k;

      for(int k1 = 0; k1 < i1; ++k1) {
         noisechunk.advanceCellX(k1);

         for(int l1 = 0; l1 < j1; ++l1) {
            int i2 = pChunk.getSectionsCount() - 1;
            LevelChunkSection levelchunksection = pChunk.getSection(i2);

            for(int j2 = pCellCountY - 1; j2 >= 0; --j2) {
               noisechunk.selectCellYZ(j2, l1);

               for(int k2 = l - 1; k2 >= 0; --k2) {
                  int l2 = (pMinCellY + j2) * l + k2;
                  int i3 = l2 & 15;
                  int j3 = pChunk.getSectionIndex(l2);
                  if (i2 != j3) {
                     i2 = j3;
                     levelchunksection = pChunk.getSection(j3);
                  }

                  double d0 = (double)k2 / (double)l;
                  noisechunk.updateForY(l2, d0);

                  for(int k3 = 0; k3 < k; ++k3) {
                     int l3 = i + k1 * k + k3;
                     int i4 = l3 & 15;
                     double d1 = (double)k3 / (double)k;
                     noisechunk.updateForX(l3, d1);

                     for(int j4 = 0; j4 < k; ++j4) {
                        int k4 = j + l1 * k + j4;
                        int l4 = k4 & 15;
                        double d2 = (double)j4 / (double)k;
                        noisechunk.updateForZ(k4, d2);
                        BlockState blockstate = noisechunk.getInterpolatedState();
                        if (blockstate == null) {
                           blockstate = this.settings.value().defaultBlock();
                        }

                        blockstate = this.debugPreliminarySurfaceLevel(noisechunk, l3, l2, k4, blockstate);
                        if (blockstate != AIR && !SharedConstants.debugVoidTerrain(pChunk.getPos())) {
                           levelchunksection.setBlockState(i4, i3, l4, blockstate, false);
                           heightmap.update(i4, l2, l4, blockstate);
                           heightmap1.update(i4, l2, l4, blockstate);
                           if (aquifer.shouldScheduleFluidUpdate() && !blockstate.getFluidState().isEmpty()) {
                              blockpos$mutableblockpos.set(l3, l2, k4);
                              pChunk.markPosForPostprocessing(blockpos$mutableblockpos);
                           }
                        }
                     }
                  }
               }
            }
         }

         noisechunk.swapSlices();
      }

      noisechunk.stopInterpolation();
      return pChunk;
   }

   private BlockState debugPreliminarySurfaceLevel(NoiseChunk pChunk, int pX, int pY, int pZ, BlockState pState) {
      return pState;
   }

   public int getGenDepth() {
      return this.settings.value().noiseSettings().height();
   }

   public int getSeaLevel() {
      return this.settings.value().seaLevel();
   }

   public int getMinY() {
      return this.settings.value().noiseSettings().minY();
   }

   public void spawnOriginalMobs(WorldGenRegion pLevel) {
      if (!this.settings.value().disableMobGeneration()) {
         ChunkPos chunkpos = pLevel.getCenter();
         Holder<Biome> holder = pLevel.getBiome(chunkpos.getWorldPosition().atY(pLevel.getMaxBuildHeight() - 1));
         WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
         worldgenrandom.setDecorationSeed(pLevel.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
         NaturalSpawner.spawnMobsForChunkGeneration(pLevel, holder, chunkpos, worldgenrandom);
      }
   }
}