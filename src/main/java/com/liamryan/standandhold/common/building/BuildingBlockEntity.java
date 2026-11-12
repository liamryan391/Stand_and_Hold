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
import com.liamryan.standandhold.common.structure.SiteProvenance;
import com.liamryan.standandhold.common.structure.ExpansionOperation;
import com.liamryan.standandhold.common.structure.OutpostGrowth;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.config.ResponseConfig;
import com.liamryan.standandhold.common.threat.ThreatResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
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
    private static final int INHABITED_BASE_LAYOUT = 1;
    private Dynamic<?> retainedMainBaseLayout;
    private Dynamic<?> retainedSite;
    private long siteEpoch;
    private long operationCacheEpoch = -1;
    private java.util.Optional<ExpansionOperation> operationCache;
    private long yardCacheEpoch = -1;
    private java.util.Optional<ExpansionOperation> yardCache;
    private long deckCacheEpoch = -1;
    private java.util.Optional<ExpansionOperation> deckCache;
    private OutpostGrowth.Settings growth = OutpostGrowth.Settings.DEFAULT;
    private Dynamic<?> retainedGrowth;
    private boolean growthSupported = true;
    private com.liamryan.standandhold.common.npc.DefenderDuty duty = com.liamryan.standandhold.common.npc.DefenderDuty.DEFAULT;
    private Dynamic<?> retainedDuty;
    private boolean dutySupported = true;

    public BuildingBlockEntity(BlockPos pos, BlockState block) { super(BuildingRegistry.ENTITY.get(), pos, block); }
    public BuildingKind kind() { return ((BuildingBlock) getBlockState().getBlock()).kind(); }
    public BuildingState state() { return state; }
    public int supplyCapacity() { return BuildingConfig.capacity(kind(), state.level()); }
    public com.liamryan.standandhold.common.npc.DefenderDuty duty() { return duty; }
    public boolean dutySupported() { return dutySupported; }
    public void changeDuty(com.liamryan.standandhold.common.npc.DefenderDuty next) {
        requireWritable();
        if (kind() != BuildingKind.COMMAND_POST || !dutySupported) { throw new IllegalStateException("Unsupported duty data retained; storage remains available."); }
        if (!duty.equals(next)) { duty = next; setChanged(); }
    }
    public SiteProvenance site() {
        if (retainedSite == null) { throw new IllegalArgumentException("No site provenance; old/manual posts are not adopted."); }
        if (!(retainedSite.convert(NbtOps.INSTANCE).getValue() instanceof CompoundTag tag)) {
            throw new IllegalArgumentException("Malformed site provenance; retained without repair.");
        }
        return SiteProvenance.read(tag);
    }
    public void initializeSite(SiteProvenance site) {
        requireWritable();
        if (kind() != BuildingKind.COMMAND_POST || isMainBase() || retainedSite != null || !worldPosition.equals(site.anchor())) {
            throw new IllegalStateException("Site provenance can only initialize a new checkpoint post once.");
        }
        retainedSite = new Dynamic<>(NbtOps.INSTANCE, site.save()); siteEpoch++; setChanged();
    }
    public long siteEpoch() { return siteEpoch; }
    public boolean growthSupported() { return growthSupported; }
    public OutpostGrowth.Settings growthSettings() { return growth; }
    public void requireGrowthWritable() {
        requireWritable();
        if (!growthSupported) { throw new IllegalStateException("Unsupported growth data retained; normal building storage remains available."); }
    }
    public void changeGrowth(OutpostGrowth.Settings settings) {
        requireGrowthWritable();
        if (!growth.equals(settings)) { growth = settings; siteEpoch++; setChanged(); }
    }
    public java.util.Optional<ExpansionOperation> siteOperation() {
        if (operationCacheEpoch == siteEpoch) { return operationCache; }
        var site = site();
        var tag = (CompoundTag) retainedSite.convert(NbtOps.INSTANCE).getValue();
        if (tag.get("operation") instanceof CompoundTag receipt && receipt.getIntOr("operation_version", -1) != 1) {
            throw new IllegalArgumentException("Unsupported legacy apron operation version; retained for review.");
        }
        var parsed = tag.get("operation") == null ? java.util.Optional.<ExpansionOperation>empty() : java.util.Optional.of(ExpansionOperation.read(tag.get("operation"), site));
        operationCache = parsed; operationCacheEpoch = siteEpoch; return parsed;
    }
    public void storeSiteOperation(ExpansionOperation operation) {
        requireWritable(); var site = site(); operation.validateSite(site);
        if (operation.targetRevision() != 1 || yardOperation().isPresent() || deckOperation().isPresent()) { throw new IllegalStateException("Legacy apron cannot replace a yard/deck receipt."); }
        var old = siteOperation().orElse(null);
        if (kind() != BuildingKind.COMMAND_POST || isMainBase() || !worldPosition.equals(site.anchor())
                || site.appliedRevision() != 0 && !(site.appliedRevision() == 1 && old != null && old.id().equals(operation.id()) && operation.phase() == ExpansionOperation.Phase.APPLIED)
                || old != null && (!old.id().equals(operation.id()) && old.phase() != ExpansionOperation.Phase.PREPARED
                || old.id().equals(operation.id()) && (!old.changes().equals(operation.changes()) || !old.protectedDigest().equals(operation.protectedDigest())))) {
            throw new IllegalStateException("Cannot replace a sealed/ambiguous expansion receipt or alter its intent.");
        }
        var tag = ((CompoundTag) retainedSite.convert(NbtOps.INSTANCE).getValue()).copy();
        var saved = tag.getCompoundOrEmpty("operation").copy(); var current = operation.save();
        for (String key : current.keySet()) {
            if (key.equals("cells") && old != null && old.id().equals(operation.id())) { continue; }
            saved.put(key, current.get(key));
        }
        tag.put("operation", saved);
        if (operation.phase() == ExpansionOperation.Phase.APPLIED) { tag.putInt("applied_revision", 1); }
        retainedSite = new Dynamic<>(NbtOps.INSTANCE, tag); siteEpoch++; setChanged();
    }
    public java.util.Optional<ExpansionOperation> yardOperation() {
        if (yardCacheEpoch == siteEpoch) { return yardCache; }
        var site = site(); var tag = (CompoundTag) retainedSite.convert(NbtOps.INSTANCE).getValue();
        var parsed = tag.get("yard_operation") == null ? java.util.Optional.<ExpansionOperation>empty()
                : java.util.Optional.of(ExpansionOperation.read(tag.get("yard_operation"), site));
        if (parsed.isPresent() && parsed.get().targetRevision() != 2) { throw new IllegalArgumentException("Unsupported yard receipt revision."); }
        yardCache = parsed; yardCacheEpoch = siteEpoch; return parsed;
    }
    public void storeYardOperation(ExpansionOperation operation) {
        requireGrowthWritable(); var site = site(); operation.validateSite(site); var old = yardOperation().orElse(null);
        if (operation.targetRevision() != 2 || deckOperation().isPresent() || kind() != BuildingKind.COMMAND_POST || isMainBase() || !worldPosition.equals(site.anchor())
                || site.appliedRevision() != operation.sourceRevision() && !(site.appliedRevision() == 2 && old != null && old.id().equals(operation.id()) && operation.phase() == ExpansionOperation.Phase.APPLIED)
                || old != null && (!old.id().equals(operation.id()) || !old.changes().equals(operation.changes()) || !old.protectedDigest().equals(operation.protectedDigest())
                        || old.sourceRevision() != operation.sourceRevision())) {
            throw new IllegalStateException("Cannot replace a sealed/ambiguous yard receipt or change its intent.");
        }
        var apron = siteOperation().orElse(null);
        if (operation.sourceRevision() == 0 && apron != null || operation.sourceRevision() == 1 && (apron == null || apron.phase() != ExpansionOperation.Phase.APPLIED)) {
            throw new IllegalStateException("Yard source requires no apron intent, or a sealed revision-1 apron.");
        }
        var tag = ((CompoundTag) retainedSite.convert(NbtOps.INSTANCE).getValue()).copy();
        var saved = tag.getCompoundOrEmpty("yard_operation").copy(); var current = operation.save();
        for (String key : current.keySet()) { if (!key.equals("cells") || old == null) { saved.put(key, current.get(key)); } }
        tag.put("yard_operation", saved);
        if (operation.phase() == ExpansionOperation.Phase.APPLIED) { tag.putInt("applied_revision", 2); }
        retainedSite = new Dynamic<>(NbtOps.INSTANCE, tag); siteEpoch++; setChanged();
    }
    public java.util.Optional<ExpansionOperation> deckOperation() {
        if (deckCacheEpoch == siteEpoch) { return deckCache; }
        var site = site(); var tag = (CompoundTag) retainedSite.convert(NbtOps.INSTANCE).getValue();
        var parsed = tag.get("deck_operation") == null ? java.util.Optional.<ExpansionOperation>empty()
                : java.util.Optional.of(ExpansionOperation.read(tag.get("deck_operation"), site));
        if (parsed.isPresent() && parsed.get().targetRevision() != 3) { throw new IllegalArgumentException("Unsupported deck receipt revision."); }
        deckCache = parsed; deckCacheEpoch = siteEpoch; return parsed;
    }
    public void storeDeckOperation(ExpansionOperation operation) {
        requireGrowthWritable(); var site = site(); operation.validateSite(site); var old = deckOperation().orElse(null);
        if (operation.targetRevision() != 3 || kind() != BuildingKind.COMMAND_POST || isMainBase() || !worldPosition.equals(site.anchor())
                || site.appliedRevision() != 2 && !(site.appliedRevision() == 3 && old != null && old.id().equals(operation.id()) && operation.phase() == ExpansionOperation.Phase.APPLIED)
                || old != null && (!old.id().equals(operation.id()) || !old.changes().equals(operation.changes()) || !old.protectedDigest().equals(operation.protectedDigest()))) {
            throw new IllegalStateException("Cannot replace a sealed/ambiguous deck receipt or change its intent.");
        }
        var yard = yardOperation().orElseThrow(() -> new IllegalArgumentException("Deck requires a sealed yard receipt.")); var apron = siteOperation().orElse(null);
        if (yard.phase() != ExpansionOperation.Phase.APPLIED || yard.sourceRevision() == 0 && apron != null
                || yard.sourceRevision() == 1 && (apron == null || apron.phase() != ExpansionOperation.Phase.APPLIED)) {
            throw new IllegalStateException("Deck requires consistent sealed apron/yard source receipts.");
        }
        var tag = ((CompoundTag) retainedSite.convert(NbtOps.INSTANCE).getValue()).copy();
        var saved = tag.getCompoundOrEmpty("deck_operation").copy(); var current = operation.save();
        for (String key : current.keySet()) { if (!key.equals("cells") || old == null) { saved.put(key, current.get(key)); } }
        tag.put("deck_operation", saved);
        if (operation.phase() == ExpansionOperation.Phase.APPLIED) { tag.putInt("applied_revision", 3); }
        retainedSite = new Dynamic<>(NbtOps.INSTANCE, tag); siteEpoch++; setChanged();
    }
    public boolean isMainBase() { return mainBase && kind() == BuildingKind.COMMAND_POST; }
    public void initializeMainBase() { requireWritable(); if (!mainBase) { mainBase = true; setChanged(); } }
    public void initializeInhabitedMainBase() {
        if (kind() != BuildingKind.COMMAND_POST || isMainBase() || retainedMainBaseLayout != null) {
            throw new IllegalStateException("Main Base layout can only initialize a new command anchor.");
        }
        initializeMainBase();
        retainedMainBaseLayout = new Dynamic<>(NbtOps.INSTANCE, IntTag.valueOf(INHABITED_BASE_LAYOUT));
    }
    public String mainBaseLayoutLabel() {
        if (retainedMainBaseLayout == null) { return "unversioned foundation"; }
        return retainedMainBaseLayout.convert(NbtOps.INSTANCE).getValue() instanceof IntTag revision
                && revision.intValue() == INHABITED_BASE_LAYOUT ? "inhabited layout v1" : "unknown layout (retained)";
    }
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
        if (retainedMainBaseLayout != null) { output.store("main_base_layout", Codec.PASSTHROUGH, retainedMainBaseLayout); }
        if (!dutySupported && retainedDuty != null) { output.store("duties", Codec.PASSTHROUGH, retainedDuty); }
        else if (kind() == BuildingKind.COMMAND_POST || retainedDuty != null) {
            var saved = retainedDuty != null && retainedDuty.convert(NbtOps.INSTANCE).getValue() instanceof CompoundTag old ? old.copy() : new CompoundTag();
            saved.remove("watch"); var current = duty.save();
            for (String key : current.keySet()) { saved.put(key, current.get(key)); }
            output.store("duties", CompoundTag.CODEC, saved);
        }
        if (retainedSite != null) { output.store("site", Codec.PASSTHROUGH, retainedSite); }
        if (!growthSupported && retainedGrowth != null) { output.store("growth", Codec.PASSTHROUGH, retainedGrowth); }
        else {
            CompoundTag saved = retainedGrowth != null && retainedGrowth.convert(NbtOps.INSTANCE).getValue() instanceof CompoundTag old ? old.copy() : new CompoundTag();
            var current = growth.save(); for (String key : current.keySet()) { saved.put(key, current.get(key)); }
            output.store("growth", CompoundTag.CODEC, saved);
        }
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
        retainedMainBaseLayout = input.read("main_base_layout", Codec.PASSTHROUGH).orElse(null);
        retainedDuty = input.read("duties", Codec.PASSTHROUGH).orElse(null);
        duty = com.liamryan.standandhold.common.npc.DefenderDuty.DEFAULT; dutySupported = true;
        if (retainedDuty != null) {
            try {
                if (!(retainedDuty.convert(NbtOps.INSTANCE).getValue() instanceof CompoundTag raw)) { throw new IllegalArgumentException("Malformed duty data."); }
                duty = com.liamryan.standandhold.common.npc.DefenderDuty.read(raw);
            } catch (IllegalArgumentException failure) { dutySupported = false; }
        }
        retainedSite = input.read("site", Codec.PASSTHROUGH).orElse(null);
        siteEpoch++;
        retainedGrowth = input.read("growth", Codec.PASSTHROUGH).orElse(null);
        growth = OutpostGrowth.Settings.DEFAULT; growthSupported = true;
        if (retainedGrowth != null) {
            try {
                if (!(retainedGrowth.convert(NbtOps.INSTANCE).getValue() instanceof CompoundTag tag)) { throw new IllegalArgumentException("Malformed growth data"); }
                growth = OutpostGrowth.Settings.read(tag);
            } catch (IllegalArgumentException failure) { growthSupported = false; }
        }
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
                OutpostGrowth.tick(server, building);
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
                building.change(BuildingStorage.generate(building.state, BuildingConfig.SUPPLIES.get(), building.supplyCapacity()));
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

    /** Building ticks/registration have no acting player: order starts/completions are broadcast once, progress is silent. */
    static void refreshMissions(ServerLevel server, HumanWorldData data) {
        com.liamryan.standandhold.common.mission.MissionNotices.sync(server.getServer(), null, com.liamryan.standandhold.common.mission.MissionNotices.NOT_OBSERVED, false);
    }
    private void reportOnce(RuntimeException exception) {
        if (!reportedFailure) { LogUtils.getLogger().debug("Building at {} paused: {}", worldPosition, exception.getMessage()); reportedFailure = true; }
    }
    @Override public Component getDisplayName() {
        return Component.translatable(isMainBase() ? "gui.standandhold.main_base_command" : "block.standandhold." + kind().id());
    }
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
        var orders = BuildingMenu.orders(FoundationResources.get(level.getServer()));
        buffer.writeVarInt(orders.size());
        for (var order : orders) {
            buffer.writeUtf(order.id(), 128); buffer.writeUtf(order.title(), 80); buffer.writeVarInt(order.objective()); buffer.writeVarInt(order.target());
            buffer.writeVarLong(order.points()); buffer.writeVarLong(order.supplies()); buffer.writeVarInt(order.research());
        }
    }
}
