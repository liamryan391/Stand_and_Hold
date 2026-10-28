package com.liamryan.standandhold.client;

import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.common.building.BuildingMenu;
import com.liamryan.standandhold.common.building.BuildingState;
import com.liamryan.standandhold.common.building.MenuValues;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.liamryan.standandhold.common.building.BuildingMenu.*;

public final class BuildingScreen extends AbstractContainerScreen<BuildingMenu> {
    private Button upgrade;
    private Button next;
    private Button pause;
    private Button start;
    private Button deposit;
    private Button withdraw;
    private boolean sampleMode;
    private int mouseX, mouseY;
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
            start = button("start_research", 8, 177, 90, TOGGLE_RESEARCH);
            next = button("next_research", 102, 177, 90, NEXT_RESEARCH);
            pause = button("pause", 202, 177, 90, PAUSE);
        }
    }
    private Button button(String label, int x, int y, int width, int action) {
        var result = addRenderableWidget(Button.builder(Component.translatable("gui.standandhold." + label), button -> {
            send(action);
        }).bounds(leftPos + x, topPos + y, width, 20).build());
        result.setTooltip(Tooltip.create(Component.translatable("gui.standandhold.tip." + label)));
        return result;
    }
    private void send(int action) { if (minecraft.gameMode != null) { minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action); } }
    private void updateTransfers() {
        boolean valid = menu.number(STATUS) != 7;
        deposit.setMessage(Component.translatable(sampleMode ? "gui.standandhold.deposit_samples" : "gui.standandhold.import"));
        withdraw.setMessage(Component.translatable(sampleMode ? "gui.standandhold.take_samples" : "gui.standandhold.export"));
        long batch = menu.number(BATCH);
        deposit.setTooltip(Tooltip.create(Component.translatable(sampleMode ? "gui.standandhold.tip.store" : "gui.standandhold.tip.import", batch)));
        withdraw.setTooltip(Tooltip.create(Component.translatable(sampleMode ? "gui.standandhold.tip.take" : "gui.standandhold.tip.export", batch)));
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
            boolean running = menu.number(RUNNING) != 0;
            start.setMessage(Component.translatable(running ? "gui.standandhold.pause_research" : "gui.standandhold.start_research"));
            start.setTooltip(Tooltip.create(Component.translatable(running ? "gui.standandhold.tip.pause_research" : "gui.standandhold.tip.start_research")));
            start.active = valid && menu.target() != null && (running || menu.number(STATUS) == 0);
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        super.extractBackground(graphics, mx, my, partial);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xff20272b);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xff81918b);
        int header = switch (menu.kind()) { case COMMAND_POST -> 0xff43523d; case LAB -> 0xff244d55; case CRATE -> 0xff645744; };
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + 21, header);
        for (int y : new int[]{24, 84, 174, 199}) { graphics.fill(leftPos + 8, topPos + y, leftPos + 292, topPos + y + 1, 0xff46515a); }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        mouseX = mx; mouseY = my;
        text(g, title.getString(), 7, 284, 0xfff0f3f1);
        line(g, 28, "gui.standandhold.progression", menu.number(POINTS), menu.number(STAGE));
        line(g, 42, "gui.standandhold.global", menu.number(GLOBAL));
        line(g, 56, "gui.standandhold.local", menu.number(LOCAL), menu.number(CAPACITY));
        line(g, 70, "gui.standandhold.samples", menu.number(SAMPLES), menu.number(BATCH));
        if (menu.kind() == BuildingKind.COMMAND_POST) {
            int level = (int) Math.clamp(menu.number(LEVEL), 1, 5);
            line(g, 88, "gui.standandhold.level", level, Component.translatable("gui.standandhold.level." + level));
            line(g, 100, "gui.standandhold.generation", menu.number(GENERATION_POINTS), menu.number(GENERATION_SUPPLIES), menu.number(GENERATION_INTERVAL) / 20);
            line(g, 112, "gui.standandhold.defenders", menu.number(DEFENDERS), menu.number(DEFENDER_CAP));
            line(g, 124, "gui.standandhold.deployment", Component.translatable("gui.standandhold.deployment." + menu.number(DEPLOYMENT_STATUS)));
            if (level < 5) {
                line(g, 138, "gui.standandhold.upgrade_cost", menu.number(UPGRADE_SUPPLIES), menu.number(UPGRADE_SAMPLES));
                line(g, 150, "gui.standandhold.upgrade_points", menu.number(UPGRADE_POINTS));
                line(g, 162, "gui.standandhold.required_research", menu.upgradeResearch());
            }
            wrap(g, status(menu.number(UPGRADE_STATUS)), 178, 184);
        } else if (menu.kind() == BuildingKind.LAB) {
            var option = menu.target();
            wrap(g, option == null ? status(menu.number(STATUS)) : option.title(), 88);
            boolean completed = menu.number(STATUS) == 1;
            long work = completed ? menu.number(REQUIRED_WORK) : menu.number(WORK);
            if (completed) { line(g, 113, "gui.standandhold.work_complete", work, menu.number(REQUIRED_WORK)); }
            else { line(g, 113, "gui.standandhold.work", work, menu.number(REQUIRED_WORK),
                    Math.max(0, menu.number(REQUIRED_WORK) - work), menu.number(LAB_INTERVAL) / 20.0); }
            if (option != null) { line(g, 127, "gui.standandhold.research_cost", option.supplies(), option.samples(), option.stage()); }
            String status = menu.number(STATUS) == 0 ? Component.translatable(menu.number(RUNNING) != 0 ? "gui.standandhold.researching"
                    : menu.number(WORK) > 0 ? "gui.standandhold.paused" : "gui.standandhold.ready_to_start").getString() : status(menu.number(STATUS));
            wrap(g, status, 141);
            g.fill(8, 164, 292, 171, 0xff101916);
            int progress = MenuValues.progressWidth(work, menu.number(REQUIRED_WORK), 282);
            g.fill(9, 165, 9 + progress, 170, menu.number(STATUS) == 1 ? 0xff9db85a : 0xff4caba5);
            g.outline(8, 164, 284, 7, 0xff81918b);
        }
    }
    private void line(GuiGraphicsExtractor g, int y, String key, Object... values) {
        text(g, Component.translatable(key, values).getString(), y, 284, 0xffedf0ed);
    }
    private static String status(long code) { return Component.translatable("gui.standandhold.status." + Math.clamp(code, 0, 10)).getString(); }
    private void text(GuiGraphicsExtractor g, String full, int y, int width, int colour) {
        String fitted = GuiTextLayout.fit(full, width, font::width);
        g.text(font, fitted, 8, y, colour, false);
        if (!fitted.equals(full)) { tooltip(g, full, y, width, 10); }
    }
    private void tooltip(GuiGraphicsExtractor g, String full, int y, int width, int height) {
        if (mouseX >= leftPos + 8 && mouseX < leftPos + 8 + width && mouseY >= topPos + y && mouseY < topPos + y + height) {
            g.setTooltipForNextFrame(Component.literal(full), mouseX, mouseY);
        }
    }
    private void wrap(GuiGraphicsExtractor g, String text, int y) {
        wrap(g, text, y, 284);
    }
    private void wrap(GuiGraphicsExtractor g, String text, int y, int width) {
        var lines = GuiTextLayout.lines(text, width, 2, font::width);
        for (int i = 0; i < lines.size(); i++) { g.text(font, lines.get(i), 8, y + i * 10, 0xffe0d391, false); }
        if (!String.join(" ", lines).equals(text)) { tooltip(g, text, y, width, 20); }
    }
}
