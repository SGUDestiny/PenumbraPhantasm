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

public class EnteredDimensionIdTrigger extends SimpleCriterionTrigger<EnteredDimensionIdTrigger.TriggerInstance> {
    public static final EnteredDimensionIdTrigger INSTANCE = new EnteredDimensionIdTrigger();
    public static final ResourceLocation ID = new ResourceLocation(PenumbraPhantasm.MODID, "entered_dimension_id_contains");

    private EnteredDimensionIdTrigger() {}

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate playerPredicate, DeserializationContext context) {
        @Nullable String fromFragment = json.has("from_dimension_id_contains") ? GsonHelper.getAsString(json, "from_dimension_id_contains") : null;
        @Nullable String toFragment = json.has("to_dimension_id_contains") ? GsonHelper.getAsString(json, "to_dimension_id_contains") : null;
        if (fromFragment == null && toFragment == null) {
            throw new JsonSyntaxException("entered_dimension_id_contains needs from_dimension_id_contains and/or to_dimension_id_contains");
        }
        return new TriggerInstance(playerPredicate, fromFragment, toFragment);
    }

    public void trigger(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        this.trigger(player, instance -> instance.matches(from, to));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final String fromDimensionIdContains;
        @Nullable
        private final String toDimensionIdContains;

        public TriggerInstance(ContextAwarePredicate player, @Nullable String fromDimensionIdContains, @Nullable String toDimensionIdContains) {
            super(ID, player);
            this.fromDimensionIdContains = fromDimensionIdContains;
            this.toDimensionIdContains = toDimensionIdContains;
        }

        public boolean matches(ResourceKey<Level> from, ResourceKey<Level> to) {
            if (fromDimensionIdContains != null && !from.location().toString().contains(fromDimensionIdContains)) {
                return false;
            }
            if (toDimensionIdContains != null && !to.location().toString().contains(toDimensionIdContains)) {
                return false;
            }
            return true;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext conditions) {
            JsonObject json = super.serializeToJson(conditions);
            if (fromDimensionIdContains != null) {
                json.addProperty("from_dimension_id_contains", fromDimensionIdContains);
            }
            if (toDimensionIdContains != null) {
                json.addProperty("to_dimension_id_contains", toDimensionIdContains);
            }
            return json;
        }
    }
}
