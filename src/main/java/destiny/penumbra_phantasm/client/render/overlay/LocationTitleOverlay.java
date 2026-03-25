package destiny.penumbra_phantasm.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.item.MusicMediumItem;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

public class LocationTitleOverlay {
    public static int lastTick = -1;

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;
        PoseStack pose = guiGraphics.pose();

        if (player == null) return;

        if (!player.level().dimension().location().equals(new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths"))) return;

        //Getting capability
        ScreenAnimationCapability cap;
        LazyOptional<ScreenAnimationCapability> lazyCapability = player.getCapability(CapabilityRegistry.SCREEN_ANIMATION);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else return; // If capability isn't present

        String currentLocation = cap.currentLocation;
        int ticker = cap.titleAlphaTicker;
        if (ticker < 0)
            return;

        if(lastTick == -1 || ticker == 0)
            lastTick = 0;

        if (ticker == 2 && lastTick != 2) {
            player.playSound(SoundRegistry.HIM_QUICK.get(), 0.125f, 1f);
        }

        float tickDelta = ticker / 80f;
        float titleAlpha;
        if (tickDelta <= 0.25f) {
            titleAlpha = Mth.lerp(tickDelta / 0.25f, 0f, 1f);
        } else if (tickDelta <= 0.75f) {
            titleAlpha = 1f;
        } else {
            titleAlpha = Mth.lerp((tickDelta - 0.75f) / 0.25f, 1f, 0f);
        }

        float subtitleAlpha = 0f;
        if (ticker >= 20) {
            int subtitleTicker = ticker - 20;

            float subtitleTickDelta = subtitleTicker / 60f;
            if (subtitleTickDelta <= 0.25f) {
                subtitleAlpha = Mth.lerp(subtitleTickDelta / 0.25f, 0f, 1f);
            } else if (subtitleTickDelta <= 0.75f) {
                subtitleAlpha = 1f;
            } else {
                subtitleAlpha = Mth.lerp((subtitleTickDelta - 0.75f) / 0.25f, 1f, 0f);
            }
        }

        String fountainTranslatable = Util.makeDescriptionId("location", new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain"));

        String fieldOfHopesAndDreamsTranslatable = Util.makeDescriptionId("biome", new ResourceLocation(PenumbraPhantasm.MODID, "field_of_hopes_and_dreams"));
        String scarletForestTranslatable = Util.makeDescriptionId("biome", new ResourceLocation(PenumbraPhantasm.MODID, "scarlet_forest"));

        int color = 0xFFFFFF;
        if (currentLocation.equals(fountainTranslatable))
            color = getColor();
        if (currentLocation.equals(fieldOfHopesAndDreamsTranslatable))
            color = 0xCD00D1;
        if (currentLocation.equals(scarletForestTranslatable))
            color = 0xE8004D;

        pose.pushPose();
        pose.translate(width / 2f, height / 2f, 0f);
        pose.scale(3f, 3f, 1f);
        pose.translate(-width / 2f, -height / 2f, 0f);
        drawCenteredString(guiGraphics, Component.translatable(currentLocation), width / 2, (int) (height / 2.65f), color, titleAlpha);
        pose.popPose();

        pose.pushPose();
        pose.translate(width / 2f, height / 2f, 0f);
        pose.scale(2f, 2f, 1f);
        pose.translate(-width / 2f, -height / 2f, 0f);
        drawCenteredString(guiGraphics, Component.translatable(currentLocation + ".description"), width / 2, (int) (height / 2.8f), color, subtitleAlpha);
        pose.popPose();

        lastTick = ticker;
    });

    public static void drawCenteredString(GuiGraphics graphics, Component lineString, int x, int y, int color, float alpha) {
        RenderSystem.setShaderColor(1f ,1f, 1f, alpha);
        graphics.drawCenteredString(Minecraft.getInstance().font, lineString.getString(), x, y, color);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static int getColor() {
        int period = 7500;
        long time = System.currentTimeMillis();

        float cyclePos = (float) ((time % period) / (double) period);

        float angle = (float) (cyclePos * 2 * Math.PI);

        int red = (int) ((Math.sin(angle) * 127) + 128);
        int green = (int) ((Math.sin(angle + 2 * Math.PI / 3) * 127) + 128);
        int blue = (int) ((Math.sin(angle + 4 * Math.PI / 3) * 127) + 128);

        return (red << 16) | (green << 8) | blue;
    }
}
