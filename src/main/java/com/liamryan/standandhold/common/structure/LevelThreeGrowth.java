package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.config.StructureConfig;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** One fresh preflight and synchronous shared application, never a saved queue or repair path. */
public final class LevelThreeGrowth {
    public record Attempt(ExpansionOperation operation, ExpansionApply.Access access) { }
    private LevelThreeGrowth() { }
    public static Attempt prepare(ServerPlayer player, BuildingBlockEntity post) {
        OutpostPreview.requirePost(player, post);
        return prepare(player.level(), post, player);
    }
    public static Attempt prepare(ServerLevel level, BuildingBlockEntity post) { return prepare(level, post, null); }
    private static Attempt prepare(ServerLevel level, BuildingBlockEntity post, ServerPlayer player) {
        if (!StructureConfig.EXPANSION.get()) { throw new IllegalArgumentException("Growth disabled by server; preview stays read-only."); }
        var inspection = LevelThreePreview.inspectDetails(level, post); var plan = inspection.plan();
        var operation = new ExpansionOperation(UUID.randomUUID(), plan.site().id(), plan.changes(),
                OutpostExpansion.deckDigest(level, post, plan.site(), plan.changes()), ExpansionOperation.Phase.PREPARED, 0, "", 2, 3);
        var base = player == null ? OutpostExpansion.access(level, post, operation) : OutpostExpansion.access(player, post, operation);
        return new Attempt(operation, new ExpansionApply.Access() {
            public boolean valid() { return base.valid(); }
            public CompoundTag read(BlockPos pos) { return base.read(pos); }
            public boolean write(ExpansionOperation.Change change) {
                if (!inspection.clear(change)) { throw new OutpostPreview.EntityObstruction("Observed actor entered deck edit at " + change.pos().toShortString()); }
                return base.write(change);
            }
            public String protectedDigest() { return base.protectedDigest(); }
            public void store(ExpansionOperation current) { base.store(current); }
        });
    }
    public static ExpansionApply.Result applyNow(ServerPlayer player, BuildingBlockEntity post) {
        var attempt = prepare(player, post); return ExpansionApply.apply(attempt.operation(), attempt.access());
    }
    public static ExpansionApply.Result applyNow(ServerLevel level, BuildingBlockEntity post) {
        var attempt = prepare(level, post); return ExpansionApply.apply(attempt.operation(), attempt.access());
    }
}
