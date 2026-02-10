package destiny.penumbra_phantasm.server.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

//TODO:
// - Make perfect strike mechanic

public class FamiliarSwordItem extends SwordItem {
    public FamiliarSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }
}
