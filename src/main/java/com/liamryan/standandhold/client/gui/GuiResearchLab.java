package com.liamryan.standandhold.client.gui;

import com.liamryan.standandhold.common.gui.ContainerResearchLab;
import net.minecraft.client.gui.inventory.GuiContainer;

public final class GuiResearchLab extends GuiContainer {
    private final ContainerResearchLab container;

    public GuiResearchLab(ContainerResearchLab container) {
        super(container);
        this.container = container;
        xSize = 196;
        ySize = 132;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString("Research Lab", 10, 10, 0x263238);
        int progress = safeValue(container.getProgress());
        int required = Math.max(1, container.getProgressRequired());
        fontRenderer.drawString("Research progress: " + progress + "/" + required, 10, 30, 0x37474F);
        fontRenderer.drawString("Parasite samples: " + safeValue(container.getStoredSamples()) + "/" + safeValue(container.getMaxStoredSamples()), 10, 62, 0x37474F);
        fontRenderer.drawString("Local supplies: " + safeValue(container.getStoredSupplies()) + "/" + safeValue(container.getMaxStoredSupplies()), 10, 76, 0x37474F);

        int barLeft = 10;
        int barTop = 45;
        int barWidth = 160;
        int filledWidth = Math.min(barWidth, Math.max(0, (int) ((long) progress * barWidth / required)));
        drawRect(barLeft, barTop, barLeft + barWidth, barTop + 10, 0xFFCFD8DC);
        drawRect(barLeft, barTop, barLeft + filledWidth, barTop + 10, 0xFF26A69A);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        int left = guiLeft;
        int top = guiTop;
        drawRect(left, top, left + xSize, top + ySize, 0xFFECEFF1);
        drawRect(left, top, left + xSize, top + 22, 0xFFB0BEC5);
        drawRect(left, top + ySize - 4, left + xSize, top + ySize, 0xFF78909C);
    }

    private int safeValue(int value) {
        return Math.max(0, value);
    }
}
