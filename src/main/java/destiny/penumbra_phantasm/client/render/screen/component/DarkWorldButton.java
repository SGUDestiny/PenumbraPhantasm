package destiny.penumbra_phantasm.client.render.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class DarkWorldButton extends AbstractButton {
    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets.png");
    public static final ResourceLocation DEFAULT_TEXTURE_GLOW = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets_glow.png");
    protected static final DarkWorldButton.CreateNarration DEFAULT_NARRATION = (p_253298_) -> p_253298_.get();
    public static final int DEFAULT_TEXTURE_X = 0;
    public static final int DEFAULT_TEXTURE_Y = 46;
    public static final int DEFAULT_TEXTURE_WIDTH = 200;
    public static final int DEFAULT_STATE_HEIGHT = 20;
    public static final int DEFAULT_STATE_COUNT = 3;
    public static final int DEFAULT_BORDER_X = 20;
    public static final int DEFAULT_BORDER_Y = 4;

    private final DarkWorldButton.OnPress onPress;
    private final DarkWorldButton.CreateNarration createNarration;
    private ResourceLocation texture;
    private int textureX, textureY;
    private int textureWidth, textureHeight;
    private int borderX, borderY;
    private int stateHeight;
    private int stateCount;

    protected DarkWorldButton(int x, int y, int width, int height, Component message,
                              DarkWorldButton.OnPress onPress, DarkWorldButton.CreateNarration createNarration,
                              ResourceLocation texture, int textureX, int textureY,
                              int textureWidth, int textureHeight, int stateHeight, int stateCount,
                              int borderX, int borderY) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.createNarration = createNarration;
        this.texture = texture;
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.stateHeight = stateHeight;
        this.stateCount = stateCount;
        this.borderX = borderX;
        this.borderY = borderY;
    }

    protected DarkWorldButton(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message,
                builder.onPress, builder.createNarration,
                builder.texture, builder.textureX, builder.textureY,
                builder.textureWidth, builder.textureHeight, builder.stateHeight,
                builder.stateCount, builder.borderX, builder.borderY);
        setTooltip(builder.tooltip);
    }

    @Override
    public void onPress() {
        onPress.onPress(this);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return createNarration.createNarrationMessage(super::createNarrationMessage);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onPress();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible) {
            if (net.minecraft.client.gui.navigation.CommonInputs.selected(keyCode)) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                onPress();
                return true;
            }
        }
        return false;
    }

    private int getStateIndex() {
        if (!active) return 0;
        if (isHoveredOrFocused()) return 2;
        return 1;
    }

    private int getTextureU() {
        return textureX;
    }

    private int getTextureV() {
        int idx = Math.min(getStateIndex(), stateCount - 1);
        return textureY + idx * stateHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        long periodMs = 5000;
        long elapsed = System.currentTimeMillis() % periodMs;
        float t = (float) elapsed / periodMs;
        float glow = Mth.sin(t * Mth.PI * 2);

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitNineSliced(texture,
                this.getX(), this.getY(),
                this.getWidth(), this.getHeight(),
                borderX, borderY,
                textureWidth, stateHeight,
                getTextureU(), getTextureV());

        if (isHoveredOrFocused()) {
            guiGraphics.setColor(glow, glow, glow, this.alpha);

            guiGraphics.blitNineSliced(DEFAULT_TEXTURE_GLOW,
                    this.getX(), this.getY(),
                    this.getWidth(), this.getHeight(),
                    borderX, borderY,
                    textureWidth, stateHeight,
                    getTextureU(), getTextureV());
        }

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int color = this.getFGColor();
        this.renderString(guiGraphics, minecraft.font, color | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        this.renderScrollingString(guiGraphics, font, 2, color);
    }

    public static Builder builder(Component message, DarkWorldButton.OnPress onPress) {
        return new Builder(message, onPress);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final DarkWorldButton.OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x, y;
        private int width = 150;
        private int height = 20;
        private DarkWorldButton.CreateNarration createNarration = DEFAULT_NARRATION;

        private ResourceLocation texture = DEFAULT_TEXTURE;
        private int textureX = DEFAULT_TEXTURE_X;
        private int textureY = DEFAULT_TEXTURE_Y;
        private int textureWidth = DEFAULT_TEXTURE_WIDTH;
        private int textureHeight = DEFAULT_STATE_HEIGHT * DEFAULT_STATE_COUNT;
        private int stateHeight = DEFAULT_STATE_HEIGHT;
        private int stateCount = DEFAULT_STATE_COUNT;
        private int borderX = DEFAULT_BORDER_X;
        private int borderY = DEFAULT_BORDER_Y;

        public Builder(Component message, DarkWorldButton.OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(DarkWorldButton.CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder textureRegion(int x, int y, int width, int height, int stateHeight, int stateCount) {
            this.textureX = x;
            this.textureY = y;
            this.textureWidth = width;
            this.textureHeight = height;
            this.stateHeight = stateHeight;
            this.stateCount = stateCount;
            return this;
        }

        public Builder nineSlice(int borderX, int borderY) {
            this.borderX = borderX;
            this.borderY = borderY;
            return this;
        }

        public DarkWorldButton build() {
            return new DarkWorldButton(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(DarkWorldButton var1);
    }
}