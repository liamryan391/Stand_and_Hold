package com.liamryan.standandhold.client.gui;

import com.liamryan.standandhold.client.ClientSyncedData;
import com.liamryan.standandhold.common.gui.ContainerResearchLab;
import com.liamryan.standandhold.common.network.PacketGuiAction;
import com.liamryan.standandhold.common.network.PacketSyncTileData;
import com.liamryan.standandhold.common.network.StandAndHoldNetwork;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;

public final class GuiResearchLab extends GuiContainer {
    private static final int BUTTON_NEXT_RESEARCH = 0;
    private static final int BUTTON_COMPLETE_RESEARCH = 1;

    private final ContainerResearchLab container;

    public GuiResearchLab(ContainerResearchLab container) {
        super(container);
        this.container = container;
        xSize = 220;
        ySize = 164;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(BUTTON_NEXT_RESEARCH, guiLeft + 10, guiTop + 124, 78, 20, "Next"));
        buttonList.add(new GuiButton(BUTTON_COMPLETE_RESEARCH, guiLeft + 96, guiTop + 124, 92, 20, "Complete"));
        StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_REQUEST_SYNC, container.getPos()));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_NEXT_RESEARCH) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_RESEARCH_LAB_NEXT, container.getPos()));
        } else if (button.id == BUTTON_COMPLETE_RESEARCH) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_RESEARCH_LAB_COMPLETE, container.getPos()));
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
        String targetLabel = hasLabSnapshot && !snapshot.getTargetResearchLabel().isEmpty() ? snapshot.getTargetResearchLabel() : "none";

        fontRenderer.drawString("Target: " + fontRenderer.trimStringToWidth(targetLabel, 170), 10, 30, 0x37474F);
        fontRenderer.drawString("Research progress: " + progress + "/" + required, 10, 44, 0x37474F);
        fontRenderer.drawString("Parasite samples: " + safeValue(storedSamples) + "/" + safeValue(maxStoredSamples), 10, 76, 0x37474F);
        fontRenderer.drawString("Local supplies: " + safeValue(storedSupplies) + "/" + safeValue(maxStoredSupplies), 10, 90, 0x37474F);

        int barLeft = 10;
        int barTop = 59;
        int barWidth = 176;
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
