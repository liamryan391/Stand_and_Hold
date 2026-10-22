package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.foundation.FoundationAmounts;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.sample.SampleItems;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.BuildingConfig;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.StructureConfig;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.npc.DefenderDeployment;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.config.ResponseConfig;
import com.liamryan.standandhold.common.threat.ThreatResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BuildingBlockEntity extends BlockEntity implements MenuProvider {
    private BuildingState state = BuildingState.EMPTY;
    private int elapsed;
    private int deploymentElapsed;
    private int responseElapsed;
    private int schema = 1;
    private Dynamic<?> retainedState;
    private boolean malformed;
    private boolean removalHandled;
    private boolean reportedFailure;
    private boolean mainBase;
    private int mainBaseNotices;

    public BuildingBlockEntity(BlockPos pos, BlockState block) { super(BuildingRegistry.ENTITY.get(), pos, block); }
    public BuildingKind kind() { return ((BuildingBlock) getBlockState().getBlock()).kind(); }
    public BuildingState state() { return state; }
    public boolean isMainBase() { return mainBase && kind() == BuildingKind.COMMAND_POST; }
    public void initializeMainBase() { requireWritable(); if (!mainBase) { mainBase = true; setChanged(); } }
    public boolean claimMainBaseNotice(boolean active) {
        requireWritable();
        if (!isMainBase()) { return false; }
        int next = BuildingTransactions.claimMainBaseNotice(mainBaseNotices, active);
        if (next == mainBaseNotices) { return false; }
        mainBaseNotices = next;
        setChanged();
        return true;
    }
    public boolean operational(HumanWorldData data) {
        return !isMainBase() || data.isMainBase(new BuildingPosition(level.dimension().identifier(), worldPosition))
                && ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level() >= StructureConfig.ACTIVATION_STAGE.get();
    }
    public void requireWritable() {
        if (schema != 1 || malformed) { throw new IllegalStateException("Unsupported/malformed building save; restore a compatible mod/backup."); }
    }
    public void change(BuildingState next) {
        requireWritable();
        if (!state.equals(next)) { state = next; setChanged(); }
    }
    public BuildingTransactions.Upgrade upgradeCost() {
        int index = Math.min(3, state.level() - 1);
        return new BuildingTransactions.Upgrade(BuildingConfig.UPGRADE_POINTS.get().get(index).longValue(),
                BuildingConfig.UPGRADE_SUPPLIES.get().get(index).longValue(), BuildingConfig.UPGRADE_SAMPLES.get().get(index).intValue(),
                BuildingConfig.UPGRADE_RESEARCH.get().get(index));
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("building_version", schema);
        output.putBoolean("main_base", mainBase);
        output.putInt("main_base_notices", mainBaseNotices);
        if ((schema != 1 || malformed) && retainedState != null) {
            output.store("building", Codec.PASSTHROUGH, retainedState);
        } else {
            CompoundTag tag = retainedState != null && retainedState.convert(NbtOps.INSTANCE).getValue() instanceof CompoundTag old ? old.copy() : new CompoundTag();
            CompoundTag current = state.save();
            for (String key : current.keySet()) { tag.put(key, current.get(key)); }
            output.store("building", CompoundTag.CODEC, tag);
        }
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        schema = input.getIntOr("building_version", 1);
        mainBase = input.getBooleanOr("main_base", false);
        mainBaseNotices = Math.clamp(input.getIntOr("main_base_notices", 0), 0, 3);
        retainedState = input.read("building", Codec.PASSTHROUGH).orElse(null);
        malformed = retainedState != null && !(retainedState.convert(NbtOps.INSTANCE).getValue() instanceof CompoundTag);
        state = malformed || retainedState == null ? BuildingState.EMPTY : BuildingState.CODEC.parse(retainedState).getOrThrow();
        elapsed = 0; // Deliberately no offline catch-up or saved wall-clock timestamps.
        deploymentElapsed = 0;
    }
    @Override public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server && kind() == BuildingKind.COMMAND_POST) {
            try {
                HumanWorldData data = HumanWorldData.get(server.getServer());
                if (isMainBase() ? data.registerMainBase(position(server)) : data.registerCommandPost(position(server))) { refreshMissions(server, data); }
            } catch (IllegalArgumentException | IllegalStateException exception) { reportOnce(exception); }
        }
    }
    private BuildingPosition position(ServerLevel server) { return new BuildingPosition(server.dimension().identifier(), worldPosition); }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState block) {
        super.preRemoveSideEffects(pos, block);
        if (removalHandled || !(level instanceof ServerLevel server)) { return; }
        removalHandled = true;
        try {
            requireWritable();
            HumanWorldData data = HumanWorldData.get(server.getServer());
            if (kind() == BuildingKind.COMMAND_POST) { data.removeCommandPost(position(server).key()); }
            if (isMainBase()) { data.removeMainBase(position(server).key()); }
            int samples = state.samples();
            try { data.setSupplies(FoundationAmounts.credit(data.supplies(), state.supplies())); }
            catch (IllegalArgumentException exception) { LogUtils.getLogger().warn("Supply recovery overflow at {}: {}", pos, exception.getMessage()); }
            change(BuildingState.EMPTY);
            while (samples > 0) {
                int count = Math.min(64, samples);
                Block.popResource(server, pos, new ItemStack(SampleItems.TISSUE.get(), count));
                samples -= count;
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            LogUtils.getLogger().warn("Building removal at {} could not recover virtual storage: {}", pos, exception.getMessage());
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState block, BuildingBlockEntity building) {
        if (!(level instanceof ServerLevel server)) { return; }
        if (building.kind() == BuildingKind.COMMAND_POST && ++building.responseElapsed >= ResponseConfig.CHECK_INTERVAL.get()) {
            building.responseElapsed = 0;
            try {
                building.requireWritable();
                if (building.operational(HumanWorldData.get(server.getServer()))) { ThreatResponse.tick(server, building); }
            } catch (IllegalArgumentException | IllegalStateException exception) { building.reportOnce(exception); }
        }
        if (building.kind() == BuildingKind.COMMAND_POST && ++building.deploymentElapsed >= NpcConfig.INTERVAL.get()) {
            building.deploymentElapsed = 0;
            try {
                building.requireWritable();
                if (building.operational(HumanWorldData.get(server.getServer()))) { DefenderDeployment.attempt(server, building); }
            }
            catch (IllegalArgumentException | IllegalStateException exception) { building.reportOnce(exception); }
        }
        int interval = building.kind() == BuildingKind.COMMAND_POST ? BuildingConfig.INTERVAL.get() : BuildingConfig.LAB_INTERVAL.get();
        if (++building.elapsed < interval) { return; }
        building.elapsed = 0;
        try {
            building.requireWritable();
            HumanWorldData data = HumanWorldData.get(server.getServer());
            data.requireSupportedVersion();
            boolean changed = false;
            if (building.kind() == BuildingKind.COMMAND_POST) {
                if (!building.operational(data)) { return; }
                int points = BuildingConfig.POINTS.get();
                if (points > 0 && data.points() <= Long.MAX_VALUE - points) {
                    HumanPointManager.addPoints(server.getServer(), points);
                    changed = true;
                }
                long add = Math.min(BuildingConfig.SUPPLIES.get(), Math.max(0, BuildingConfig.CAPACITY.get() - building.state.supplies()));
                if (add > 0) { building.change(building.state.supplies(building.state.supplies() + add)); }
            } else if (building.kind() == BuildingKind.LAB && !building.state.target().isEmpty()) {
                FoundationCatalog catalog = FoundationResources.get(server.getServer());
                var entry = catalog.research(building.state.target());
                long[] thresholds = HumanProgressionConfig.thresholds();
                String blocked = BuildingTransactions.researchBlocked(building.state, data, entry, thresholds);
                if (building.state.researching() && blocked.equals("Ready")) {
                    int required = BuildingConfig.LAB_WORK.get();
                    BuildingState next = building.state.work(Math.min(required, building.state.work() + (building.state.work() < required ? 1 : 0)));
                    boolean completed = next.work() >= required;
                    if (completed) {
                        next = BuildingTransactions.finishResearch(next, data, entry, thresholds, required);
                        changed = true;
                    }
                    building.change(next);
                    if (completed) {
                        server.getServer().getPlayerList().broadcastSystemMessage(
                                Component.translatable("message.standandhold.research_complete", entry.title()), false);
                    }
                } else if (building.state.researching() && blocked.equals("Completed")) {
                    building.change(building.state.running(false));
                }
            }
            if (changed) { refreshMissions(server, data); }
            building.reportedFailure = false;
        } catch (IllegalArgumentException | IllegalStateException exception) { building.reportOnce(exception); }
    }

    static void refreshMissions(ServerLevel server, HumanWorldData data) {
        MissionManager.refresh(data, FoundationResources.get(server.getServer()), HumanProgressionConfig.thresholds());
    }
    private void reportOnce(RuntimeException exception) {
        if (!reportedFailure) { LogUtils.getLogger().debug("Building at {} paused: {}", worldPosition, exception.getMessage()); reportedFailure = true; }
    }
    @Override public Component getDisplayName() { return Component.translatable("block.standandhold." + kind().id()); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BuildingMenu(id, inventory, this, FoundationResources.get(player.level().getServer()));
    }
    public void writeMenuData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
        buffer.writeEnum(kind());
        var entries = FoundationResources.get(level.getServer()).research().values();
        buffer.writeVarInt(entries.size());
        for (var entry : entries) {
            buffer.writeUtf(entry.id(), 128); buffer.writeUtf(entry.title(), 80);
            buffer.writeVarLong(entry.supplyCost()); buffer.writeVarInt(entry.sampleCost()); buffer.writeVarInt(entry.requiredStage());
        }
    }
}
