package com.liamryan.standandhold.client.gui;

import com.liamryan.standandhold.client.ClientSyncedData;
import com.liamryan.standandhold.common.gui.ContainerFieldCommandPost;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.network.PacketGuiAction;
import com.liamryan.standandhold.common.network.PacketSyncTileData;
import com.liamryan.standandhold.common.network.StandAndHoldNetwork;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;

public final class GuiFieldCommandPost extends GuiContainer {
    private static final int BUTTON_UPGRADE = 0;
    private static final int BUTTON_IMPORT_SUPPLIES = 1;
    private static final int BUTTON_EXPORT_SUPPLIES = 2;

    private final ContainerFieldCommandPost container;

    public GuiFieldCommandPost(ContainerFieldCommandPost container) {
        super(container);
        this.container = container;
        xSize = 220;
        ySize = 184;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(BUTTON_UPGRADE, guiLeft + 10, guiTop + 132, 62, 20, "Upgrade"));
        buttonList.add(new GuiButton(BUTTON_IMPORT_SUPPLIES, guiLeft + 78, guiTop + 132, 62, 20, "Import"));
        buttonList.add(new GuiButton(BUTTON_EXPORT_SUPPLIES, guiLeft + 146, guiTop + 132, 62, 20, "Export"));
        StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_REQUEST_SYNC, container.getPos()));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_UPGRADE) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_COMMAND_POST_UPGRADE, container.getPos()));
        } else if (button.id == BUTTON_IMPORT_SUPPLIES) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_SUPPLIES_TO_LOCAL, container.getPos()));
        } else if (button.id == BUTTON_EXPORT_SUPPLIES) {
            StandAndHoldNetwork.sendToServer(new PacketGuiAction(PacketGuiAction.ACTION_SUPPLIES_TO_GLOBAL, container.getPos()));
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
        fontRenderer.drawString("Global supplies: " + safeValue(ClientSyncedData.getSupplyPoints(container.getSupplyPoints())), 10, 72, 0x37474F);
        fontRenderer.drawString("Local stockpile: " + safeValue(storedSupplies) + "/" + safeValue(maxStoredSupplies), 10, 86, 0x37474F);
        fontRenderer.drawString("Point gen: +" + getPointGenerationAmount() + " / " + getSeconds(StandAndHoldConfig.infrastructure.fieldCommandPostTickInterval) + "s", 10, 100, 0x455A64);
        fontRenderer.drawString("Defenders: max " + getMaxDefenders() + " / " + getSeconds(StandAndHoldConfig.infrastructure.outpostDefenderSpawnInterval) + "s", 10, 114, 0x455A64);
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

    private int getPointGenerationAmount() {
        return StandAndHoldConfig.infrastructure.enableFieldCommandPostPointGeneration ? Math.max(0, StandAndHoldConfig.infrastructure.fieldCommandPostPointsPerInterval) : 0;
    }

    private int getMaxDefenders() {
        return StandAndHoldConfig.infrastructure.enableOutpostDefenderSpawning ? Math.max(0, StandAndHoldConfig.infrastructure.outpostMaxDefenders) : 0;
    }

    private int getSeconds(int ticks) {
        return Math.max(1, (Math.max(1, ticks) + 19) / 20);
    }
}
