package mekanism.client.gui.qio;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.lwjgl.glfw.GLFW;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiConfirmationDialog;
import mekanism.client.gui.element.GuiConfirmationDialog.DialogType;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiQIOFrequencySelect<CONTAINER extends Container> extends GuiMekanism<CONTAINER> {

    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private GuiTextScrollList scrollList;
    private TextFieldWidget frequencyField;
    private boolean privateMode;

    private boolean init = false;

    public GuiQIOFrequencySelect(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize -= 6;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 105, 89, 13));
        addButton(new GuiInnerScreen(this, 136, 105, 13, 13));
        addButton(scrollList = new GuiTextScrollList(this, 27, 39, 122, 42));

        addButton(publicButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 17, 60, 20, MekanismLang.PUBLIC, () -> {
            privateMode = false;
            updateButtons();
        }));
        addButton(privateButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 17, 60, 20, MekanismLang.PRIVATE, () -> {
            privateMode = true;
            updateButtons();
        }));
        addButton(setButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 119, 50, 18, MekanismLang.BUTTON_SET, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? getPrivateFrequencies().get(selection) : getPublicFrequencies().get(selection);
                setFrequency(freq.getName());
            }
            updateButtons();
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 79, getGuiTop() + 119, 50, 18, MekanismLang.BUTTON_DELETE, () -> {
            GuiConfirmationDialog.show(this, MekanismLang.FREQUENCY_DELETE_CONFIRM.translate(), () -> {
                int selection = scrollList.getSelection();
                if (selection != -1) {
                    Frequency freq = privateMode ? getPrivateFrequencies().get(selection) : getPublicFrequencies().get(selection);
                    sendRemoveFrequency(freq.getIdentity());
                    scrollList.clearSelection();
                }
                updateButtons();
            }, DialogType.DANGER);
        }));
        addButton(new GuiSlot(SlotType.NORMAL, this, 131, 119));
        addButton(new ColorButton(this, getGuiLeft() + 132, getGuiTop() + 120, 16, 16,
            () -> getFrequency() != null ? getFrequency().getColor() : null,
            () -> sendColorUpdate(0),
            () -> sendColorUpdate(1)));
        addButton(frequencyField = new TextFieldWidget(font, getGuiLeft() + 50, getGuiTop() + 107, 86, 11, ""));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        addButton(new MekanismImageButton(this, getGuiLeft() + 137, getGuiTop() + 106, 11, 12, getButtonLocation("checkmark"), () -> {
            setFrequency(frequencyField.getText());
            frequencyField.setText("");
            updateButtons();
        }));
        updateButtons();
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String s = frequencyField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        frequencyField.setText(s);
    }

    public void setFrequency(String freq) {
        if (!freq.isEmpty()) {
            sendSetFrequency(new FrequencyIdentity(freq, !privateMode));
        }
    }

    public ITextComponent getSecurity(Frequency freq) {
        if (freq.isPublic()) {
            return MekanismLang.PUBLIC.translate();
        }
        return MekanismLang.PRIVATE.translateColored(EnumColor.DARK_RED);
    }

    private void updateButtons() {
        if (getOwnerUsername() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (Frequency freq : getPrivateFrequencies()) {
                text.add(freq.getName());
            }
        } else {
            for (Frequency freq : getPublicFrequencies()) {
                text.add(freq.getName() + " (" + freq.getClientOwner() + ")");
            }
        }
        scrollList.setText(text);
        if (privateMode) {
            publicButton.active = true;
            privateButton.active = false;
        } else {
            publicButton.active = false;
            privateButton.active = true;
        }
        if (scrollList.hasSelection()) {
            Frequency freq = privateMode ? getPrivateFrequencies().get(scrollList.getSelection()) :
                                           getPublicFrequencies().get(scrollList.getSelection());
            setButton.active = getFrequency() == null || !getFrequency().equals(freq);
            deleteButton.active = getOwnerUUID().equals(freq.getOwner());
        } else {
            setButton.active = false;
            deleteButton.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        Frequency freq = getFrequency();
        if (!init && getFrequency() != null) {
            init = true;
            privateMode = freq.isPrivate();
        }
        updateButtons();
        frequencyField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateButtons();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (frequencyField.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                frequencyField.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                setFrequency(frequencyField.getText());
                frequencyField.setText("");
                return true;
            }
            return frequencyField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (frequencyField.canWrite()) {
            if (Character.isDigit(c) || Character.isLetter(c) || FrequencyManager.SPECIAL_CHARS.contains(c)) {
                //Only allow a subset of characters to be entered into the frequency text box
                return frequencyField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(MekanismLang.QIO_FREQUENCY_SELECT.translate(), 5);
        drawString(OwnerDisplay.of(getOwnerUUID(), getOwnerUsername()).getTextComponent(), 8, 143, titleTextColor());
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(frequencyComponent, 32, 84, titleTextColor());
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(securityComponent, 32, 94, titleTextColor());
        Frequency frequency = getFrequency();
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        if (frequency == null) {
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 84, 0x797979);
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 94, 0x797979);
        } else {
            drawTextScaledBound(frequency.getName(), 32 + frequencyOffset, 84, 0x797979, xSize - 32 - frequencyOffset - 4);
            drawString(getSecurity(frequency), 32 + getStringWidth(securityComponent), 94, 0x797979);
        }
        drawTextScaledBound(MekanismLang.SET.translate(), 27, 107, titleTextColor(), 20);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    public abstract void sendSetFrequency(FrequencyIdentity identity);
    public abstract void sendRemoveFrequency(FrequencyIdentity identity);
    public abstract void sendColorUpdate(int extra);

    public abstract QIOFrequency getFrequency();

    public abstract String getOwnerUsername();
    public abstract UUID getOwnerUUID();

    public abstract List<QIOFrequency> getPublicFrequencies();
    public abstract List<QIOFrequency> getPrivateFrequencies();
}
