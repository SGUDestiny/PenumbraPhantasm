package destiny.penumbra_phantasm.client.render.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FireDoorScreenButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/fire_door_button.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int BUTTON_WIDTH = 128;
    private static final int BUTTON_HEIGHT = 24;

    public FireDoorScreenButton(int x, int y, Component message, net.minecraft.client.gui.components.Button.OnPress onPress) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int u = 0;
        int v = this.isHoveredOrFocused() ? BUTTON_HEIGHT : 0;

        guiGraphics.blit(TEXTURE, this.getX(), this.getY(), u, v, this.width, this.height, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int textColor = 0xFFFFFF;
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2, textColor);
    }

    @Override
    public void playDownSound(SoundManager pHandler) {
    }
}