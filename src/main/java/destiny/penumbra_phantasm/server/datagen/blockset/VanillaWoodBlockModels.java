package destiny.penumbra_phantasm.server.datagen.blockset;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class VanillaWoodBlockModels {

    private static final String PLANKS = "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"minecraft:block/cherry_planks\"}}";
    private static final String PLANKS_EM = "{\"parent\":\"penumbra_phantasm:block/template_cube_all_emissive\",\"textures\":{\"all\":\"minecraft:block/cherry_planks\"}}";
    private static final String STAIRS = "{\"parent\":\"minecraft:block/stairs\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String STAIRS_EM = "{\"parent\":\"penumbra_phantasm:block/template_stairs_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String STAIRS_INNER = "{\"parent\":\"minecraft:block/inner_stairs\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String STAIRS_INNER_EM = "{\"parent\":\"penumbra_phantasm:block/template_inner_stairs_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String STAIRS_OUTER = "{\"parent\":\"minecraft:block/outer_stairs\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String STAIRS_OUTER_EM = "{\"parent\":\"penumbra_phantasm:block/template_outer_stairs_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String SLAB = "{\"parent\":\"minecraft:block/slab\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String SLAB_EM = "{\"parent\":\"penumbra_phantasm:block/template_slab_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String SLAB_TOP = "{\"parent\":\"minecraft:block/slab_top\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String SLAB_TOP_EM = "{\"parent\":\"penumbra_phantasm:block/template_slab_top_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_planks\",\"side\":\"minecraft:block/cherry_planks\",\"top\":\"minecraft:block/cherry_planks\"}}";
    private static final String FENCE_POST = "{\"parent\":\"minecraft:block/fence_post\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FENCE_POST_EM = "{\"parent\":\"penumbra_phantasm:block/template_fence_post_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FENCE_SIDE = "{\"parent\":\"minecraft:block/fence_side\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FENCE_SIDE_EM = "{\"parent\":\"penumbra_phantasm:block/template_fence_side_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FENCE_INV = "{\"parent\":\"minecraft:block/fence_inventory\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FENCE_INV_EM = "{\"parent\":\"penumbra_phantasm:block/template_fence_inventory_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG = "{\"parent\":\"minecraft:block/template_fence_gate\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG_EM = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG_OPEN = "{\"parent\":\"minecraft:block/template_fence_gate_open\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG_OPEN_EM = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_open_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG_WALL = "{\"parent\":\"minecraft:block/template_fence_gate_wall\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG_WALL_EM = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_wall_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG_WALL_OPEN = "{\"parent\":\"minecraft:block/template_fence_gate_wall_open\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String FG_WALL_OPEN_EM = "{\"parent\":\"penumbra_phantasm:block/template_fence_gate_wall_open_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String TD_BOTTOM = "{\"parent\":\"minecraft:block/template_orientable_trapdoor_bottom\",\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    private static final String TD_BOTTOM_EM = "{\"parent\":\"penumbra_phantasm:block/template_orientable_trapdoor_bottom_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    private static final String TD_TOP = "{\"parent\":\"minecraft:block/template_orientable_trapdoor_top\",\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    private static final String TD_TOP_EM = "{\"parent\":\"penumbra_phantasm:block/template_orientable_trapdoor_top_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    private static final String TD_OPEN = "{\"parent\":\"minecraft:block/template_orientable_trapdoor_open\",\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    private static final String TD_OPEN_EM = "{\"parent\":\"penumbra_phantasm:block/template_orientable_trapdoor_open_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_trapdoor\"}}";
    private static final String BTN = "{\"parent\":\"minecraft:block/button\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String BTN_EM = "{\"parent\":\"penumbra_phantasm:block/template_button_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String BTN_P = "{\"parent\":\"minecraft:block/button_pressed\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String BTN_P_EM = "{\"parent\":\"penumbra_phantasm:block/template_button_pressed_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String BTN_INV = "{\"parent\":\"minecraft:block/button_inventory\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String BTN_INV_EM = "{\"parent\":\"penumbra_phantasm:block/template_button_inventory_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String PL_UP = "{\"parent\":\"minecraft:block/pressure_plate_up\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String PL_UP_EM = "{\"parent\":\"penumbra_phantasm:block/template_pressure_plate_up_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String PL_DN = "{\"parent\":\"minecraft:block/pressure_plate_down\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String PL_DN_EM = "{\"parent\":\"penumbra_phantasm:block/template_pressure_plate_down_emissive\",\"textures\":{\"texture\":\"minecraft:block/cherry_planks\"}}";
    private static final String DOOR_BL = "{\"parent\":\"minecraft:block/door_bottom_left\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_BL_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_left_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_BLO = "{\"parent\":\"minecraft:block/door_bottom_left_open\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_BLO_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_left_open_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_BR = "{\"parent\":\"minecraft:block/door_bottom_right\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_BR_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_right_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_BRO = "{\"parent\":\"minecraft:block/door_bottom_right_open\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_BRO_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_bottom_right_open_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TL = "{\"parent\":\"minecraft:block/door_top_left\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TL_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_top_left_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TLO = "{\"parent\":\"minecraft:block/door_top_left_open\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TLO_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_top_left_open_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TR = "{\"parent\":\"minecraft:block/door_top_right\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TR_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_top_right_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TRO = "{\"parent\":\"minecraft:block/door_top_right_open\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";
    private static final String DOOR_TRO_EM = "{\"parent\":\"penumbra_phantasm:block/template_door_top_right_open_emissive\",\"textures\":{\"bottom\":\"minecraft:block/cherry_door_bottom\",\"top\":\"minecraft:block/cherry_door_top\"}}";

    private VanillaWoodBlockModels() {
    }

    private static JsonElement j(String raw, WoodBlockset w) {
        return JsonParser.parseString(BlocksetVanillaWoodSubst.substitute(raw, w));
    }

    public static JsonElement planks(WoodBlockset w) {
        return j(w.isEmissive() ? PLANKS_EM : PLANKS, w);
    }

    public static JsonElement stairs(WoodBlockset w) {
        return j(w.isEmissive() ? STAIRS_EM : STAIRS, w);
    }

    public static JsonElement stairsInner(WoodBlockset w) {
        return j(w.isEmissive() ? STAIRS_INNER_EM : STAIRS_INNER, w);
    }

    public static JsonElement stairsOuter(WoodBlockset w) {
        return j(w.isEmissive() ? STAIRS_OUTER_EM : STAIRS_OUTER, w);
    }

    public static JsonElement slab(WoodBlockset w) {
        return j(w.isEmissive() ? SLAB_EM : SLAB, w);
    }

    public static JsonElement slabTop(WoodBlockset w) {
        return j(w.isEmissive() ? SLAB_TOP_EM : SLAB_TOP, w);
    }

    public static JsonElement fencePost(WoodBlockset w) {
        return j(w.isEmissive() ? FENCE_POST_EM : FENCE_POST, w);
    }

    public static JsonElement fenceSide(WoodBlockset w) {
        return j(w.isEmissive() ? FENCE_SIDE_EM : FENCE_SIDE, w);
    }

    public static JsonElement fenceInventory(WoodBlockset w) {
        return j(w.isEmissive() ? FENCE_INV_EM : FENCE_INV, w);
    }

    public static JsonElement fenceGate(WoodBlockset w) {
        return j(w.isEmissive() ? FG_EM : FG, w);
    }

    public static JsonElement fenceGateOpen(WoodBlockset w) {
        return j(w.isEmissive() ? FG_OPEN_EM : FG_OPEN, w);
    }

    public static JsonElement fenceGateWall(WoodBlockset w) {
        return j(w.isEmissive() ? FG_WALL_EM : FG_WALL, w);
    }

    public static JsonElement fenceGateWallOpen(WoodBlockset w) {
        return j(w.isEmissive() ? FG_WALL_OPEN_EM : FG_WALL_OPEN, w);
    }

    public static JsonElement trapdoorBottom(WoodBlockset w) {
        return j(w.isEmissive() ? TD_BOTTOM_EM : TD_BOTTOM, w);
    }

    public static JsonElement trapdoorTop(WoodBlockset w) {
        return j(w.isEmissive() ? TD_TOP_EM : TD_TOP, w);
    }

    public static JsonElement trapdoorOpen(WoodBlockset w) {
        return j(w.isEmissive() ? TD_OPEN_EM : TD_OPEN, w);
    }

    public static JsonElement doorBottomLeft(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_BL_EM : DOOR_BL, w);
    }

    public static JsonElement doorBottomLeftOpen(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_BLO_EM : DOOR_BLO, w);
    }

    public static JsonElement doorBottomRight(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_BR_EM : DOOR_BR, w);
    }

    public static JsonElement doorBottomRightOpen(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_BRO_EM : DOOR_BRO, w);
    }

    public static JsonElement doorTopLeft(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_TL_EM : DOOR_TL, w);
    }

    public static JsonElement doorTopLeftOpen(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_TLO_EM : DOOR_TLO, w);
    }

    public static JsonElement doorTopRight(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_TR_EM : DOOR_TR, w);
    }

    public static JsonElement doorTopRightOpen(WoodBlockset w) {
        return j(w.isEmissive() ? DOOR_TRO_EM : DOOR_TRO, w);
    }

    public static JsonElement button(WoodBlockset w) {
        return j(w.isEmissive() ? BTN_EM : BTN, w);
    }

    public static JsonElement buttonPressed(WoodBlockset w) {
        return j(w.isEmissive() ? BTN_P_EM : BTN_P, w);
    }

    public static JsonElement buttonInventory(WoodBlockset w) {
        return j(w.isEmissive() ? BTN_INV_EM : BTN_INV, w);
    }

    public static JsonElement pressurePlateUp(WoodBlockset w) {
        return j(w.isEmissive() ? PL_UP_EM : PL_UP, w);
    }

    public static JsonElement pressurePlateDown(WoodBlockset w) {
        return j(w.isEmissive() ? PL_DN_EM : PL_DN, w);
    }
}
