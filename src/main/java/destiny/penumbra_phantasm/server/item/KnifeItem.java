package destiny.penumbra_phantasm.server.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

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
            builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2, AttributeModifier.Operation.ADDITION));

            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        if (!player.onGround() || level.dimension().location().toString().equals("penumbra_phantasm:dark_depths")) {
            return InteractionResultHolder.fail(stack);
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

        player.push(0, 0.6, 0);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

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
                        level.setBlockAndUpdate(player.getOnPos().above(), BlockRegistry.DARK_FOUNTAIN_OPENING.get().defaultBlockState());
                        player.getCooldowns().addCooldown(stack.getItem(), 30 * 20);

                        if (needsNetherStar) {
                            tag.putBoolean("determination", false);
                        }

                        if (isSingleUse) {
                            stack.hurtAndBreak(stack.getMaxDamage(), player, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                        }
                    }
                }

            } else if (tick >= 0) {
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
