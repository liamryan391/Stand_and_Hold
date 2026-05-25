package com.liamryan.standandhold.client.gui;

import com.liamryan.standandhold.client.ClientSyncedData;
import com.liamryan.standandhold.common.gui.ContainerFieldCommandPost;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.network.PacketGuiAction;
import com.liamryan.standandhold.common.network.PacketSyncTileData;
import com.liamryan.standandhold.common.network.StandAndHoldNetwork;
import com.liamryan.standandhold.common.progression.HumanStage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;

public final class GuiFieldCommandPost extends GuiContainer {
    private static final int BUTTON_UPGRADE = 0;

    private final ContainerFieldCommandPost container;

    public GuiFieldCommandPost(ContainerFieldCommandPost container) {
        super(container);
        this.container = container;
        xSize = 210;
        ySize = 152;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(BUTTON_UPGRADE, guiLeft + 10, guiTop + 110, 82, 20, "Upgrade"));
        StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_REQUEST_SYNC, container.getPos()));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_UPGRADE) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_COMMAND_POST_UPGRADE, container.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString("Field Command Post", 10, 10, 0x263238);
        ClientSyncedData.TileSnapshot snapshot = ClientSyncedData.getTileData(container.getPos());
        int stageId = Math.max(0, ClientSyncedData.getStageId(container.getStageId()));
        int upgradeLevel = snapshot != null && snapshot.getTypeId() == PacketSyncTileData.TYPE_FIELD_COMMAND_POST ? snapshot.getUpgradeLevel() : container.getUpgradeLevel();
        int storedSupplies = snapshot != null && snapshot.getTypeId() == PacketSyncTileData.TYPE_FIELD_COMMAND_POST ? snapshot.getStoredSupplies() : container.getStoredSupplies();
        int maxStoredSupplies = snapshot != null && snapshot.getTypeId() == PacketSyncTileData.TYPE_FIELD_COMMAND_POST ? snapshot.getMaxStoredSupplies() : container.getMaxStoredSupplies();
        HumanStage stage = HumanStage.byId(stageId);
        FieldCommandPostLevel level = FieldCommandPostLevel.byLevel(Math.max(1, upgradeLevel));
        fontRenderer.drawString("Human points: " + safeValue(ClientSyncedData.getHumanPoints(container.getHumanPoints())), 10, 30, 0x37474F);
        fontRenderer.drawString("Stage " + stage.getId() + ": " + stage.getDisplayName(), 10, 44, 0x37474F);
        fontRenderer.drawString("Level " + level.getLevel() + ": " + level.getDisplayName(), 10, 58, 0x37474F);
        fontRenderer.drawString("Supplies: " + safeValue(ClientSyncedData.getSupplyPoints(container.getSupplyPoints())), 10, 72, 0x37474F);
        fontRenderer.drawString("Local stockpile: " + safeValue(storedSupplies) + "/" + safeValue(maxStoredSupplies), 10, 86, 0x37474F);
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
