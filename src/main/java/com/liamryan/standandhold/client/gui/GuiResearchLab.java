package com.liamryan.standandhold.client.gui;

import com.liamryan.standandhold.client.ClientSyncedData;
import com.liamryan.standandhold.common.gui.ContainerResearchLab;
import com.liamryan.standandhold.common.network.PacketGuiAction;
import com.liamryan.standandhold.common.network.PacketSyncTileData;
import com.liamryan.standandhold.common.network.StandAndHoldNetwork;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;

public final class GuiResearchLab extends GuiContainer {
    private static final int BUTTON_PREVIOUS_RESEARCH = 0;
    private static final int BUTTON_NEXT_RESEARCH = 1;
    private static final int BUTTON_COMPLETE_RESEARCH = 2;
    private static final int BUTTON_IMPORT_SUPPLIES = 3;
    private static final int BUTTON_EXPORT_SUPPLIES = 4;
    private static final int BUTTON_CREATIVE_COMPLETE = 5;

    private final ContainerResearchLab container;

    public GuiResearchLab(ContainerResearchLab container) {
        super(container);
        this.container = container;
        xSize = 236;
        ySize = 224;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(BUTTON_PREVIOUS_RESEARCH, guiLeft + 10, guiTop + 150, 58, 20, "Back"));
        buttonList.add(new GuiButton(BUTTON_NEXT_RESEARCH, guiLeft + 72, guiTop + 150, 58, 20, "Next"));
        buttonList.add(new GuiButton(BUTTON_COMPLETE_RESEARCH, guiLeft + 134, guiTop + 150, 92, 20, "Complete"));
        buttonList.add(new GuiButton(BUTTON_IMPORT_SUPPLIES, guiLeft + 10, guiTop + 176, 68, 20, "Import"));
        buttonList.add(new GuiButton(BUTTON_EXPORT_SUPPLIES, guiLeft + 82, guiTop + 176, 68, 20, "Export"));
        buttonList.add(new GuiButton(BUTTON_CREATIVE_COMPLETE, guiLeft + 154, guiTop + 176, 72, 20, "Creative"));
        StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_REQUEST_SYNC, container.getPos()));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_PREVIOUS_RESEARCH) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_RESEARCH_LAB_PREVIOUS, container.getPos()));
        } else if (button.id == BUTTON_NEXT_RESEARCH) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_RESEARCH_LAB_NEXT, container.getPos()));
        } else if (button.id == BUTTON_COMPLETE_RESEARCH) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_RESEARCH_LAB_COMPLETE, container.getPos()));
        } else if (button.id == BUTTON_CREATIVE_COMPLETE) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_RESEARCH_LAB_CREATIVE_COMPLETE, container.getPos()));
        } else if (button.id == BUTTON_IMPORT_SUPPLIES) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_SUPPLIES_TO_LOCAL, container.getPos()));
        } else if (button.id == BUTTON_EXPORT_SUPPLIES) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_SUPPLIES_TO_GLOBAL, container.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString("Research Lab", 10, 10, 0x263238);
        ClientSyncedData.TileSnapshot snapshot = ClientSyncedData.getTileData(container.getPos());
        boolean hasLabSnapshot = snapshot != null && snapshot.getTypeId() == PacketSyncTileData.TYPE_RESEARCH_LAB;
        int progress = safeValue(hasLabSnapshot ? snapshot.getResearchProgress() : container.getProgress());
        int required = Math.max(1, hasLabSnapshot ? snapshot.getResearchProgressRequired() : container.getProgressRequired());
        int storedSamples = hasLabSnapshot ? snapshot.getStoredSamples() : container.getStoredSamples();
        int maxStoredSamples = hasLabSnapshot ? snapshot.getMaxStoredSamples() : container.getMaxStoredSamples();
        int storedSupplies = hasLabSnapshot ? snapshot.getStoredSupplies() : container.getStoredSupplies();
        int maxStoredSupplies = hasLabSnapshot ? snapshot.getMaxStoredSupplies() : container.getMaxStoredSupplies();
        int targetSampleCost = hasLabSnapshot ? snapshot.getTargetSampleCost() : 0;
        int targetSupplyCost = hasLabSnapshot ? snapshot.getTargetSupplyCost() : 0;
        String targetId = hasLabSnapshot && !snapshot.getTargetResearchId().isEmpty() ? snapshot.getTargetResearchId() : "none";
        String targetLabel = hasLabSnapshot && !snapshot.getTargetResearchLabel().isEmpty() ? snapshot.getTargetResearchLabel() : "none";
        int globalSupplies = safeValue(ClientSyncedData.getSupplyPoints(0));

        drawTrimmed("Target: " + targetLabel, 10, 28, 214, 0x37474F);
        drawTrimmed("ID: " + targetId, 10, 40, 214, 0x546E7A);
        fontRenderer.drawString("Progress: " + progress + "/" + required + " (" + getPercent(progress, required) + "%)", 10, 54, 0x37474F);
        fontRenderer.drawString("Needs: " + safeValue(targetSampleCost) + " samples, " + safeValue(targetSupplyCost) + " supplies", 10, 82, 0x37474F);
        fontRenderer.drawString("Stored samples: " + safeValue(storedSamples) + "/" + safeValue(maxStoredSamples), 10, 96, 0x37474F);
        fontRenderer.drawString("Local supplies: " + safeValue(storedSupplies) + "/" + safeValue(maxStoredSupplies), 10, 110, 0x37474F);
        fontRenderer.drawString("Global supplies: " + globalSupplies, 10, 124, 0x37474F);
        drawTrimmed("Status: " + getStatusLabel(targetId, progress, required, storedSamples, targetSampleCost, globalSupplies, targetSupplyCost), 10, 136, 214, 0x455A64);

        int barLeft = 10;
        int barTop = 67;
        int barWidth = 200;
        int filledWidth = required <= 0 ? 0 : Math.min(barWidth, Math.max(0, (int) ((long) progress * barWidth / required)));
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

    private int getPercent(int progress, int required) {
        return required <= 0 ? 0 : Math.min(100, Math.max(0, (int) ((long) safeValue(progress) * 100L / required)));
    }

    private String getStatusLabel(String targetId, int progress, int required, int storedSamples, int sampleCost, int globalSupplies, int supplyCost) {
        if (targetId == null || targetId.isEmpty() || "none".equals(targetId)) {
            return "No ready target; complete prerequisites or choose later.";
        }

        if (progress < required) {
            return "Researching selected target. Creative bypass is test-only.";
        }

        if (safeValue(storedSamples) < safeValue(sampleCost)) {
            return "Needs samples before completion.";
        }

        if (safeValue(globalSupplies) < safeValue(supplyCost)) {
            return "Needs global supplies before completion.";
        }

        return "Ready to complete.";
    }

    private void drawTrimmed(String text, int x, int y, int width, int color) {
        fontRenderer.drawString(fontRenderer.trimStringToWidth(text, width), x, y, color);
    }
}
