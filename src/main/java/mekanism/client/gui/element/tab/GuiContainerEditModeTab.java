package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class GuiContainerEditModeTab<TILE extends TileEntityMekanism & IFluidContainerManager> extends GuiInsetElement<TILE> {

    private static final ResourceLocation BOTH = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_both.png");
    private static final ResourceLocation FILL = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_fill.png");
    private static final ResourceLocation EMPTY = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_empty.png");

    public GuiContainerEditModeTab(IGuiWrapper gui, TILE tile) {
        super(BOTH, gui, tile, gui.getWidth(), 138, 26, 18, false);
    }

    @Override
    protected ResourceLocation getOverlay() {
        return switch (dataSource.getContainerEditMode()) {
            case FILL -> FILL;
            case EMPTY -> EMPTY;
            default -> super.getOverlay();
        };
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        displayTooltips(guiGraphics, mouseX, mouseY, dataSource.getContainerEditMode().getTextComponent());
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? GuiInteraction.NEXT_MODE : GuiInteraction.PREVIOUS_MODE, dataSource));
    }

    @Override
    public boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_CONTAINER_EDIT_MODE);
    }
}
