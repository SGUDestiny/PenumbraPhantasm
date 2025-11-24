package destiny.penumbra_phantasm.server.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderBlitUtil
{
    public static void blit(ResourceLocation pAtlasLocation, PoseStack stack, float pX, float pY, float pUOffset, float pVOffset, float pUWidth, float pVHeight) {
        RenderBlitUtil.blit(pAtlasLocation, stack, pX, pY, 0, pUOffset, pVOffset, pUWidth, pVHeight, 256, 256);
    }

    /**
     * Blits a portion of the texture specified by the atlas location onto the screen at the given coordinates with a
     * blit offset and texture coordinates.
     * @param pAtlasLocation the location of the texture atlas.
     * @param pX the x-coordinate of the blit position.
     * @param pY the y-coordinate of the blit position.
     * @param pBlitOffset the z-level offset for rendering order.
     * @param pUOffset the horizontal texture coordinate offset.
     * @param pVOffset the vertical texture coordinate offset.
     * @param pUWidth the width of the blitted portion in texture coordinates.
     * @param pVHeight the height of the blitted portion in texture coordinates.
     * @param pTextureWidth the width of the texture.
     * @param pTextureHeight the height of the texture.
     */
    public static void blit(ResourceLocation pAtlasLocation, PoseStack stack, float pX, float pY, float pBlitOffset, float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
        RenderBlitUtil.blit(pAtlasLocation, stack, pX, pX + pUWidth, pY, pY + pVHeight, pBlitOffset, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
    }

    /**
     * Blits a portion of the texture specified by the atlas location onto the screen at the given position and
     * dimensions with texture coordinates.
     * @param pAtlasLocation the location of the texture atlas.
     * @param pX the x-coordinate of the top-left corner of the blit position.
     * @param pY the y-coordinate of the top-left corner of the blit position.
     * @param pWidth the width of the blitted portion.
     * @param pHeight the height of the blitted portion.
     * @param pUOffset the horizontal texture coordinate offset.
     * @param pVOffset the vertical texture coordinate offset.
     * @param pUWidth the width of the blitted portion in texture coordinates.
     * @param pVHeight the height of the blitted portion in texture coordinates.
     * @param pTextureWidth the width of the texture.
     * @param pTextureHeight the height of the texture.
     */
    public static void blit(ResourceLocation pAtlasLocation, PoseStack stack, float pX, float pY, float pWidth, float pHeight, float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
        RenderBlitUtil.blit(pAtlasLocation, stack, pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
    }

    /**
     * Blits a portion of the texture specified by the atlas location onto the screen at the given position and
     * dimensions with texture coordinates.
     * @param pAtlasLocation the location of the texture atlas.
     * @param pX the x-coordinate of the top-left corner of the blit position.
     * @param pY the y-coordinate of the top-left corner of the blit position.
     * @param pUOffset the horizontal texture coordinate offset.
     * @param pVOffset the vertical texture coordinate offset.
     * @param pWidth the width of the blitted portion.
     * @param pHeight the height of the blitted portion.
     * @param pTextureWidth the width of the texture.
     * @param pTextureHeight the height of the texture.
     */
    public static void blit(ResourceLocation pAtlasLocation, PoseStack stack, float pX, float pY, float pUOffset, float pVOffset, float pWidth, float pHeight, float pTextureWidth, float pTextureHeight) {
        RenderBlitUtil.blit(pAtlasLocation, stack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight);
    }

    /**
     * Performs the inner blit operation for rendering a texture with the specified coordinates and texture coordinates.
     * @param pAtlasLocation the location of the texture atlas.
     * @param pX1 the x-coordinate of the first corner of the blit position.
     * @param pX2 the x-coordinate of the second corner of the blit position.
     * @param pY1 the y-coordinate of the first corner of the blit position.
     * @param pY2 the y-coordinate of the second corner of the blit position.
     * @param pBlitOffset the z-level offset for rendering order.
     * @param pUWidth the width of the blitted portion in texture coordinates.
     * @param pVHeight the height of the blitted portion in texture coordinates.
     * @param pUOffset the horizontal texture coordinate offset.
     * @param pVOffset the vertical texture coordinate offset.
     * @param pTextureWidth the width of the texture.
     * @param pTextureHeight the height of the texture.
     */
    static void blit(ResourceLocation pAtlasLocation, PoseStack stack, float pX1, float pX2, float pY1, float pY2, float pBlitOffset, float pUWidth, float pVHeight, float pUOffset, float pVOffset, float pTextureWidth, float pTextureHeight) {
        RenderBlitUtil.innerBlit(pAtlasLocation, stack, pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / pTextureWidth, (pUOffset + pUWidth) / pTextureWidth, (pVOffset + 0.0F) / pTextureHeight, (pVOffset + pVHeight) / pTextureHeight);
    }

    /**
     * Performs the inner blit operation for rendering a texture with the specified coordinates and texture coordinates
     * without color tfloating.
     * @param pAtlasLocation the location of the texture atlas.
     * @param pX1 the x-coordinate of the first corner of the blit position.
     * @param pX2 the x-coordinate of the second corner of the blit position.
     * @param pY1 the y-coordinate of the first corner of the blit position.
     * @param pY2 the y-coordinate of the second corner of the blit position.
     * @param pBlitOffset the z-level offset for rendering order.
     * @param pMinU the minimum horizontal texture coordinate.
     * @param pMaxU the maximum horizontal texture coordinate.
     * @param pMinV the minimum vertical texture coordinate.
     * @param pMaxV the maximum vertical texture coordinate.
     */
    static void innerBlit(ResourceLocation pAtlasLocation, PoseStack pose, float pX1, float pX2, float pY1, float pY2, float pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        RenderSystem.setShaderTexture(0, pAtlasLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = pose.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, pX1, pY1, pBlitOffset).uv(pMinU, pMinV).endVertex();
        bufferbuilder.vertex(matrix4f, pX1, pY2, pBlitOffset).uv(pMinU, pMaxV).endVertex();
        bufferbuilder.vertex(matrix4f, pX2, pY2, pBlitOffset).uv(pMaxU, pMaxV).endVertex();
        bufferbuilder.vertex(matrix4f, pX2, pY1, pBlitOffset).uv(pMaxU, pMinV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}