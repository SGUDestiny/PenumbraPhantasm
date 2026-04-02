package destiny.penumbra_phantasm.server.datagen.blockset;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class BlocksetVanillaWoodSubst {

    private static final String NS = "penumbra_phantasm:block/";

    private BlocksetVanillaWoodSubst() {
    }

    public static JsonElement json(String vanillaTemplateJson, WoodBlockset w) {
        return JsonParser.parseString(substitute(vanillaTemplateJson, w));
    }

    public static String substitute(String s, WoodBlockset w) {
        String t = s;
        String tex = NS + w.textureKey();
        t = t.replace("minecraft:block/cherry_fence_gate_wall_open", NS + w.fenceGate() + "_wall_open");
        t = t.replace("minecraft:block/cherry_fence_gate_wall", NS + w.fenceGate() + "_wall");
        t = t.replace("minecraft:block/cherry_fence_gate_open", NS + w.fenceGate() + "_open");
        t = t.replace("minecraft:block/cherry_fence_gate", NS + w.fenceGate());
        t = t.replace("minecraft:block/cherry_fence_post", NS + w.fence() + "_post");
        t = t.replace("minecraft:block/cherry_fence_side", NS + w.fence() + "_side");
        t = t.replace("minecraft:block/cherry_fence_inventory", NS + w.fence() + "_inventory");
        t = t.replace("minecraft:block/cherry_trapdoor_open", NS + w.trapdoor() + "_open");
        t = t.replace("minecraft:block/cherry_trapdoor_top", NS + w.trapdoor() + "_top");
        t = t.replace("minecraft:block/cherry_trapdoor_bottom", NS + w.trapdoor() + "_bottom");
        t = t.replace("minecraft:block/cherry_trapdoor", NS + w.trapdoor());
        t = t.replace("minecraft:block/cherry_button_pressed", NS + w.button() + "_pressed");
        t = t.replace("minecraft:block/cherry_button", NS + w.button());
        t = t.replace("minecraft:block/cherry_pressure_plate_down", NS + w.pressurePlate() + "_down");
        t = t.replace("minecraft:block/cherry_pressure_plate", NS + w.pressurePlate());
        t = t.replace("minecraft:block/cherry_door_", NS + w.door() + "_");
        t = t.replace("minecraft:block/cherry_stairs_inner", NS + w.stairs() + "_inner");
        t = t.replace("minecraft:block/cherry_stairs_outer", NS + w.stairs() + "_outer");
        t = t.replace("minecraft:block/cherry_stairs", NS + w.stairs());
        t = t.replace("minecraft:block/cherry_slab_top", NS + w.slab() + "_top");
        t = t.replace("minecraft:block/cherry_slab", NS + w.slab());
        t = t.replace("minecraft:block/cherry_planks", tex);
        return t;
    }
}
