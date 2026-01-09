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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
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
            if(entry.getValue().animationTimer > 125 || entry.getValue().animationTimer == -1)
            {
                double playerX = player.getX();
                double playerZ = player.getZ();

                BlockPos fountainPos = entry.getValue().getFountainPos();
                double fountainX = fountainPos.getX();
                double fountainZ = fountainPos.getZ();

                Vec2 flatPlayerPos = new Vec2((float)playerX, (float) playerZ);
                Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);

                if (flatFountainPos.distanceToSqr(flatPlayerPos) < 64) {
                    fountain = entry.getValue();
                    break; // If fountain within 8 blocks of this(8 squared is 64)
                }
            }
        }
        if (fountain == null) {
            return;
        }

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        boolean isInDarkWorld = player.level().dimension() == ResourceKey.create(Registries.DIMENSION, new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths"));

        double fountainX = fountain.getFountainPos().getX();
        double fountainY = fountain.getFountainPos().getY();
        double fountainZ = fountain.getFountainPos().getZ();

        Vec2 flatPlayerPos = new Vec2((float)playerX, (float) playerZ);
        Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);

        float distance = flatPlayerPos.distanceToSqr(flatFountainPos) / 16F;
        if(!isInDarkWorld)
            distance = (float) player.position().distanceToSqr(Vec3.atLowerCornerOf(fountain.getFountainPos())) / 16F;

        if(isInDarkWorld && playerY < fountainY)
            distance = (float) player.position().distanceToSqr(Vec3.atLowerCornerOf(fountain.getFountainPos())) / 16F;

        float alpha = Mth.lerp(distance, 3.5f, 0f);

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
