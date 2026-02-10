package destiny.penumbra_phantasm.client.render.screen;

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
import java.util.UUID;

public class FountainDarknessOverlay {
    public static final ResourceLocation DARKNESS_VERTICAL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/fountain_darkness_vertical.png");
    public static final ResourceLocation DARKNESS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/fountain_darkness_old.png");

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;

        if (player == null) return;
        UUID playerUuid = player.getUUID();

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        Vec2 flatPlayerPos = new Vec2((float)playerX, (float) playerZ);

        //Getting capability
        DarkFountainCapability cap;
        LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else return; // If capability isn't present

        //Finding fountain
        DarkFountain fountain = null;
        for(Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet()) {
            DarkFountain entryFountain = entry.getValue();

            if(entryFountain.animationTimer > 125 || entryFountain.animationTimer == -1) {
                BlockPos fountainPos = entry.getValue().getFountainPos();
                double fountainX = fountainPos.getX();
                double fountainZ = fountainPos.getZ();

                Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);

                if (flatFountainPos.distanceToSqr(flatPlayerPos) < 64) {
                    fountain = entry.getValue();
                    break; // If fountain within 8 blocks of this(8 squared is 64)
                }
            }
        }
        if (fountain == null) return;

        //Rendering shenanigans
        if (DarkFountain.isDarkWorldStatic(level.dimension())) {
            double fountainX = fountain.getFountainPos().getX();
            double fountainY = fountain.getFountainPos().getY();
            double fountainZ = fountain.getFountainPos().getZ();
            Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);

            float distance = flatPlayerPos.distanceToSqr(flatFountainPos) / 16F;

            if(playerY < fountainY)
                distance = (float) player.position().distanceToSqr(Vec3.atLowerCornerOf(fountain.getFountainPos())) / 16F;

            float alpha = Mth.lerp(distance, 3.5f, 0f);

            PoseStack pose = guiGraphics.pose();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, 0f + alpha);
            RenderSystem.setShaderTexture(0, DARKNESS);

            pose.pushPose();
            pose.scale(1f, 1f, 1f);
            guiGraphics.blit(DARKNESS, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
            pose.popPose();
        } else {
            PoseStack pose = guiGraphics.pose();
            double distance = fountain.getFountainPos().getCenter().distanceTo(player.getEyePosition().add(0, -1, 0));
            float distanceDelta = (float) ((distance - 1.5f) / 5);
            float verticalTranslation = (int) (Mth.lerp(distanceDelta, 0, -height + (height / 10f)));

            pose.pushPose();
            pose.translate(0f, verticalTranslation - height / 4f, 0f);
            guiGraphics.blit(DARKNESS_VERTICAL, 0, 0, 0, 0.0F, 0.0F, width, height + height / 2, width, height + height / 2);
            pose.popPose();
        }
    });
}
