package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class FriendItem extends Item {
    public FriendItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean b) {
        if (level.isClientSide()) return;

        if (entity instanceof Player player) {
            if (stack.getTag() == null) {
                stack.getOrCreateTag().putInt("animation", 5);
                stack.getOrCreateTag().putInt("fun", 0);
                stack.getOrCreateTag().putBoolean("reset", false);
            }

            // 0-4 Appear
            // 5 Idle
            // 6 Look Left
            // 7 Look Up
            // 8 Look Right
            // 9-19 Disappear

            int animation = stack.getTag().getInt("animation");
            int fun = stack.getTag().getInt("fun");
            boolean reset = stack.getTag().getBoolean("reset");

            if (level.getGameTime() % 10 == 0 && level.random.nextFloat() > 0.7 && fun == 0) {
                if (player.getOffhandItem() == stack || player.getMainHandItem() == stack || getTravelSlots(player).size() <= 2) {
                    fun = level.random.nextInt(2, 4);
                } else {
                    fun = level.random.nextInt(2, 6);
                }

                stack.getOrCreateTag().putInt("fun", fun);
            }

            if (level.getGameTime() % 7 == 0) {
                //Look Left
                if (fun == 2) {
                    if (!reset) {
                        stack.getOrCreateTag().putInt("animation", 6);
                        stack.getOrCreateTag().putBoolean("reset", true);
                    } else if (animation == 6) {
                        stack.getOrCreateTag().putInt("animation", 5);
                        stack.getOrCreateTag().putInt("fun", 0);
                        stack.getOrCreateTag().putBoolean("reset", false);
                    }
                }

                //Look Up
                if (fun == 3) {
                    if (!reset) {
                        stack.getOrCreateTag().putInt("animation", 7);
                        stack.getOrCreateTag().putBoolean("reset", true);
                    } else if (animation == 7) {
                        stack.getOrCreateTag().putInt("animation", 5);
                        stack.getOrCreateTag().putInt("fun", 0);
                        stack.getOrCreateTag().putBoolean("reset", false);
                    }
                }

                //Look Right
                if (fun == 4) {
                    if (!reset) {
                        stack.getOrCreateTag().putInt("animation", 8);
                        stack.getOrCreateTag().putBoolean("reset", true);
                    } else if (animation == 8) {
                        stack.getOrCreateTag().putInt("animation", 5);
                        stack.getOrCreateTag().putInt("fun", 0);
                        stack.getOrCreateTag().putBoolean("reset", false);
                    }
                }
            }

            if (level.getGameTime() % 2 == 0) {
                //Appear
                if (fun == 1) {
                    if (!reset) {
                        stack.getOrCreateTag().putInt("animation", 0);
                        stack.getOrCreateTag().putBoolean("reset", true);
                    } else if (animation >= 0 && animation < 5) {
                        stack.getOrCreateTag().putInt("animation", animation + 1);
                    } else if (animation == 5) {
                        stack.getOrCreateTag().putInt("animation", 5);
                        stack.getOrCreateTag().putInt("fun", 0);
                        stack.getOrCreateTag().putBoolean("reset", false);
                    }
                }

                //Disappear
                if (fun == 5) {
                    ArrayList<Integer> slots = getTravelSlots(player);
                    int chosenSlot = slots.get(level.random.nextInt(0, slots.size()));
                    int currentSlot = getCurrentSlot(player, stack);

                    ItemStack oldItem = player.getInventory().getItem(chosenSlot).copy();

                    if (!reset) {
                        stack.getOrCreateTag().put("item", oldItem.serializeNBT());

                        stack.getOrCreateTag().putInt("animation", 9);
                        stack.getOrCreateTag().putBoolean("reset", true);

                        ItemStack newFriend = new ItemStack(ItemRegistry.FRIEND.get());
                        newFriend.getOrCreateTag().putInt("animation", 0);
                        newFriend.getOrCreateTag().putInt("fun", 1);
                        newFriend.getOrCreateTag().putBoolean("reset", false);

                        player.getInventory().setItem(chosenSlot, newFriend);
                    } else if (animation >= 9 && animation < 18) {
                        stack.getOrCreateTag().putInt("animation", animation + 1);
                    } else if (animation == 18) {
                        player.getInventory().setItem(currentSlot, ItemStack.of(stack.getTag().getCompound("item")));
                    }
                }
            }
        }
    }

    public ArrayList<Integer> getTravelSlots(Player player) {
        ArrayList<Integer> slots = new ArrayList<>();

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (player.getInventory().items.get(i).getItem() != ItemRegistry.FRIEND.get()) {
                slots.add(i);
            }
        }
        return slots;
    }

    public int getCurrentSlot(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (player.getInventory().items.get(i) == stack) {
                return i;
            }
        }
        return 0;
    }
}
