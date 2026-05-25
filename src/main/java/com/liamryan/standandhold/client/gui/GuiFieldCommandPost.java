package com.liamryan.standandhold.client.gui;

import com.liamryan.standandhold.common.gui.ContainerFieldCommandPost;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.progression.HumanStage;
import net.minecraft.client.gui.inventory.GuiContainer;

public final class GuiFieldCommandPost extends GuiContainer {
    private final ContainerFieldCommandPost container;

    public GuiFieldCommandPost(ContainerFieldCommandPost container) {
        super(container);
        this.container = container;
        xSize = 196;
        ySize = 128;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString("Field Command Post", 10, 10, 0x263238);
        int stageId = Math.max(0, container.getStageId());
        HumanStage stage = HumanStage.byId(stageId);
        FieldCommandPostLevel level = FieldCommandPostLevel.byLevel(Math.max(1, container.getUpgradeLevel()));
        fontRenderer.drawString("Human points: " + safeValue(container.getHumanPoints()), 10, 30, 0x37474F);
        fontRenderer.drawString("Stage " + stage.getId() + ": " + stage.getDisplayName(), 10, 44, 0x37474F);
        fontRenderer.drawString("Level " + level.getLevel() + ": " + level.getDisplayName(), 10, 58, 0x37474F);
        fontRenderer.drawString("Supplies: " + safeValue(container.getSupplyPoints()), 10, 72, 0x37474F);
        fontRenderer.drawString("Local stockpile: " + safeValue(container.getStoredSupplies()) + "/" + safeValue(container.getMaxStoredSupplies()), 10, 86, 0x37474F);
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
