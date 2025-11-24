package destiny.penumbra_phantasm.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.RenderBlitUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResultHolder;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;

public class DarknessOverlay {
    public static final ResourceLocation TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/darkness.png");

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
            if(entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 64)
            {
                fountain = entry.getValue();
                break; // If fountain within 8 blocks of this(8 squared is 64)
            }
        }
        if (fountain == null) {
            return;
        }

        float distance = Mth.sqrt((float) (Math.pow(player.position().x - fountain.getFountainPos().getX(), 2) + Math.pow(player.position().y - fountain.getFountainPos().getY(), 2) + Math.pow(player.position().z - fountain.getFountainPos().getZ(), 2)));
        float alpha = Mth.lerp(distance / 8f, 3.5f, 0f);

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
