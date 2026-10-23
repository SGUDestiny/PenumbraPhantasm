package destiny.penumbra_phantasm.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RenderTypes extends RenderType {
    public RenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    public static RenderType getFountainRenderType(ResourceLocation texture) {
        return create("fountain", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(CULL)
                        .createCompositeState(true));
    }

    public static RenderType getFountainNoCullRenderType(ResourceLocation texture) {
        return create("fountain_no_cull", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .createCompositeState(true));
    }

    public static RenderType getFountainBeamRenderType() {
        return create("fountain_beam", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(true));
    }

    public static RenderType getFountainShockwaveRenderType(ResourceLocation texture) {
        return create("fountain_shockwave", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(CULL)
                        .createCompositeState(true));
    }

    private static final RenderStateShard.ShaderStateShard FOUNTAIN_MASKED_SHADER =
            new RenderStateShard.ShaderStateShard(() -> ModShaders.FOUNTAIN_MASKED);

    public static RenderType getFountainMaskedRenderType(ResourceLocation maskTexture, ResourceLocation flowTexture, boolean shouldWriteDepth) {
        CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                .setShaderState(FOUNTAIN_MASKED_SHADER)
                .setTextureState(
                        RenderStateShard.MultiTextureStateShard.builder()
                                .add(maskTexture, false, false)
                                .add(flowTexture, false, false)
                                .build()
                )
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY);
        if (!shouldWriteDepth)
            builder.setWriteMaskState(COLOR_WRITE);

        return RenderType.create(
                "fountain_masked_portal",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                builder.createCompositeState(false)
        );
    }

    public static RenderType getNegativePhotonsRenderType(ResourceLocation maskTexture, ResourceLocation flowTexture, boolean shouldWriteDepth) {
        CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                .setShaderState(FOUNTAIN_MASKED_SHADER)
                .setTextureState(
                        RenderStateShard.MultiTextureStateShard.builder()
                                .add(maskTexture, false, false)
                                .add(flowTexture, false, false)
                                .build()
                )
                .setCullState(RenderStateShard.NO_CULL)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY);
        if (!shouldWriteDepth)
            builder.setWriteMaskState(COLOR_WRITE);

        return RenderType.create(
                "negative_photons",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                builder.createCompositeState(false)
        );
    }

    public static RenderType getEmissiveRenderType(ResourceLocation texture) {
        return create("emissive", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(CULL)
                        .createCompositeState(true));
    }

    public static RenderType getEmissiveRenderNoDepthRenderType(ResourceLocation texture) {
        return create("emissive", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(CULL)
                        .createCompositeState(true));
    }
}
