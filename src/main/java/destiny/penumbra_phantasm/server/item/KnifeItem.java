package destiny.penumbra_phantasm.server.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fountain.DarkRoom;
import destiny.penumbra_phantasm.server.fountain.RoomScanner;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;

public class KnifeItem extends SwordItem {
    public static final String MAKING_TICK = "makingTick";
    public static final String ORIGIN_YAW = "originYaw";
    public static final String ORIGIN_X = "originX";
    public static final String ORIGIN_Y = "originY";
    public static final String ORIGIN_Z = "originZ";

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        //Cancel making a fountain in dark worlds
        if (DarkWorldUtil.isDarkWorld(level)) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_inside_dark_world"), true);
            return InteractionResultHolder.fail(stack);
        }

        //If player isn't grounded, player doesn't stand on solid block, or player's feet block isn't air, cancel
        if (!player.onGround() || !level.getBlockState(player.getOnPos()).isSolidRender(level, player.getOnPos())
                || level.getBlockState(player.getOnPos().above()) != Blocks.AIR.defaultBlockState()) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_invalid_position"), true);
            return InteractionResultHolder.fail(stack);
        }

        //Cancel if player isn't inside a valid room
        RoomScanner.RoomScanResult roomResult = RoomScanner.scan(level, player.getOnPos().above(), Config.maxRoomVolume, false);
        if (!roomResult.isValid()) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_unsealed_or_too_big"), true);
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

        //Cancel if there is another dark fountain nearby
        for(Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet())
        {
            if(entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 256)
            {
                player.displayClientMessage(Component.translatable("making_fountain_another_fountain_nearby"), true);
                return InteractionResultHolder.fail(stack); // If fountain within 16 blocks of this(16 squared is 256)
            }
        }

        //Cancel checks passed, begin fountain making
        tag.putInt(MAKING_TICK, 0);
        tag.putFloat(ORIGIN_YAW, player.getYHeadRot() * -1);
        tag.putDouble(ORIGIN_X, player.getX());
        tag.putDouble(ORIGIN_Y, player.getEyeY());
        tag.putDouble(ORIGIN_Z, player.getZ());

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    //FIXME:
    // - Fix particle being seen only on one client
    // - Make animations synced between all clients

    //TODO:
    // - Made opening the fountain depend on the soul capability and determination (100% = 1 fountain)
    // - Clean up code (?)

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean b) {
            if (entity instanceof Player player) {
                CompoundTag tag = stack.getOrCreateTag();

                //Failsafe if tag isn't present
                if (!tag.contains(MAKING_TICK)) {
                    tag.putInt(MAKING_TICK, -2);
                }

                int makingTick = tag.getInt(MAKING_TICK);

                if (makingTick >= 0) {
                    //Play player animation on first tick
                    if (makingTick == 0) {
                        if (level.isClientSide()) {
                            //Get the animation for that player
                            ModifierLayer<IAnimation> animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(new ResourceLocation(PenumbraPhantasm.MODID, "fountain_make"));

                            if (animation != null) {
                                //You can set an animation from anywhere ON THE CLIENT
                                //Do not attempt to do this on a server, that will only fail

                                animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation(PenumbraPhantasm.MODID, "fountain_make"))));
                                //You might use  animation.replaceAnimationWithFade(); to create fade effect instead of sudden change
                                //See javadoc for details
                            }
                        }
                    }

                    //Animate particles
                    animateParticles(tag, level, makingTick);

                    //After particles, make fountain
                    if (makingTick >= 14) {
                        if (level instanceof ServerLevel serverLevel) {
                            makeFountain(tag, player, serverLevel, stack);
                        }
                    } else {
                        //Keep ticking up as long as ticker isn't or above 14
                        makingTick++;
                        tag.putInt(MAKING_TICK, makingTick);
                    }
                }
            }
    }

    private void animateParticles(CompoundTag tag, Level level, int tick) {
        float originYaw = tag.getFloat(ORIGIN_YAW);
        double originX = tag.getDouble(ORIGIN_X);
        double originY = tag.getDouble(ORIGIN_Y);
        double originZ = tag.getDouble(ORIGIN_Z);

        double yawRad = Math.toRadians(originYaw);
        double forwardX = Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);

        //Center with an offset forward from the player
        double offsetDist = 2.0;
        double centerX = originX + forwardX * offsetDist;
        double centerZ = originZ + forwardZ * offsetDist;

        //Row should move to the right from player perspective
        double rowX = -forwardZ;

        //Spacing between particles
        double spacing = 0.5;

        //Index for particles [-4;4]
        int index = tick - 7;
        double offsetAlongRow = index * spacing;

        //Final particle positioning
        double particleX = centerX + rowX * offsetAlongRow;
        double particleY = originY + (-0.5f + level.getRandom().nextFloat() * 0.5f);
        double particleZ = centerZ + forwardX * offsetAlongRow;

        //Spawn particle
        level.addParticle(ParticleTypeRegistry.FOUNTAIN_TARGET.get(), particleX, particleY, particleZ, 0, 0, 0);
    }

    private void makeFountain(CompoundTag tag, Player player, ServerLevel level, ItemStack stack) {
        //If player isn't grounded, player doesn't stand on solid block, or player's feet block isn't air, cancel
        if (!player.onGround() || !level.getBlockState(player.getOnPos()).isSolidRender(level, player.getOnPos())
                || level.getBlockState(player.getOnPos().above()) != Blocks.AIR.defaultBlockState()) {
            player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_invalid_position"), true);
            return;
        }

        //Immediately stop ticking
        tag.putInt(MAKING_TICK, -2);

        //Create light world fountain position from player feet position
        BlockPos lightFountainPos = player.getOnPos().above();

        //Get fountain capability
        DarkFountainCapability cap;
        LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else {
            // If capability isn't present
            sendErrorMessage(player);
            return;
        }

        //Scan if the room is still valid, get result with all blocks
        RoomScanner.RoomScanResult roomResult = RoomScanner.scan(level, lightFountainPos, Config.maxRoomVolume, false);
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
            return;
        }

        //Create dark world level based on position, dimension and dark world type
        ServerLevel targetLevel = DarkWorldUtil.createDarkWorld(level.getServer(), lightFountainPos, level.dimension(), finalDarkWorldType);
        if (targetLevel == null) {
            sendErrorMessage(player);
            return;
        }

        //Create dark world fountain position in target level, account for worldgen
        BlockPos darkFountainPos = targetLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.getOnPos());

        //Add light world fountain to the capability
        cap.addDarkFountain(lightFountainPos, level.dimension(), darkFountainPos, targetLevel.dimension(), 0, 0, 0, 0, new HashSet<>());
        //Force load light world fountain chunk to pre-generate it
        ChunkPos lightFountainChunk = level.getChunk(player.blockPosition()).getPos();
        level.setChunkForced(lightFountainChunk.x, lightFountainChunk.z, true);

        //Create new dark room instance for the fountain room
        DarkRoom fountainRoom = new DarkRoom(lightFountainPos, roomResult.getPositions(), roomResult.getDoorPositions());
        AABB roomBox = getRoomAABBFromPositions(roomResult.getPositions());
        Set<BlockPos> positionSet = new HashSet<>(roomResult.getPositions());

        //For every entity in fountain room, add to fountain's transport tickers
        for (Entity ent : level.getEntitiesOfClass(Entity.class, roomBox)) {
            if (positionSet.contains(ent.blockPosition()) || positionSet.contains(ent.blockPosition().above())) {
                fountainRoom.getTransportTickers().put(ent.getUUID(), 0);
            }
        }

        //Get light world fountain from the capability
        DarkFountain lightFountain = cap.darkFountains.get(lightFountainPos);

        //Add fountain room to the fountain
        lightFountain.addRoom(fountainRoom);

        //Add dark world fountain to the capability
        cap.addDarkFountain(darkFountainPos, targetLevel.dimension(), lightFountainPos, level.dimension(), 0, 0, 0, 0, new HashSet<>());
        //Force load dark world fountain chunk to pre-generate it
        ChunkPos darkFountainChunk = targetLevel.getChunk(player.blockPosition()).getPos();
        targetLevel.setChunkForced(darkFountainChunk.x, darkFountainChunk.z, true);

        //If player is not creative, put cooldown on knife
        if (!player.isCreative()) {
            player.getCooldowns().addCooldown(stack.getItem(), 30 * 20);
        }

        //Break knife if single use
        if (isSingleUse) {
            stack.hurtAndBreak(stack.getMaxDamage(), player, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
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
}
