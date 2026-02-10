package destiny.penumbra_phantasm.server.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;

public class KnifeItem extends SwordItem {
    public boolean isSingleUse;
    public boolean needsNetherStar;
    private final int damage;
    public KnifeItem(Tier tier, int damage, float speed, boolean isSingleUse, boolean needsNetherStar, Properties properties) {
        super(tier, damage, speed, properties);
        this.isSingleUse = isSingleUse;
        this.needsNetherStar = needsNetherStar;
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

        if (!player.onGround() || DarkFountain.isDarkWorldStatic(level.dimension())) {
            return InteractionResultHolder.fail(stack);
        }

        DarkFountainCapability cap;
        LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else return InteractionResultHolder.fail(stack); // If capability isn't present

        for(Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet())
        {
            if(entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 256)
            {
                player.displayClientMessage(Component.literal("Too close to another Dark Fountain"), true);
                return InteractionResultHolder.fail(stack); // If fountain within 16 blocks of this(16 squared is 256)
            }
        }

        if (needsNetherStar && !tag.getBoolean("determination")) {
            return InteractionResultHolder.fail(stack);
        }
        tag.putInt("tick", 0);

        float initYaw = player.getYHeadRot() * -1;
        double initX = player.getX();
        double initY = player.getEyeY();
        double initZ = player.getZ();
        tag.putFloat("initYaw", initYaw);
        tag.putDouble("initX", initX);
        tag.putDouble("initY", initY);
        tag.putDouble("initZ", initZ);

        //player.push(0, 0.6, 0);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    //FIXME:
    // - Fix particles being seen only on one client
    // - Make animations synced between all clients

    //TODO:
    // - Made opening the fountain depend on the soul capability and determination (100% = 1 fountain)
    // - Clean up code (?)

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean b) {
            if (entity instanceof Player player) {
                CompoundTag tag = stack.getOrCreateTag();
                if (!tag.contains("tick")) {
                    tag.putInt("tick", -2);
                }

                int tick = tag.getInt("tick");

                if (tick >= 14) {
                    tag.putInt("tick", -2);
                    if (!level.getBlockState(player.getOnPos()).isAir()) {
                        if (!level.isClientSide()) {
                            Iterator<ResourceKey<Level>> set = level.getServer().levelKeys().iterator();
                            ResourceKey<Level> target = null;
                            while (set.hasNext()) {
                                ResourceKey<Level> current = set.next();
                                if (current.equals(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths"))))
                                    target = current;
                            }

                            if (target == null) {
                                return;
                            }

                            ResourceKey<Level> finalTarget = target;
                            ServerLevel targetLevel = level.getServer().getLevel(finalTarget);
                            if (targetLevel == null)
                                return;

                            ChunkPos fountainChunk = level.getChunk(player.blockPosition()).getPos();
                            targetLevel.setChunkForced(fountainChunk.x, fountainChunk.z, true);

                            BlockPos fountainPos = targetLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.getOnPos());

                            //Make Light World fountain
                            level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> cap.addDarkFountain(level, player.getOnPos().above(), level.dimension(), fountainPos, finalTarget, 0, 0, 0, new HashSet<>()));

                            //Make Dark World fountain
                            targetLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(
                                    cap -> cap.addDarkFountain(targetLevel, fountainPos,
                                            targetLevel.dimension(), player.getOnPos().above(), level.dimension(), 0, 0, 0, new HashSet<>()));

                            targetLevel.setChunkForced(fountainChunk.x, fountainChunk.z, false);

                            if (!player.isCreative()) {
                                player.getCooldowns().addCooldown(stack.getItem(), 30 * 20);
                            }

                            if (needsNetherStar) {
                                tag.putBoolean("determination", false);
                            }

                            if (isSingleUse) {
                                stack.hurtAndBreak(stack.getMaxDamage(), player, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                            }
                        }
                    }

                } else if (tick >= 0) {
                    if (tick == 0) {
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

                    float initYaw = tag.getFloat("initYaw");
                    double initX = tag.getDouble("initX");
                    double initY = tag.getDouble("initY");
                    double initZ = tag.getDouble("initZ");

                    double yawRad = Math.toRadians(initYaw);
                    double forwardX = Math.sin(yawRad);
                    double forwardZ = Math.cos(yawRad);

                    // Center: offset in front
                    double offsetDist = 2.0;
                    double centerX = initX + forwardX * offsetDist;
                    double centerY = initY;
                    double centerZ = initZ + forwardZ * offsetDist;

                    // Row direction: to the right from player perspective
                    double rowX = -forwardZ;
                    double rowZ = forwardX;

                    // Spacing
                    double spacing = 0.5;

                    // Index for left-to-right: -4 to +4
                    int index = tick - 7;
                    double offsetAlongRow = index * spacing;

                    // Particle position
                    double partX = centerX + rowX * offsetAlongRow;
                    double partY = centerY + (-0.5f + level.getRandom().nextFloat() * 0.5f);
                    double partZ = centerZ + rowZ * offsetAlongRow;

                    level.addParticle(ParticleTypeRegistry.FOUNTAIN_TARGET.get(), partX, partY, partZ, 0, 0, 0);

                    tick++;
                    tag.putInt("tick", tick);
                }
            }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("determination") || super.isFoil(stack);
    }
}
