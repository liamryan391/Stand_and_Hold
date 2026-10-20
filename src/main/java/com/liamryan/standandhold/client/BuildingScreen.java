package com.liamryan.standandhold.client;

import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.common.building.BuildingMenu;
import com.liamryan.standandhold.common.building.BuildingState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.liamryan.standandhold.common.building.BuildingMenu.*;

public final class BuildingScreen extends AbstractContainerScreen<BuildingMenu> {
    private Button upgrade;
    private Button next;
    private Button pause;
    private Button deposit;
    private Button withdraw;
    private boolean sampleMode;
    public BuildingScreen(BuildingMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 300, 230); }

    @Override protected void init() {
        super.init();
        deposit = addRenderableWidget(Button.builder(Component.translatable("gui.standandhold.import"), button -> send(sampleMode ? DEPOSIT_SAMPLES : IMPORT))
                .bounds(leftPos + 8, topPos + 202, 90, 20).build());
        withdraw = addRenderableWidget(Button.builder(Component.translatable("gui.standandhold.export"), button -> send(sampleMode ? TAKE_SAMPLES : EXPORT))
                .bounds(leftPos + 102, topPos + 202, 90, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(sampleMode ? "gui.standandhold.sample_mode" : "gui.standandhold.supply_mode"), button -> {
            sampleMode = !sampleMode;
            button.setMessage(Component.translatable(sampleMode ? "gui.standandhold.sample_mode" : "gui.standandhold.supply_mode"));
            updateTransfers();
        }).bounds(leftPos + 202, topPos + 202, 90, 20).build());
        if (menu.kind() == BuildingKind.COMMAND_POST) { upgrade = button("upgrade", 202, 177, 90, UPGRADE); }
        if (menu.kind() == BuildingKind.LAB) {
            next = button("next_research", 8, 177, 184, NEXT_RESEARCH);
            pause = button("pause", 202, 177, 90, PAUSE);
        }
    }
    private Button button(String label, int x, int y, int width, int action) {
        return addRenderableWidget(Button.builder(Component.translatable("gui.standandhold." + label), button -> {
            send(action);
        }).bounds(leftPos + x, topPos + y, width, 20).build());
    }
    private void send(int action) { if (minecraft.gameMode != null) { minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action); } }
    private void updateTransfers() {
        boolean valid = menu.number(STATUS) != 7;
        deposit.setMessage(Component.translatable(sampleMode ? "gui.standandhold.deposit_samples" : "gui.standandhold.import"));
        withdraw.setMessage(Component.translatable(sampleMode ? "gui.standandhold.take_samples" : "gui.standandhold.export"));
        long batch = menu.number(BATCH);
        deposit.active = valid && (sampleMode ? menu.number(HELD_SAMPLES) > 0 && menu.number(SAMPLES) < BuildingState.MAX_SAMPLES
                : batch > 0 && menu.number(GLOBAL) >= batch && menu.number(LOCAL) <= menu.number(CAPACITY) - batch);
        withdraw.active = valid && (sampleMode ? menu.number(SAMPLES) > 0 && menu.number(SAMPLE_ROOM) > 0
                : batch > 0 && menu.number(LOCAL) >= batch && menu.number(GLOBAL) <= Long.MAX_VALUE - batch);
    }
    @Override protected void containerTick() {
        boolean valid = menu.number(STATUS) != 7;
        updateTransfers();
        if (upgrade != null) { upgrade.active = valid && menu.number(UPGRADE_STATUS) == 0; }
        if (next != null) {
            next.active = valid && menu.hasResearchOptions();
            pause.active = valid && (menu.target() != null || menu.number(STATUS) == 10);
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        super.extractBackground(graphics, mx, my, partial);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xff202724);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xff81918b);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + 21, 0xff36433d);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.text(font, title, 8, 7, 0xfff0f3f1, false);
        line(g, 28, "gui.standandhold.progression", menu.number(POINTS), menu.number(STAGE));
        line(g, 42, "gui.standandhold.global", menu.number(GLOBAL));
        line(g, 56, "gui.standandhold.local", menu.number(LOCAL), menu.number(CAPACITY));
        line(g, 70, "gui.standandhold.samples", menu.number(SAMPLES), menu.number(BATCH));
        if (menu.kind() == BuildingKind.COMMAND_POST) {
            String[] names = {"Field Camp", "Reinforced Outpost", "Military Outpost", "Fortified Base", "Main Base"};
            int level = (int) Math.clamp(menu.number(LEVEL), 1, 5);
            line(g, 88, "gui.standandhold.level", level, names[level - 1]);
            line(g, 102, "gui.standandhold.generation", menu.number(GENERATION_POINTS), menu.number(GENERATION_SUPPLIES), menu.number(GENERATION_INTERVAL) / 20);
            if (level < 5) {
                line(g, 122, "gui.standandhold.upgrade_cost", menu.number(UPGRADE_SUPPLIES), menu.number(UPGRADE_SAMPLES));
                line(g, 136, "gui.standandhold.upgrade_points", menu.number(UPGRADE_POINTS));
                wrap(g, "Research: " + menu.upgradeResearch(), 150);
            }
            wrap(g, BuildingMenu.statusText(menu.number(UPGRADE_STATUS)), 178, 184);
        } else if (menu.kind() == BuildingKind.LAB) {
            var option = menu.target();
            wrap(g, option == null ? menu.number(STATUS) == 10 ? "Target no longer defined" : "No research selected" : option.title(), 88);
            line(g, 113, "gui.standandhold.work", menu.number(WORK), menu.number(REQUIRED_WORK), menu.number(LAB_INTERVAL) / 20.0);
            if (option != null) { line(g, 127, "gui.standandhold.research_cost", option.supplies(), option.samples(), option.stage()); }
            wrap(g, BuildingMenu.statusText(menu.number(STATUS)), 146);
        }
    }
    private void line(GuiGraphicsExtractor g, int y, String key, Object... values) {
        g.text(font, font.plainSubstrByWidth(Component.translatable(key, values).getString(), 284), 8, y, 0xffedf0ed, false);
    }
    private void wrap(GuiGraphicsExtractor g, String text, int y) {
        wrap(g, text, y, 284);
    }
    private void wrap(GuiGraphicsExtractor g, String text, int y, int width) {
        var lines = font.split(Component.literal(text), width);
        for (int i = 0; i < Math.min(2, lines.size()); i++) { g.text(font, lines.get(i), 8, y + i * 10, 0xffe0d391, false); }
    }
}
