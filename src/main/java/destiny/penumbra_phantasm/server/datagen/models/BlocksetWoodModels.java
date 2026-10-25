package destiny.penumbra_phantasm.server.datagen.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import destiny.penumbra_phantasm.server.datagen.BlocksetVanillaWoodReplace;
import destiny.penumbra_phantasm.server.datagen.blocksets.WoodBlockset;

public class BlocksetWoodModels {
    //Default models
    public static final String PLANKS_MODEL = "{\"parent\":\"minecraft:block/cube_all\"," +
            "\"textures\":{\"all\":\"minecraft:block/cherry_planks\"}}";

    public static final String STAIRS_MODEL = "{\"parent\":\"minecraft:block/stairs\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";
    public static final String STAIRS_INNER_MODEL = "{\"parent\":\"minecraft:block/inner_stairs\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";
    public static final String STAIRS_OUTER_MODEL = "{\"parent\":\"minecraft:block/outer_stairs\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";

    public static final String SLAB_MODEL = "{\"parent\":\"minecraft:block/slab\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";
    public static final String SLAB_TOP_MODEL = "{\"parent\":\"minecraft:block/slab_top\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";

    public static final String FENCE_POST_MODEL = "{\"parent\":\"minecraft:block/fence_post\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_SIDE_MODEL = "{\"parent\":\"minecraft:block/fence_side\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_INVENTORY_MODEL = "{\"parent\":\"minecraft:block/fence_inventory\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String FENCE_GATE_MODEL = "{\"parent\":\"minecraft:block/template_fence_gate\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_GATE_OPEN_MODEL = "{\"parent\":\"minecraft:block/template_fence_gate_open\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_GATE_WALL_MODEL = "{\"parent\":\"minecraft:block/template_fence_gate_wall\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_GATE_WALL_OPEN_MODEL = "{\"parent\":\"minecraft:block/template_fence_gate_wall_open\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String TRAPDOOR_BOTTOM_MODEL = "{\"parent\":\"minecraft:block/template_orientable_trapdoor_bottom\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    public static final String TRAPDOOR_TOP_MODEL = "{\"parent\":\"minecraft:block/template_orientable_trapdoor_top\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    public static final String TRAPDOOR_OPEN_MODEL = "{\"parent\":\"minecraft:block/template_orientable_trapdoor_open\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";

    public static final String BUTTON_MODEL = "{\"parent\":\"minecraft:block/button\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String BUTTON_PRESSED_MODEL = "{\"parent\":\"minecraft:block/button_pressed\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String BUTTON_INVENTORY_MODEL = "{\"parent\":\"minecraft:block/button_inventory\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String PRESSURE_PLATE_UP_MODEL = "{\"parent\":\"minecraft:block/pressure_plate_up\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String PRESSURE_PLATE_DOWN_MODEL = "{\"parent\":\"minecraft:block/pressure_plate_down\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String DOOR_BOTTOM_LEFT_MODEL = "{\"parent\":\"minecraft:block/door_bottom_left\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_BOTTOM_LEFT_OPEN_MODEL = "{\"parent\":\"minecraft:block/door_bottom_left_open\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_BOTTOM_RIGHT_MODEL = "{\"parent\":\"minecraft:block/door_bottom_right\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_BOTTOM_RIGHT_OPEN_MODEL = "{\"parent\":\"minecraft:block/door_bottom_right_open\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_LEFT_MODEL = "{\"parent\":\"minecraft:block/door_top_left\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_LEFT_OPEN_MODEL = "{\"parent\":\"minecraft:block/door_top_left_open\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_RIGHT_MODEL = "{\"parent\":\"minecraft:block/door_top_right\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_RIGHT_OPEN_MODEL = "{\"parent\":\"minecraft:block/door_top_right_open\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";



    //Emissive models
    public static final String PLANKS_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_cube_all_emissive\"," +
            "\"textures\":{\"all\":\"minecraft:block/cherry_planks\"}}";

    public static final String STAIRS_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_stairs_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";
    public static final String STAIRS_INNER_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_inner_stairs_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";
    public static final String STAIRS_OUTER_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_outer_stairs_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";

    public static final String SLAB_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_slab_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";
    public static final String SLAB_TOP_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_slab_top_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\"," +
            "\"side\":\"minecraft:block/cherry_planks\"," +
            "\"top\":\"minecraft:block/cherry_planks\"}}";

    public static final String FENCE_POST_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_fence_post_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_SIDE_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_fence_side_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_INVENTORY_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_fence_inventory_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String FENCE_GATE_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_GATE_OPEN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_open_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_GATE_WALL_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_wall_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String FENCE_GATE_WALL_OPEN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_wall_open_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String TRAPDOOR_BOTTOM_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_orientable_trapdoor_bottom_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    public static final String TRAPDOOR_TOP_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_orientable_trapdoor_top_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    public static final String TRAPDOOR_OPEN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_orientable_trapdoor_open_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";

    public static final String BUTTON_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_button_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String BUTTON_PRESSED_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_button_pressed_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String BUTTON_INVENTORY_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_button_inventory_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String PRESSURE_PLATE_UP_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_pressure_plate_up_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    public static final String PRESSURE_PLATE_DOWN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_pressure_plate_down_emissive\"," +
            "\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";

    public static final String DOOR_BOTTOM_LEFT_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_left_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_BOTTOM_LEFT_OPEN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_left_open_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_BOTTOM_RIGHT_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_right_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_BOTTOM_RIGHT_OPEN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_right_open_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_LEFT_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_top_left_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_LEFT_OPEN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_top_left_open_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_RIGHT_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_top_right_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";
    public static final String DOOR_TOP_RIGHT_OPEN_EMISSIVE_MODEL = "{\"parent\":\"penumbra_phantasm:block/template_door_top_right_open_emissive\"," +
            "\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\"," +
            "\"top\":\"minecraft:block/cherry_door_top\"}}";

    private BlocksetWoodModels() {
    }

    public static JsonElement replace(String model, WoodBlockset woodSet) {
        return JsonParser.parseString(BlocksetVanillaWoodReplace.replace(model, woodSet));
    }

    public static JsonElement planks(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? PLANKS_EMISSIVE_MODEL : PLANKS_MODEL, woodSet);
    }

    public static JsonElement stairs(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? STAIRS_EMISSIVE_MODEL : STAIRS_MODEL, woodSet);
    }

    public static JsonElement stairsInner(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? STAIRS_INNER_EMISSIVE_MODEL : STAIRS_INNER_MODEL, woodSet);
    }

    public static JsonElement stairsOuter(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? STAIRS_OUTER_EMISSIVE_MODEL : STAIRS_OUTER_MODEL, woodSet);
    }

    public static JsonElement slab(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? SLAB_EMISSIVE_MODEL : SLAB_MODEL, woodSet);
    }

    public static JsonElement slabTop(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? SLAB_TOP_EMISSIVE_MODEL : SLAB_TOP_MODEL, woodSet);
    }

    public static JsonElement fencePost(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? FENCE_POST_EMISSIVE_MODEL : FENCE_POST_MODEL, woodSet);
    }

    public static JsonElement fenceSide(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? FENCE_SIDE_EMISSIVE_MODEL : FENCE_SIDE_MODEL, woodSet);
    }

    public static JsonElement fenceInventory(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? FENCE_INVENTORY_EMISSIVE_MODEL : FENCE_INVENTORY_MODEL, woodSet);
    }

    public static JsonElement fenceGate(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? FENCE_GATE_EMISSIVE_MODEL : FENCE_GATE_MODEL, woodSet);
    }

    public static JsonElement fenceGateOpen(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? FENCE_GATE_OPEN_EMISSIVE_MODEL : FENCE_GATE_OPEN_MODEL, woodSet);
    }

    public static JsonElement fenceGateWall(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? FENCE_GATE_WALL_EMISSIVE_MODEL : FENCE_GATE_WALL_MODEL, woodSet);
    }

    public static JsonElement fenceGateWallOpen(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? FENCE_GATE_WALL_OPEN_EMISSIVE_MODEL : FENCE_GATE_WALL_OPEN_MODEL, woodSet);
    }

    public static JsonElement trapdoorBottom(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? TRAPDOOR_BOTTOM_EMISSIVE_MODEL : TRAPDOOR_BOTTOM_MODEL, woodSet);
    }

    public static JsonElement trapdoorTop(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? TRAPDOOR_TOP_EMISSIVE_MODEL : TRAPDOOR_TOP_MODEL, woodSet);
    }

    public static JsonElement trapdoorOpen(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? TRAPDOOR_OPEN_EMISSIVE_MODEL : TRAPDOOR_OPEN_MODEL, woodSet);
    }

    public static JsonElement doorBottomLeft(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_BOTTOM_LEFT_EMISSIVE_MODEL : DOOR_BOTTOM_LEFT_MODEL, woodSet);
    }

    public static JsonElement doorBottomLeftOpen(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_BOTTOM_LEFT_OPEN_EMISSIVE_MODEL : DOOR_BOTTOM_LEFT_OPEN_MODEL, woodSet);
    }

    public static JsonElement doorBottomRight(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_BOTTOM_RIGHT_EMISSIVE_MODEL : DOOR_BOTTOM_RIGHT_MODEL, woodSet);
    }

    public static JsonElement doorBottomRightOpen(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_BOTTOM_RIGHT_OPEN_EMISSIVE_MODEL : DOOR_BOTTOM_RIGHT_OPEN_MODEL, woodSet);
    }

    public static JsonElement doorTopLeft(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_TOP_LEFT_EMISSIVE_MODEL : DOOR_TOP_LEFT_MODEL, woodSet);
    }

    public static JsonElement doorTopLeftOpen(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_TOP_LEFT_OPEN_EMISSIVE_MODEL : DOOR_TOP_LEFT_OPEN_MODEL, woodSet);
    }

    public static JsonElement doorTopRight(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_TOP_RIGHT_EMISSIVE_MODEL : DOOR_TOP_RIGHT_MODEL, woodSet);
    }

    public static JsonElement doorTopRightOpen(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? DOOR_TOP_RIGHT_OPEN_EMISSIVE_MODEL : DOOR_TOP_RIGHT_OPEN_MODEL, woodSet);
    }

    public static JsonElement button(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? BUTTON_EMISSIVE_MODEL : BUTTON_MODEL, woodSet);
    }

    public static JsonElement buttonPressed(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? BUTTON_PRESSED_EMISSIVE_MODEL : BUTTON_PRESSED_MODEL, woodSet);
    }

    public static JsonElement buttonInventory(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? BUTTON_INVENTORY_EMISSIVE_MODEL : BUTTON_INVENTORY_MODEL, woodSet);
    }

    public static JsonElement pressurePlateUp(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? PRESSURE_PLATE_UP_EMISSIVE_MODEL : PRESSURE_PLATE_UP_MODEL, woodSet);
    }

    public static JsonElement pressurePlateDown(WoodBlockset woodSet) {
        return replace(woodSet.isEmissive() ? PRESSURE_PLATE_DOWN_EMISSIVE_MODEL : PRESSURE_PLATE_DOWN_MODEL, woodSet);
    }
}