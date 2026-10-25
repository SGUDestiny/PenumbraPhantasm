package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.datagen.blocksets.WoodBlockset;

public class BlocksetVanillaWoodReplace {
    public static final String PATH = PenumbraPhantasm.MODID + ":block/";

    public BlocksetVanillaWoodReplace() {
    }

    public static JsonElement replaceJson(String vanillaTemplate, WoodBlockset woodSet) {
        return JsonParser.parseString(replace(vanillaTemplate, woodSet));
    }

    public static String replace(String vanillaTemplate, WoodBlockset woodSet) {
        String template = vanillaTemplate;
        String texture = PATH + woodSet.textureKey();

        template = template.replace("minecraft:block/cherry_fence_gate_wall_open", PATH + woodSet.fenceGate() + "_wall_open");
        template = template.replace("minecraft:block/cherry_fence_gate_wall", PATH + woodSet.fenceGate() + "_wall");
        template = template.replace("minecraft:block/cherry_fence_gate_open", PATH + woodSet.fenceGate() + "_open");
        template = template.replace("minecraft:block/cherry_fence_gate", PATH + woodSet.fenceGate());

        template = template.replace("minecraft:block/cherry_fence_post", PATH + woodSet.fence() + "_post");
        template = template.replace("minecraft:block/cherry_fence_side", PATH + woodSet.fence() + "_side");
        template = template.replace("minecraft:block/cherry_fence_inventory", PATH + woodSet.fence() + "_inventory");

        template = template.replace("minecraft:block/cherry_trapdoor_open", PATH + woodSet.trapdoor() + "_open");
        template = template.replace("minecraft:block/cherry_trapdoor_top", PATH + woodSet.trapdoor() + "_top");
        template = template.replace("minecraft:block/cherry_trapdoor_bottom", PATH + woodSet.trapdoor() + "_bottom");
        template = template.replace("minecraft:block/cherry_trapdoor", PATH + woodSet.trapdoor());

        template = template.replace("minecraft:block/cherry_button_pressed", PATH + woodSet.button() + "_pressed");
        template = template.replace("minecraft:block/cherry_button", PATH + woodSet.button());

        template = template.replace("minecraft:block/cherry_pressure_plate_down", PATH + woodSet.pressurePlate() + "_down");
        template = template.replace("minecraft:block/cherry_pressure_plate", PATH + woodSet.pressurePlate());

        template = template.replace("minecraft:block/cherry_door_", PATH + woodSet.door() + "_");

        template = template.replace("minecraft:block/cherry_stairs_inner", PATH + woodSet.stairs() + "_inner");
        template = template.replace("minecraft:block/cherry_stairs_outer", PATH + woodSet.stairs() + "_outer");
        template = template.replace("minecraft:block/cherry_stairs", PATH + woodSet.stairs());

        template = template.replace("minecraft:block/cherry_slab_top", PATH + woodSet.slab() + "_top");
        template = template.replace("minecraft:block/cherry_slab", PATH + woodSet.slab());

        template = template.replace("minecraft:block/cherry_planks", texture);

        return template;
    }
}