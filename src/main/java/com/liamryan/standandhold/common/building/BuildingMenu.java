package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.BuildingConfig;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public final class BuildingMenu extends AbstractContainerMenu {
    public static final int POINTS = 0, GLOBAL = 1, LOCAL = 2, STAGE = 3, LEVEL = 4, SAMPLES = 5, WORK = 6,
            TARGET = 7, STATUS = 8, CAPACITY = 9, REQUIRED_WORK = 10, GENERATION_INTERVAL = 11, GENERATION_POINTS = 12,
            GENERATION_SUPPLIES = 13, UPGRADE_POINTS = 14, UPGRADE_SUPPLIES = 15, UPGRADE_SAMPLES = 16,
            UPGRADE_STATUS = 17, BATCH = 18, UPGRADE_RESEARCH = 19, LAB_INTERVAL = 20;
    public static final int IMPORT = 0, EXPORT = 1, UPGRADE = 2, PAUSE = 3, NEXT_RESEARCH = 4;
    private final long[] numbers = new long[21];
    private final BlockPos pos;
    private final BuildingKind kind;
    private final List<Option> options;
    private final BuildingBlockEntity building;
    private final FoundationCatalog catalog;
    private final Player owner;

    public record Option(String id, String title, long supplies, int samples, int stage) { }
    public BuildingMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(BuildingRegistry.MENU.get(), id);
        owner = inventory.player;
        pos = buffer.readBlockPos(); kind = buffer.readEnum(BuildingKind.class);
        int count = buffer.readVarInt();
        if (count < 0 || count > 64) { throw new IllegalArgumentException("Invalid research menu size."); }
        List<Option> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entries.add(new Option(buffer.readUtf(128), buffer.readUtf(80), buffer.readVarLong(), buffer.readVarInt(), buffer.readVarInt()));
        }
        options = List.copyOf(entries); building = null; catalog = null;
        addSync();
    }
    public BuildingMenu(int id, Inventory inventory, BuildingBlockEntity building, FoundationCatalog catalog) {
        super(BuildingRegistry.MENU.get(), id);
        owner = inventory.player; this.building = building; this.catalog = catalog;
        pos = building.getBlockPos(); kind = building.kind();
        options = catalog.research().values().stream().map(entry -> new Option(entry.id(), entry.title(), entry.supplyCost(), entry.sampleCost(), entry.requiredStage())).toList();
        updateNumbers(); addSync();
    }
    private void addSync() {
        // Vanilla menu data packets carry signed shorts: four limbs preserve every long bit.
        addDataSlots(new ContainerData() {
            public int getCount() { return numbers.length * 4; }
            public int get(int index) { return MenuValues.limb(numbers[index / 4], index % 4); }
            public void set(int index, int value) {
                numbers[index / 4] = MenuValues.accept(numbers[index / 4], index % 4, value);
            }
        });
    }
    public long number(int field) { return numbers[field]; }
    public BuildingKind kind() { return kind; }
    public boolean hasResearchOptions() { return !options.isEmpty(); }
    public Option target() { int index = (int) numbers[TARGET]; return index >= 0 && index < options.size() ? options.get(index) : null; }
    public String upgradeResearch() {
        int index = (int) numbers[UPGRADE_RESEARCH];
        return index >= 0 && index < options.size() ? options.get(index).title() : index == -1 ? "None" : "Unknown research ID";
    }
    public static String statusText(long status) {
        return switch ((int) status) {
            case 0 -> "Ready"; case 1 -> "Completed"; case 2 -> "Required stage not reached";
            case 3 -> "Prerequisite research incomplete"; case 4 -> "Insufficient local supplies";
            case 5 -> "Insufficient samples (insertion in M5)"; case 6 -> "No research selected";
            case 7 -> "Unavailable: save/data changed; reopen"; case 8 -> "Maximum level";
            case 9 -> "Human point requirement not reached";
            case 10 -> "Target removed: select another research";
            default -> "Blocked";
        };
    }
    private static int statusCode(String reason) {
        if (reason.equals("Ready")) { return 0; }
        if (reason.equals("Completed")) { return 1; }
        if (reason.equals("Maximum level")) { return 8; }
        if (reason.contains("human points")) { return 9; }
        if (reason.contains("stage")) { return 2; }
        if (reason.contains("local supplies")) { return 4; }
        if (reason.contains("samples")) { return 5; }
        return 3;
    }
    private int optionIndex(String id) {
        for (int i = 0; i < options.size(); i++) { if (options.get(i).id().equals(id)) { return i; } }
        return -1;
    }
    private void updateNumbers() {
        if (building == null || !(building.getLevel() instanceof ServerLevel server)) { return; }
        try {
            building.requireWritable();
            if (FoundationResources.get(server.getServer()) != catalog) { throw new IllegalStateException("Definitions reloaded; reopen menu."); }
            HumanWorldData data = HumanWorldData.get(server.getServer());
            data.requireSupportedVersion();
            BuildingState state = building.state();
            numbers[POINTS] = data.points(); numbers[GLOBAL] = data.supplies(); numbers[LOCAL] = state.supplies();
            numbers[STAGE] = ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level();
            numbers[LEVEL] = state.level(); numbers[SAMPLES] = state.samples(); numbers[WORK] = state.work();
            numbers[TARGET] = optionIndex(state.target()); numbers[STATUS] = 6;
            if (!state.target().isEmpty()) {
                var entry = catalog.research().get(state.target());
                numbers[STATUS] = entry == null ? 10 : statusCode(BuildingTransactions.researchBlocked(state, data, entry, HumanProgressionConfig.thresholds()));
            }
            numbers[CAPACITY] = BuildingConfig.CAPACITY.get(); numbers[REQUIRED_WORK] = BuildingConfig.LAB_WORK.get();
            numbers[GENERATION_INTERVAL] = BuildingConfig.INTERVAL.get(); numbers[GENERATION_POINTS] = BuildingConfig.POINTS.get();
            numbers[GENERATION_SUPPLIES] = BuildingConfig.SUPPLIES.get(); numbers[BATCH] = BuildingConfig.TRANSFER.get();
            numbers[LAB_INTERVAL] = BuildingConfig.LAB_INTERVAL.get();
            var cost = building.upgradeCost();
            numbers[UPGRADE_POINTS] = cost.points(); numbers[UPGRADE_SUPPLIES] = cost.supplies(); numbers[UPGRADE_SAMPLES] = cost.samples();
            numbers[UPGRADE_STATUS] = !cost.research().isEmpty() && !catalog.research().containsKey(cost.research()) ? 3
                    : statusCode(BuildingTransactions.upgradeBlocked(state, data, cost));
            numbers[UPGRADE_RESEARCH] = cost.research().isEmpty() ? -1 : optionIndex(cost.research()) < 0 ? -2 : optionIndex(cost.research());
        } catch (IllegalArgumentException | IllegalStateException exception) { numbers[STATUS] = 7; numbers[UPGRADE_STATUS] = 7; }
    }
    @Override public void broadcastChanges() { updateNumbers(); super.broadcastChanges(); }
    @Override public boolean stillValid(Player player) {
        return building == null ? player == owner : player == owner && building.getLevel() == player.level()
                && !building.isRemoved() && player.level().hasChunkAt(pos) && player.level().getBlockEntity(pos) == building
                && building.kind() == kind && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    @Override public boolean clickMenuButton(Player player, int action) {
        if (building == null || player.containerMenu != this || player.isSpectator() || !stillValid(player)
                || !(player.level() instanceof ServerLevel server) || !server.getServer().isSameThread()
                || action < IMPORT || action > NEXT_RESEARCH) { return false; }
        try {
            building.requireWritable();
            if (FoundationResources.get(server.getServer()) != catalog) { throw new IllegalStateException("Definitions changed; close and reopen this menu."); }
            HumanWorldData data = HumanWorldData.get(server.getServer());
            data.requireSupportedVersion();
            BuildingState state = building.state();
            switch (action) {
                case IMPORT, EXPORT -> {
                    var result = BuildingTransactions.transfer(state, data.supplies(), BuildingConfig.TRANSFER.get(), BuildingConfig.CAPACITY.get(), action == IMPORT);
                    data.setSupplies(result.global()); building.change(result.local());
                }
                case UPGRADE -> {
                    if (kind != BuildingKind.COMMAND_POST) { return false; }
                    var cost = building.upgradeCost();
                    if (!cost.research().isEmpty()) { catalog.research(cost.research()); }
                    building.change(BuildingTransactions.upgrade(state, data, cost));
                }
                case PAUSE -> { if (kind != BuildingKind.LAB) { return false; } building.change(state.target("")); }
                case NEXT_RESEARCH -> {
                    if (kind != BuildingKind.LAB || options.isEmpty()) { return false; }
                    int index = (optionIndex(state.target()) + 1) % options.size();
                    building.change(state.target(options.get(index).id()));
                }
                default -> { return false; }
            }
            BuildingBlockEntity.refreshMissions(server, data);
            return true;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()));
            return false;
        }
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
