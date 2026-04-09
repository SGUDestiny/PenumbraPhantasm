package destiny.penumbra_phantasm.server.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import destiny.penumbra_phantasm.ServerConfig;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundCancelPlayerAnimationPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundParticlePacket;
import destiny.penumbra_phantasm.client.network.ClientBoundPlayPlayerAnimationPacket;
import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkRoom;
import destiny.penumbra_phantasm.server.fountain.RoomScanner;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class KnifeItem extends SwordItem {
    public static final String MAKING_TICK = "makingTick";
    public static final String ORIGIN_YAW = "originYaw";
    public static final String FOUNTAIN_POS = "fountainPos";
    public static final String STAB_STARTED = "stabStarted";
    public static final int IDLE_TICK = -1;

    public static final int JUMP_DURATION = 15;
    public static final int STAB_DELAY = 8;

    public boolean isSingleUse;
    private final int damage;

    public KnifeItem(Tier tier, int damage, float speed, boolean isSingleUse, Properties properties) {
        super(tier, damage, speed, properties);
        this.isSingleUse = isSingleUse;
        this.damage = damage;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2, AttributeModifier.Operation.ADDITION));
            builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -1, AttributeModifier.Operation.ADDITION));

            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        if (hand == InteractionHand.OFF_HAND)
            return InteractionResultHolder.pass(stack);

        if (level.isClientSide()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        ensureTagDefaults(tag);

        if (tag.getInt(MAKING_TICK) >= 0) {
            return InteractionResultHolder.pass(stack);
        }

        //Cancel making a fountain in dark worlds
        if (DarkWorldUtil.isDarkWorld(level)) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_inside_dark_world"), true);
            return InteractionResultHolder.fail(stack);
        }

        //If player isn't grounded, player doesn't stand on solid block, or player's feet block isn't air, cancel
        if (!isStandingOnFullFace(level, player)) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_invalid_position"), true);
            return InteractionResultHolder.fail(stack);
        }

        //Cancel if player isn't inside a valid room
        ServerLevel serverLevel = (ServerLevel) level;
        Set<BlockPos> otherAnchors = DarkFountain.otherFountainAnchors(serverLevel, null);
        Map<BlockPos, ResourceKey<Level>> otherRoomCells = DarkFountain.otherFountainRoomCellsToDarkWorld(serverLevel, null);
        BlockPos scanSeed = player.getOnPos().above();
        RoomScanner.RoomScanResult roomResult = RoomScanner.scan(level, scanSeed, ServerConfig.maxRoomVolume, false, false, otherAnchors, otherRoomCells);
        if (!roomResult.isValid()) {
            if (scanSeedBlockedByExistingFountain(scanSeed, otherAnchors, otherRoomCells)) {
                player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_room_has_active_fountain"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_unsealed_or_too_big"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        //Count blocks for every dark world type's block tag, if enough, select that type as final
        Registry<DarkWorldType> darkWorldTypeRegistry = level.registryAccess().registryOrThrow(DarkWorldType.REGISTRY_KEY);
        DarkWorldType finalDarkWorldType = null;
        for (Map.Entry<ResourceKey<DarkWorldType>, DarkWorldType> darkWorldTypeEntry : darkWorldTypeRegistry.entrySet()) {
            DarkWorldType darkWorldType = darkWorldTypeEntry.getValue();
            TagKey<Block> currentTag = DarkWorldUtil.getBlockTag(darkWorldType.blockTag());
            int blockCount = 0;

            for (BlockPos keyBlockPos : roomResult.getKeyBlockPositions()) {
                if (level.getBlockState(keyBlockPos).is(currentTag)) {
                    blockCount++;
                }
            }

            if (blockCount >= darkWorldType.blockAmount()) {
                finalDarkWorldType = darkWorldType;
            }
        }
        //If not enough key blocks are present, cancel
        if (finalDarkWorldType == null) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_not_enough_key_blocks"), true);
            return InteractionResultHolder.fail(stack);
        }

        //Get fountain capability
        DarkFountainCapability cap;
        LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else {
            // If capability isn't present
            sendErrorMessage(player);
            return InteractionResultHolder.fail(stack);
        }

        if (DarkFountainCapability.roomContainsActiveFountainAnchor(cap, roomResult.getPositions())) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_room_has_active_fountain"), true);
            return InteractionResultHolder.fail(stack);
        }

        //Cancel checks passed, try fountain making
        tag.putInt(MAKING_TICK, 0);
        tag.putFloat(ORIGIN_YAW, player.getYHeadRot() * -1);
        tag.put(FOUNTAIN_POS, NbtUtils.writeBlockPos(player.getOnPos().above()));
        tag.putBoolean(STAB_STARTED, false);
        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity player, @NotNull ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide()) return;

        if (!(player instanceof Player)) return;

        CompoundTag tag = stack.getOrCreateTag();
        ensureTagDefaults(tag);
        int makingTick = tag.getInt(MAKING_TICK);

        if (makingTick < JUMP_DURATION) {
            if (makingTick == 0) {
                PacketHandlerRegistry.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new ClientBoundPlayPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "fountain_make_jump"))
                );
            }
            animateParticles(tag, level, makingTick);
            tag.putInt(MAKING_TICK, makingTick + 1);
        } else {
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity player, int timeCharged) {
        if (level.isClientSide()) return;

        if (!(player instanceof Player)) return;

        CompoundTag tag = stack.getOrCreateTag();
        ensureTagDefaults(tag);
        int makingTick = tag.getInt(MAKING_TICK);

        if (makingTick >= JUMP_DURATION) {
            startStabPhase(player, tag);
        } else {
            resetMakingState(tag);

            PacketHandlerRegistry.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new ClientBoundCancelPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "fountain_make_jump"))
            );
            PacketHandlerRegistry.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new ClientBoundPlayPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "fountain_make_cancel"))
            );
            player.stopUsingItem();
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    //TODO:
    // - Made opening the fountain depend on the soul capability and determination (100% = 1 fountain)

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int i, boolean b) {
        if (level.isClientSide()) return;

        if (entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();

            ensureTagDefaults(tag);

            int makingTick = tag.getInt(MAKING_TICK);

            if (makingTick >= 0) {
                boolean isStillCharging = player.isUsingItem() && player.getUseItem() == stack;

                if (isStillCharging) {
                    return;
                }

                if (makingTick >= JUMP_DURATION && !tag.getBoolean(STAB_STARTED)) {
                    startStabPhase(player, tag);
                    makingTick = tag.getInt(MAKING_TICK);
                }

                if (makingTick >= JUMP_DURATION + STAB_DELAY) {
                    if (!level.isClientSide()) {
                        makeFountain(tag, player, level, stack);
                    }
                } else {
                    tag.putInt(MAKING_TICK, makingTick + 1);
                }
            }
        }
    }

    private void animateParticles(CompoundTag tag, Level level, int tick) {
        if (!tag.contains(ORIGIN_YAW) || !tag.contains(FOUNTAIN_POS)) {
            return;
        }

        float originYaw = tag.getFloat(ORIGIN_YAW);
        BlockPos fountainPos = NbtUtils.readBlockPos(tag.getCompound(FOUNTAIN_POS));

        double yawRad = Math.toRadians(originYaw);
        double forwardX = Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);

        //Center with an offset forward from the player
        double offsetDist = 2.0;
        double centerX = fountainPos.getX() + forwardX * offsetDist;
        double centerZ = fountainPos.getZ() + forwardZ * offsetDist;

        //Row should move to the right from player perspective
        double rowX = -forwardZ;

        //Spacing between particles
        double spacing = 0.5;

        //Index for particles [-4;4]
        int index = tick - 7;
        double offsetAlongRow = index * spacing;

        //Final particle positioning
        double particleX = centerX + rowX * offsetAlongRow;
        double particleY = fountainPos.getY() + 1 + (-0.5f + level.getRandom().nextFloat() * 0.5f);
        double particleZ = centerZ + forwardX * offsetAlongRow;

        //Spawn particle
        PacketHandlerRegistry.INSTANCE.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(particleX, particleY, particleZ, 32.0, level.dimension())),
                new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.FOUNTAIN_TARGET.get()), particleX, particleY, particleZ, 0, 0, 0, 1)
        );
    }

    private static boolean scanSeedBlockedByExistingFountain(BlockPos scanSeed, Set<BlockPos> otherAnchors, Map<BlockPos, ResourceKey<Level>> otherRoomCells) {
        if (otherRoomCells.containsKey(scanSeed)) {
            return true;
        }
        return otherAnchors != null && otherAnchors.contains(scanSeed);
    }

    private static boolean isStandingOnFullFace(Level level, Player player) {
        if (!player.onGround()) {
            return false;
        }
        BlockPos groundPos = player.getOnPos();
        BlockState groundState = level.getBlockState(groundPos);
        BlockState feetState = level.getBlockState(groundPos.above());
        return Block.isFaceFull(groundState.getCollisionShape(level, groundPos), Direction.UP) && feetState.isAir();
    }

    private void makeFountain(CompoundTag tag, Player player, Level level, ItemStack stack) {
        if (!tag.contains(FOUNTAIN_POS)) {
            resetMakingState(tag);
            return;
        }

        //If player isn't grounded, player doesn't stand on solid block, or player's feet block isn't air, cancel
        if (!isStandingOnFullFace(level, player)) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_invalid_position"), true);
            resetMakingState(tag);
            return;
        }

        //Get fountainPos from tag
        BlockPos fountainPos = NbtUtils.readBlockPos(tag.getCompound(FOUNTAIN_POS));

        //Get light fountain capability
        DarkFountainCapability lightCap;
        LazyOptional<DarkFountainCapability> lightLazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lightLazyCapability.isPresent() && lightLazyCapability.resolve().isPresent())
            lightCap = lightLazyCapability.resolve().get();
        else {
            // If capability isn't present
            sendErrorMessage(player);
            resetMakingState(tag);
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            resetMakingState(tag);
            return;
        }
        Set<BlockPos> otherAnchors = DarkFountain.otherFountainAnchors(serverLevel, null);
        Map<BlockPos, ResourceKey<Level>> otherRoomCells = DarkFountain.otherFountainRoomCellsToDarkWorld(serverLevel, null);
        RoomScanner.RoomScanResult roomResult = RoomScanner.scan(level, fountainPos, ServerConfig.maxRoomVolume, false, false, otherAnchors, otherRoomCells);
        if (!roomResult.isValid()) {
            if (scanSeedBlockedByExistingFountain(fountainPos, otherAnchors, otherRoomCells)) {
                player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_room_has_active_fountain"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_unsealed_or_too_big"), true);
            }
            resetMakingState(tag);
            return;
        }

        Registry<DarkWorldType> darkWorldTypeRegistry = level.registryAccess().registryOrThrow(DarkWorldType.REGISTRY_KEY);
        DarkWorldType finalDarkWorldType = null;

        //Count blocks for every dark world type's block tag, if enough, select that type as final
        for (Map.Entry<ResourceKey<DarkWorldType>, DarkWorldType> darkWorldTypeEntry : darkWorldTypeRegistry.entrySet()) {
            DarkWorldType darkWorldType = darkWorldTypeEntry.getValue();
            TagKey<Block> currentTag = DarkWorldUtil.getBlockTag(darkWorldType.blockTag());
            int blockCount = 0;

            for (BlockPos keyBlockPos : roomResult.getKeyBlockPositions()) {
                if (level.getBlockState(keyBlockPos).is(currentTag)) {
                    blockCount++;
                }
            }

            if (blockCount >= darkWorldType.blockAmount()) {
                finalDarkWorldType = darkWorldType;
            }
        }

        //If not enough key blocks are present, cancel
        if (finalDarkWorldType == null) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_not_enough_key_blocks"), true);
            resetMakingState(tag);
            return;
        }

        ResourceLocation typeId = darkWorldTypeRegistry.getKey(finalDarkWorldType);
        if (typeId == null) {
            sendErrorMessage(player);
            resetMakingState(tag);
            return;
        }

        if (DarkFountainCapability.roomContainsActiveFountainAnchor(lightCap, roomResult.getPositions())) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_room_has_active_fountain"), true);
            resetMakingState(tag);
            return;
        }

        Optional<DarkFountainCapability.PersistentDarkWorldSite> matchedSite =
                lightCap.findMatchingPersistentSite(level.getServer(), roomResult.getPositions(), typeId);

        ServerLevel targetLevel = null;
        if (matchedSite.isPresent()) {
            DarkFountainCapability.PersistentDarkWorldSite site = matchedSite.get();
            targetLevel = level.getServer().getLevel(site.dimensionKey);
            if (targetLevel == null) {
                targetLevel = DarkWorldUtil.createDarkWorld(level.getServer(), fountainPos, level.dimension(), finalDarkWorldType);
                if (targetLevel != null) {
                    site.dimensionKey = targetLevel.dimension();
                }
            }
        } else {
            targetLevel = DarkWorldUtil.createDarkWorld(level.getServer(), fountainPos, level.dimension(), finalDarkWorldType);
            if (targetLevel != null) {
                lightCap.registerPersistentSite(fountainPos, typeId, targetLevel.dimension());
            }
        }

        if (targetLevel == null) {
            sendErrorMessage(player);
            resetMakingState(tag);
            return;
        }

        if (!DarkFountainCapability.isDarkWorldAvailableForNewFountain(targetLevel)) {
            targetLevel = DarkWorldUtil.createDarkWorld(level.getServer(), fountainPos, level.dimension(), finalDarkWorldType);
            if (targetLevel == null) {
                sendErrorMessage(player);
                resetMakingState(tag);
                return;
            }
            lightCap.registerPersistentSite(fountainPos, typeId, targetLevel.dimension());
        }

        DarkFountainCapability darkCap;
        LazyOptional<DarkFountainCapability> darkLazyCapability = targetLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(darkLazyCapability.isPresent() && darkLazyCapability.resolve().isPresent())
            darkCap = darkLazyCapability.resolve().get();
        else {
            // If capability isn't present
            sendErrorMessage(player);
            resetMakingState(tag);
            return;
        }

        //Prepare dark world chunk to put the fountain in
        ChunkPos darkChunkPos = new ChunkPos(fountainPos);
        targetLevel.setChunkForced(darkChunkPos.x, darkChunkPos.z, true);

        //Create dark world fountain position in target level, account for worldgen
        BlockPos darkFountainPos = targetLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fountainPos);

        //Unload the dark world chunk
        targetLevel.setChunkForced(darkChunkPos.x, darkChunkPos.z, false);

        //Add light world fountain to the capability
        lightCap.addDarkFountain(fountainPos, level.dimension(), darkFountainPos, targetLevel.dimension(), 0, 0, 0, 0, new HashSet<>(), new ArrayList<>(), -1, -1, 0);

        //Create new dark room instance for the fountain room
        DarkRoom fountainRoom = new DarkRoom(fountainPos, roomResult.getPositions(), roomResult.getDoorPositions(), roomResult.getOutsideDoors(), roomResult.getSharedDoors());
        AABB roomBox = getRoomAABBFromPositions(roomResult.getPositions());
        Set<BlockPos> positionSet = new HashSet<>(roomResult.getPositions());

        //Get light world fountain from the capability
        DarkFountain lightFountain = lightCap.darkFountains.get(fountainPos);
        //Add fountain room to the fountain
        lightFountain.addRoom(fountainRoom);

        //For every entity in fountain room, add to fountain's transport tickers
        for (Entity ent : level.getEntitiesOfClass(Entity.class, roomBox)) {
            if (positionSet.contains(ent.blockPosition()) || positionSet.contains(ent.blockPosition().above())) {
                fountainRoom.getTransportTickers().put(ent.getUUID(), 0);
            }
        }

        //Add dark world fountain to the capability
        darkCap.addDarkFountain(darkFountainPos, targetLevel.dimension(), fountainPos, level.dimension(), 0, 0, 0, 0, new HashSet<>(), new ArrayList<>(), -1, -1, 0);

        //If player is not creative, put cooldown on knife
        if (!player.isCreative()) {
            player.getCooldowns().addCooldown(stack.getItem(), 30 * 20);
        }

        TriggerCriterions.DARK_FOUNTAIN_MAKE.trigger((ServerPlayer) player);

        player.getCooldowns().addCooldown(stack.getItem(), 5 * 20);

        //Break knife if single use
        if (isSingleUse) {
            stack.hurtAndBreak(stack.getMaxDamage(), player, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        resetMakingState(tag);
    }

    private static AABB getRoomAABBFromPositions(List<BlockPos> positions) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());

            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    private void sendErrorMessage(Player player) {
        player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_critical_error"), true);
    }

    private void ensureTagDefaults(CompoundTag tag) {
        if (!tag.contains(MAKING_TICK)) {
            tag.putInt(MAKING_TICK, IDLE_TICK);
        }
    }

    private void resetMakingState(CompoundTag tag) {
        tag.putInt(MAKING_TICK, IDLE_TICK);
        tag.remove(ORIGIN_YAW);
        tag.remove(FOUNTAIN_POS);
        tag.remove(STAB_STARTED);
    }

    private void startStabPhase(LivingEntity player, CompoundTag tag) {
        if (tag.getBoolean(STAB_STARTED)) {
            return;
        }

        tag.putBoolean(STAB_STARTED, true);
        tag.putInt(MAKING_TICK, JUMP_DURATION);

        PacketHandlerRegistry.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new ClientBoundCancelPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "fountain_make_jump"))
        );
        PacketHandlerRegistry.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new ClientBoundPlayPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "fountain_make_stab"))
        );
    }
}