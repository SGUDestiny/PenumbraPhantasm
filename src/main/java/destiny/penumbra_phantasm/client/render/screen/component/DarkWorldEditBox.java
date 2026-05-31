package destiny.penumbra_phantasm.client.render.screen.component;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DarkWorldEditBox extends AbstractWidget implements Renderable {
    public static final int BACKWARDS = -1;
    public static final int FORWARDS = 1;
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    public static final int DEFAULT_TEXT_COLOR = 14737632;
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = -16777216;
    private final Font font;
    private String value;
    private int maxLength;
    private int frame;
    private boolean bordered;
    private boolean canLoseFocus;
    private boolean isEditable;
    private boolean shiftPressed;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor;
    private int textColorUneditable;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter;
    private BiFunction<String, Integer, FormattedCharSequence> formatter;
    @Nullable
    private Component hint;

    public DarkWorldEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        this(pFont, pX, pY, pWidth, pHeight, null, pMessage);
    }

    public DarkWorldEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, @Nullable DarkWorldEditBox pEditBox, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.value = "";
        this.maxLength = 32;
        this.bordered = true;
        this.canLoseFocus = true;
        this.isEditable = true;
        this.textColor = 14737632;
        this.textColorUneditable = 7368816;
        this.filter = Objects::nonNull;
        this.formatter = (p_94147_, p_94148_) -> FormattedCharSequence.forward(p_94147_, Style.EMPTY);
        this.font = pFont;
        if (pEditBox != null) {
            this.setValue(pEditBox.getValue());
        }
    }

    public void setResponder(Consumer<String> pResponder) {
        this.responder = pResponder;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> pTextFormatter) {
        this.formatter = pTextFormatter;
    }

    public void tick() {
        ++this.frame;
    }

    protected MutableComponent createNarrationMessage() {
        Component $$0 = this.getMessage();
        return Component.translatable("gui.narrate.editBox", new Object[]{$$0, this.value});
    }

    public void setValue(String pText) {
        if (this.filter.test(pText)) {
            if (pText.length() > this.maxLength) {
                this.value = pText.substring(0, this.maxLength);
            } else {
                this.value = pText;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(pText);
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getHighlighted() {
        int $$0 = Math.min(this.cursorPos, this.highlightPos);
        int $$1 = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring($$0, $$1);
    }

    public void setFilter(Predicate<String> pValidator) {
        this.filter = pValidator;
    }

    public void insertText(String pTextToWrite) {
        int $$1 = Math.min(this.cursorPos, this.highlightPos);
        int $$2 = Math.max(this.cursorPos, this.highlightPos);
        int $$3 = this.maxLength - this.value.length() - ($$1 - $$2);
        String $$4 = SharedConstants.filterText(pTextToWrite);
        int $$5 = $$4.length();
        if ($$3 < $$5) {
            $$4 = $$4.substring(0, $$3);
            $$5 = $$3;
        }

        String $$6 = (new StringBuilder(this.value)).replace($$1, $$2, $$4).toString();
        if (this.filter.test($$6)) {
            this.value = $$6;
            this.setCursorPosition($$1 + $$5);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String pNewText) {
        if (this.responder != null) {
            this.responder.accept(pNewText);
        }

    }

    private void deleteText(int pCount) {
        if (Screen.hasControlDown()) {
            this.deleteWords(pCount);
        } else {
            this.deleteChars(pCount);
        }

    }

    public void deleteWords(int pNum) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(pNum) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int pNum) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int $$1 = this.getCursorPos(pNum);
                int $$2 = Math.min($$1, this.cursorPos);
                int $$3 = Math.max($$1, this.cursorPos);
                if ($$2 != $$3) {
                    String $$4 = (new StringBuilder(this.value)).delete($$2, $$3).toString();
                    if (this.filter.test($$4)) {
                        this.value = $$4;
                        this.moveCursorTo($$2);
                    }
                }
            }
        }
    }

    public int getWordPosition(int pNumWords) {
        return this.getWordPosition(pNumWords, this.getCursorPosition());
    }

    private int getWordPosition(int pN, int pPos) {
        return this.getWordPosition(pN, pPos, true);
    }

    private int getWordPosition(int pN, int pPos, boolean pSkipWs) {
        int $$3 = pPos;
        boolean $$4 = pN < 0;
        int $$5 = Math.abs(pN);

        for(int $$6 = 0; $$6 < $$5; ++$$6) {
            if (!$$4) {
                int $$7 = this.value.length();
                $$3 = this.value.indexOf(32, $$3);
                if ($$3 == -1) {
                    $$3 = $$7;
                } else {
                    while(pSkipWs && $$3 < $$7 && this.value.charAt($$3) == ' ') {
                        ++$$3;
                    }
                }
            } else {
                while(pSkipWs && $$3 > 0 && this.value.charAt($$3 - 1) == ' ') {
                    --$$3;
                }

                while($$3 > 0 && this.value.charAt($$3 - 1) != ' ') {
                    --$$3;
                }
            }
        }

        return $$3;
    }

    public void moveCursor(int pDelta) {
        this.moveCursorTo(this.getCursorPos(pDelta));
    }

    private int getCursorPos(int pDelta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, pDelta);
    }

    public void moveCursorTo(int pPos) {
        this.setCursorPosition(pPos);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int pPos) {
        this.cursorPos = Mth.clamp(pPos, 0, this.value.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(pKeyCode)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(pKeyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(pKeyCode)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(pKeyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {
                switch (pKeyCode) {
                    case 259:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(pCodePoint)) {
            if (this.isEditable) {
                this.insertText(Character.toString(pCodePoint));
            }

            return true;
        } else {
            return false;
        }
    }

    public void onClick(double pMouseX, double pMouseY) {
        int $$2 = Mth.floor(pMouseX) - this.getX();
        if (this.bordered) {
            $$2 -= 4;
        }

        String $$3 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        this.moveCursorTo(this.font.plainSubstrByWidth($$3, $$2).length() + this.displayPos);
    }

    public void playDownSound(SoundManager pHandler) {
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                int $$4 = -1;
                pGuiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, $$4);
                pGuiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
            }

            int $$5 = this.isEditable ? this.textColor : this.textColorUneditable;
            int $$6 = this.cursorPos - this.displayPos;
            int $$7 = this.highlightPos - this.displayPos;
            String $$8 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean $$9 = $$6 >= 0 && $$6 <= $$8.length();
            boolean $$10 = this.isFocused() && this.frame / 6 % 2 == 0 && $$9;
            int $$11 = this.bordered ? this.getX() + 4 : this.getX();
            int $$12 = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int $$13 = $$11;
            if ($$7 > $$8.length()) {
                $$7 = $$8.length();
            }

            if (!$$8.isEmpty()) {
                String $$14 = $$9 ? $$8.substring(0, $$6) : $$8;
                $$13 = pGuiGraphics.drawString(this.font, this.formatter.apply($$14, this.displayPos), $$11, $$12, $$5);
            }

            boolean $$15 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int $$16 = $$13;
            if (!$$9) {
                $$16 = $$6 > 0 ? $$11 + this.width : $$11;
            } else if ($$15) {
                $$16 = $$13 - 1;
                --$$13;
            }

            if (!$$8.isEmpty() && $$9 && $$6 < $$8.length()) {
                pGuiGraphics.drawString(this.font, this.formatter.apply($$8.substring($$6), this.cursorPos), $$13, $$12, $$5);
            }

            if (this.hint != null && $$8.isEmpty() && !this.isFocused()) {
                pGuiGraphics.drawString(this.font, this.hint, $$13, $$12, $$5);
            }

            if (!$$15 && this.suggestion != null) {
                pGuiGraphics.drawString(this.font, this.suggestion, $$16 - 1, $$12, -8355712);
            }

            if ($$10) {
                if ($$15) {
                    RenderType var10001 = RenderType.guiOverlay();
                    int var10003 = $$12 - 1;
                    int var10004 = $$16 + 1;
                    int var10005 = $$12 + 1;
                    Objects.requireNonNull(this.font);
                    pGuiGraphics.fill(var10001, $$16, var10003, var10004, var10005 + 9, -3092272);
                } else {
                    pGuiGraphics.drawString(this.font, "_", $$16, $$12, $$5);
                }
            }

            if ($$7 != $$6) {
                int $$17 = $$11 + this.font.width($$8.substring(0, $$7));
                int var19 = $$12 - 1;
                int var20 = $$17 - 1;
                int var21 = $$12 + 1;
                Objects.requireNonNull(this.font);
                this.renderHighlight(pGuiGraphics, $$16, var19, var20, var21 + 9);
            }

        }
    }

    private void renderHighlight(GuiGraphics pGuiGraphics, int pMinX, int pMinY, int pMaxX, int pMaxY) {
        if (pMinX < pMaxX) {
            int $$5 = pMinX;
            pMinX = pMaxX;
            pMaxX = $$5;
        }

        if (pMinY < pMaxY) {
            int $$6 = pMinY;
            pMinY = pMaxY;
            pMaxY = $$6;
        }

        if (pMaxX > this.getX() + this.width) {
            pMaxX = this.getX() + this.width;
        }

        if (pMinX > this.getX() + this.width) {
            pMinX = this.getX() + this.width;
        }

        pGuiGraphics.fill(RenderType.guiTextHighlight(), pMinX, pMinY, pMaxX, pMaxY, -16776961);
    }

    public void setMaxLength(int pLength) {
        this.maxLength = pLength;
        if (this.value.length() > pLength) {
            this.value = this.value.substring(0, pLength);
            this.onValueChange(this.value);
        }

    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    private boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean pEnableBackgroundDrawing) {
        this.bordered = pEnableBackgroundDrawing;
    }

    public void setTextColor(int pColor) {
        this.textColor = pColor;
    }

    public void setTextColorUneditable(int pColor) {
        this.textColorUneditable = pColor;
    }

    @javax.annotation.Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
        return this.visible && this.isEditable ? super.nextFocusPath(pEvent) : null;
    }

    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.visible && pMouseX >= (double)this.getX() && pMouseX < (double)(this.getX() + this.width) && pMouseY >= (double)this.getY() && pMouseY < (double)(this.getY() + this.height);
    }

    public void setFocused(boolean pFocused) {
        if (this.canLoseFocus || pFocused) {
            super.setFocused(pFocused);
            if (pFocused) {
                this.frame = 0;
            }

        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean pEnabled) {
        this.isEditable = pEnabled;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(int pPosition) {
        int $$1 = this.value.length();
        this.highlightPos = Mth.clamp(pPosition, 0, $$1);
        if (this.font != null) {
            if (this.displayPos > $$1) {
                this.displayPos = $$1;
            }

            int $$2 = this.getInnerWidth();
            String $$3 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), $$2);
            int $$4 = $$3.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, $$2, true).length();
            }

            if (this.highlightPos > $$4) {
                this.displayPos += this.highlightPos - $$4;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, $$1);
        }

    }

    public void setCanLoseFocus(boolean pCanLoseFocus) {
        this.canLoseFocus = pCanLoseFocus;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean pIsVisible) {
        this.visible = pIsVisible;
    }

    public void setSuggestion(@Nullable String pSuggestion) {
        this.suggestion = pSuggestion;
    }

    public int getScreenX(int pCharNum) {
        return pCharNum > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, pCharNum));
    }

    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }

    public void setHint(Component pHint) {
        this.hint = pHint;
    }
}
