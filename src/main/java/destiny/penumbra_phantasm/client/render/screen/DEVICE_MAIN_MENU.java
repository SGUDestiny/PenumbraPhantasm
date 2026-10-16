package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.internal.BrandingControl;

import javax.annotation.Nullable;
import java.util.Objects;

public class DEVICE_MAIN_MENU extends Screen {
    public static final ResourceLocation WHITE_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/white_screen.png");

    private final boolean fading;
    private long fadeInStart;
    public int tick = 0;

    public DEVICE_MAIN_MENU() {
        this(false);
    }

    public DEVICE_MAIN_MENU(boolean pFading) {
        this(pFading, (LogoRenderer)null);
    }

    public DEVICE_MAIN_MENU(boolean pFading, @Nullable LogoRenderer pLogoRenderer) {
        super(Component.translatable("narrator.screen.title"));
        this.fading = pFading;
    }

    @Override
    public void tick() {
        if (tick == 0 || tick % (int)(8.727 * 20) == 0) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundRegistry.INTRO_DRONE.get(), 1f, 0.5f));
        }

        tick++;
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float f = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        RenderSystem.enableBlend();
        graphics.setColor(0, 0, 0, this.fading ? (float)Mth.ceil(Mth.clamp(f, 0.0F, 1.0F)) : 1.0F);
        graphics.blit(WHITE_SCREEN, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        drawString(graphics, Component.literal("PENUMBRA PHANTASM v.BE35").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                2, this.height - 10, 0x006600, 1f);
    }

    public void drawString(GuiGraphics graphics, Component lineString, int x, int y, int color, float alpha) {
        RenderSystem.setShaderColor(1f ,1f, 1f, alpha);
        graphics.drawString(Minecraft.getInstance().font, lineString, x, y, color, false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
