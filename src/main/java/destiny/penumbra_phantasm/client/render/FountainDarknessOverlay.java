package destiny.penumbra_phantasm.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;

public class FountainDarknessOverlay {
    public static final ResourceLocation TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/fountain_darkness.png");

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, screenWidth, screenHeight)
            -> {
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null) return;

        DarkFountainCapability cap;
        LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else return; // If capability isn't present

        DarkFountain fountain = null;

        for(Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet())
        {
            if((entry.getValue().animationTimer > 125 || entry.getValue().animationTimer == -1) && entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 64)
            {
                fountain = entry.getValue();
                break; // If fountain within 8 blocks of this(8 squared is 64)
            }
        }
        if (fountain == null) {
            return;
        }

        double playerX = player.getX();
        double playerZ = player.getZ();

        boolean isInDarkWorld = player.level().dimension().equals(fountain.getDestinationDimension());

        double fountainX = isInDarkWorld ? fountain.getDestinationPos().getX() : fountain.getFountainPos().getX();
        double fountainZ = isInDarkWorld ? fountain.getDestinationPos().getZ() : fountain.getFountainPos().getZ();

        Vec2 flatPlayerPos = new Vec2((float)playerX, (float) playerZ);
        Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);
        float distance = flatPlayerPos.distanceToSqr(flatFountainPos);

        float alpha = Mth.lerp(distance / 24f, 3.5f, 0f);

        PoseStack pose = guiGraphics.pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 0f + alpha);
        RenderSystem.setShaderTexture(0, TEXTURE);

        pose.pushPose();
        pose.scale(1f, 1f, 1f);
        guiGraphics.blit(TEXTURE, 0, 0, -90, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
        pose.popPose();
    });
}
