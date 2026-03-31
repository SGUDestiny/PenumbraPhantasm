package destiny.penumbra_phantasm.server.advancement;

import com.google.gson.JsonSyntaxException;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import com.google.gson.JsonObject;

public class ChangedDimensionContainsTrigger extends SimpleCriterionTrigger<ChangedDimensionContainsTrigger.TriggerInstance> {
    public static final ChangedDimensionContainsTrigger INSTANCE = new ChangedDimensionContainsTrigger();
    public static final ResourceLocation ID = new ResourceLocation(PenumbraPhantasm.MODID, "changed_dimension_contains");

    private ChangedDimensionContainsTrigger() {}

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate playerPredicate, DeserializationContext context) {
        @Nullable String fromFragment = json.has("from_dimension_contains") ? GsonHelper.getAsString(json, "from_dimension_contains") : null;
        @Nullable String toFragment = json.has("to_dimension_contains") ? GsonHelper.getAsString(json, "to_dimension_contains") : null;
        if (fromFragment != null && fromFragment.isBlank()) {
            fromFragment = null;
        }
        if (toFragment != null && toFragment.isBlank()) {
            toFragment = null;
        }
        if (fromFragment == null && toFragment == null) {
            throw new JsonSyntaxException("changed_dimension_contains needs from_dimension_contains and/or to_dimension_contains");
        }
        return new TriggerInstance(playerPredicate, fromFragment, toFragment);
    }

    public void trigger(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        this.trigger(player, instance -> instance.matches(from, to));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final String fromDimensionContains;
        @Nullable
        private final String toDimensionContains;

        public TriggerInstance(ContextAwarePredicate player, @Nullable String fromDimensionIdContains, @Nullable String toDimensionIdContains) {
            super(ID, player);
            this.fromDimensionContains = fromDimensionIdContains;
            this.toDimensionContains = toDimensionIdContains;
        }

        public boolean matches(ResourceKey<Level> from, ResourceKey<Level> to) {
            if (fromDimensionContains != null && !from.location().toString().contains(fromDimensionContains)) {
                return false;
            }
            if (toDimensionContains != null && !to.location().toString().contains(toDimensionContains)) {
                return false;
            }
            return true;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext conditions) {
            JsonObject json = super.serializeToJson(conditions);
            if (fromDimensionContains != null) {
                json.addProperty("from_dimension_contains", fromDimensionContains);
            }
            if (toDimensionContains != null) {
                json.addProperty("to_dimension_contains", toDimensionContains);
            }
            return json;
        }
    }
}
