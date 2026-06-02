package destiny.penumbra_phantasm.client.render.screen.component;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class DarkWorldCycleButton<T> extends AbstractButton {
    public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);

    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets.png");
    public static final ResourceLocation DEFAULT_TEXTURE_GLOW = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets_glow.png");
    public static final int DEFAULT_TEXTURE_X = 0;
    public static final int DEFAULT_TEXTURE_Y = 46;
    public static final int DEFAULT_TEXTURE_WIDTH = 200;
    public static final int DEFAULT_STATE_HEIGHT = 20;
    public static final int DEFAULT_STATE_COUNT = 3;
    public static final int DEFAULT_BORDER_X = 20;
    public static final int DEFAULT_BORDER_Y = 4;

    private final Component name;
    private int index;
    private T value;
    private final ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;         // uppercased wrapper
    private final Function<DarkWorldCycleButton<T>, MutableComponent> narrationProvider;
    private final OnValueChange<T> onValueChange;
    private final boolean displayOnlyValue;
    private final OptionInstance.TooltipSupplier<T> tooltipSupplier;

    private final ResourceLocation texture;
    private final int textureX, textureY;
    private final int textureWidth, textureHeight;
    private final int borderX, borderY;
    private final int stateHeight;
    private final int stateCount;

    protected DarkWorldCycleButton(int x, int y, int width, int height, Component message, Component name, int index, T value, ValueListSupplier<T> values,
                                   Function<T, Component> originalValueStringifier, Function<DarkWorldCycleButton<T>, MutableComponent> narrationProvider, OnValueChange<T> onValueChange,
                                   OptionInstance.TooltipSupplier<T> tooltipSupplier, boolean displayOnlyValue, ResourceLocation texture, int textureX, int textureY,
                                   int textureWidth, int textureHeight, int stateHeight, int stateCount, int borderX, int borderY) {
        super(x, y, width, height, message);
        this.name = name;
        this.index = index;
        this.value = value;
        this.values = values;

        this.valueStringifier = val -> {
            Component original = originalValueStringifier.apply(val);
            return Component.literal(original.getString().toUpperCase()).withStyle(original.getStyle());
        };

        this.narrationProvider = narrationProvider;
        this.onValueChange = onValueChange;
        this.displayOnlyValue = displayOnlyValue;
        this.tooltipSupplier = tooltipSupplier;
        this.texture = texture;
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.stateHeight = stateHeight;
        this.stateCount = stateCount;
        this.borderX = borderX;
        this.borderY = borderY;

        updateTooltip();
    }

    private void updateTooltip() {
        this.setTooltip(this.tooltipSupplier.apply(this.value));
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            cycleValue(-1);
        } else {
            cycleValue(1);
        }
    }

    private void cycleValue(int delta) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + delta, list.size());
        T newValue = list.get(this.index);
        updateValue(newValue);
        this.onValueChange.onValueChange(this, newValue);
    }

    private T getCycledValue(int delta) {
        List<T> list = this.values.getSelectedList();
        return list.get(Mth.positiveModulo(this.index + delta, list.size()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0.0F) {
            cycleValue(-1);
        } else if (delta < 0.0F) {
            cycleValue(1);
        }
        return true;
    }

    public void setValue(T newValue) {
        List<T> list = this.values.getSelectedList();
        int i = list.indexOf(newValue);
        if (i != -1) {
            this.index = i;
        }
        updateValue(newValue);
    }

    private void updateValue(T newValue) {
        Component label = createLabelForValue(newValue);
        this.setMessage(label);
        this.value = newValue;
        updateTooltip();
    }

    private Component createLabelForValue(T val) {
        return this.displayOnlyValue ? this.valueStringifier.apply(val) : createFullName(val);
    }

    private MutableComponent createFullName(T val) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(val));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            T nextValue = this.getCycledValue(1);
            Component nextLabel = this.createLabelForValue(nextValue);
            if (this.isFocused()) {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", nextLabel));
            } else {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", nextLabel));
            }
        }
    }

    public MutableComponent createDefaultNarrationMessage() {
        return wrapDefaultNarrationMessage(
                this.displayOnlyValue ? createFullName(this.value) : getMessage()
        );
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

    // ---------- Static builders ----------
    public static <T> Builder<T> builder(Function<T, Component> valueStringifier) {
        return new Builder<>(valueStringifier);
    }

    public static Builder<Boolean> booleanBuilder(Component on, Component off) {
        return new Builder<Boolean>((val) -> val ? on : off).withValues(BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder() {
        return new Builder<Boolean>((val) -> val ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).withValues(BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder(boolean initialValue) {
        return onOffBuilder().withInitialValue(initialValue);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private OptionInstance.TooltipSupplier<T> tooltipSupplier = v -> null;
        private Function<DarkWorldCycleButton<T>, MutableComponent> narrationProvider = DarkWorldCycleButton::createDefaultNarrationMessage;
        private ValueListSupplier<T> values = ValueListSupplier.create(ImmutableList.of());
        private boolean displayOnlyValue;

        private ResourceLocation texture = DEFAULT_TEXTURE;
        private int textureX = DEFAULT_TEXTURE_X;
        private int textureY = DEFAULT_TEXTURE_Y;
        private int textureWidth = DEFAULT_TEXTURE_WIDTH;
        private int textureHeight = DEFAULT_STATE_HEIGHT * DEFAULT_STATE_COUNT;
        private int stateHeight = DEFAULT_STATE_HEIGHT;
        private int stateCount = DEFAULT_STATE_COUNT;
        private int borderX = DEFAULT_BORDER_X;
        private int borderY = DEFAULT_BORDER_Y;

        private Builder(Function<T, Component> valueStringifier) {
            this.valueStringifier = valueStringifier;
        }

        public Builder<T> withValues(Collection<T> vals) {
            return withValues(ValueListSupplier.create(vals));
        }

        @SafeVarargs
        public final Builder<T> withValues(T... vals) {
            return withValues(ImmutableList.copyOf(vals));
        }

        public Builder<T> withValues(List<T> defaultList, List<T> selectedList) {
            return withValues(ValueListSupplier.create(DEFAULT_ALT_LIST_SELECTOR, defaultList, selectedList));
        }

        public Builder<T> withValues(BooleanSupplier altListSelector, List<T> defaultList, List<T> selectedList) {
            return withValues(ValueListSupplier.create(altListSelector, defaultList, selectedList));
        }

        public Builder<T> withValues(ValueListSupplier<T> supplier) {
            this.values = supplier;
            return this;
        }

        public Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> supplier) {
            this.tooltipSupplier = supplier;
            return this;
        }

        public Builder<T> withInitialValue(T initialValue) {
            this.initialValue = initialValue;
            int i = this.values.getDefaultList().indexOf(initialValue);
            if (i != -1) {
                this.initialIndex = i;
            }
            return this;
        }

        public Builder<T> withCustomNarration(Function<DarkWorldCycleButton<T>, MutableComponent> narrationProvider) {
            this.narrationProvider = narrationProvider;
            return this;
        }

        public Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public Builder<T> texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder<T> textureRegion(int x, int y, int stateWidth, int stackedHeight, int stateHeight, int stateCount) {
            this.textureX = x;
            this.textureY = y;
            this.textureWidth = stateWidth;
            this.textureHeight = stackedHeight;
            this.stateHeight = stateHeight;
            this.stateCount = stateCount;
            return this;
        }

        public Builder<T> nineSlice(int borderX, int borderY) {
            this.borderX = borderX;
            this.borderY = borderY;
            return this;
        }

        public DarkWorldCycleButton<T> create(int x, int y, int width, int height, Component name) {
            return create(x, y, width, height, name, (btn, val) -> {});
        }

        public DarkWorldCycleButton<T> create(int x, int y, int width, int height, Component name, OnValueChange<T> onValueChange) {
            List<T> defaultList = this.values.getDefaultList();
            if (defaultList.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            }

            T initial = this.initialValue != null ? this.initialValue : defaultList.get(this.initialIndex);

            Component originalLabel = this.valueStringifier.apply(initial);
            Component initialLabel = Component.literal(originalLabel.getString().toUpperCase()).withStyle(originalLabel.getStyle());

            Component message = this.displayOnlyValue ? initialLabel : CommonComponents.optionNameValue(name, initialLabel);

            return new DarkWorldCycleButton<>(x, y, width, height, message, name,
                    this.initialIndex, initial,
                    this.values, this.valueStringifier,
                    this.narrationProvider, onValueChange,
                    this.tooltipSupplier, this.displayOnlyValue,

                    this.texture, this.textureX, this.textureY,
                    this.textureWidth, this.textureHeight,
                    this.stateHeight, this.stateCount,
                    this.borderX, this.borderY
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface ValueListSupplier<T> {
        List<T> getSelectedList();
        List<T> getDefaultList();

        static <T> ValueListSupplier<T> create(Collection<T> values) {
            final List<T> list = ImmutableList.copyOf(values);
            return new ValueListSupplier<T>() {
                public List<T> getSelectedList() { return list; }
                public List<T> getDefaultList() { return list; }
            };
        }

        static <T> ValueListSupplier<T> create(final BooleanSupplier altListSelector, List<T> defaultList, List<T> selectedList) {
            final List<T> def = ImmutableList.copyOf(defaultList);
            final List<T> sel = ImmutableList.copyOf(selectedList);
            return new ValueListSupplier<T>() {
                public List<T> getSelectedList() {
                    return altListSelector.getAsBoolean() ? sel : def;
                }
                public List<T> getDefaultList() {
                    return def;
                }
            };
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange<T> {
        void onValueChange(DarkWorldCycleButton<T> button, T value);
    }
}