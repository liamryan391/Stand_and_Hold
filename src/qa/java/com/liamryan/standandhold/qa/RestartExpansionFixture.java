package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.config.StructureConfig;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;

/** Four actual persisted states, not a serialization-only or in-process restart simulation. */
final class RestartExpansionFixture {
    private static final List<String> STATES = List.of("prepared", "mixed", "applied", "all_after");
    static void seed(ServerLevel level, Properties receipt) {
        StructureConfig.EXPANSION.set(true);
        var player = QaPlayer.join(level);
        try {
            for (int i = 0; i < STATES.size(); i++) {
                String key = "exp." + STATES.get(i); int cx = i == 0 ? 0 : i == 1 ? 1 : 3, cz = i == 3 ? 3 : 2;
                var placed = StructurePlacement.place(level, StructureKind.CHECKPOINT, cx, cz); check(placed.success(), placed.message());
                var post = (BuildingBlockEntity) level.getBlockEntity(placed.commandPost()); post.change(new BuildingState(2, 11, 1, "", 0));
                var lab = (BuildingBlockEntity) level.getBlockEntity(post.getBlockPos().offset(-2, 0, -1));
                var crate = (BuildingBlockEntity) level.getBlockEntity(post.getBlockPos().offset(3, 0, -2));
                lab.change(new BuildingState(1, 31, 2, "standandhold:field_communications", 3, false)); crate.change(crate.state().supplies(1));
                aim(player, post); receipt.setProperty(key + ".token", OutpostExpansion.prepare(player, post).toString());
                var inspection = OutpostPreview.inspectDetails(player, post);
                var operation = new ExpansionOperation(UUID.randomUUID(), post.site().id(), inspection.changes(), OutpostExpansion.protectedDigest(level, post, post.site()), ExpansionOperation.Phase.PREPARED, 0, "");
                var access = OutpostExpansion.access(player, post, operation);
                if (i == 2) { check(ExpansionApply.apply(operation, access).success(), "Actual applied site before restart"); }
                else {
                    post.storeSiteOperation(operation);
                    int writes = i == 1 ? 68 : i == 3 ? 137 : 0;
                    for (int at = 0; at < writes; at++) { check(access.write(operation.changes().get(at)), "Construct bounded interrupted-operation fixture"); }
                }
                receipt.setProperty(key + ".post", Long.toString(post.getBlockPos().asLong()));
                receipt.setProperty(key + ".site", post.site().id().toString()); receipt.setProperty(key + ".operation", operation.id().toString());
                receipt.setProperty(key + ".digest", operation.protectedDigest());
            }
        } finally { QaPlayer.closeAll(); }
    }
    static void verify(ServerLevel level, Properties receipt) {
        StructureConfig.EXPANSION.set(true); var player = QaPlayer.join(level);
        try {
            for (int i = 0; i < STATES.size(); i++) {
                String key = "exp." + STATES.get(i); var post = (BuildingBlockEntity) level.getBlockEntity(BlockPos.of(Long.parseLong(receipt.getProperty(key + ".post"))));
                aim(player, post); var operation = post.siteOperation().orElseThrow();
                check(post.site().id().toString().equals(receipt.getProperty(key + ".site")) && operation.id().toString().equals(receipt.getProperty(key + ".operation")), "Same site/operation identity after actual restart");
                check(post.state().equals(new BuildingState(2, 11, 1, "", 0)) && OutpostExpansion.protectedDigest(level, post, post.site()).equals(receipt.getProperty(key + ".digest")), "Whole original protected footprint/building NBT retained, no lab/crate refill");
                var expected = i == 0 ? ExpansionOperation.Observed.ALL_BEFORE : i == 1 ? ExpansionOperation.Observed.MIXED : ExpansionOperation.Observed.ALL_AFTER;
                check(operation.observe(pos -> NbtUtils.writeBlockState(level.getBlockState(pos))).state() == expected, "Restart never auto-replays/repairs/seals " + STATES.get(i));
                check(operation.phase() == (i == 2 ? ExpansionOperation.Phase.APPLIED : ExpansionOperation.Phase.PREPARED) && post.site().appliedRevision() == (i == 2 ? 1 : 0), "Persisted phase and applied revision unchanged by loading");
                try { OutpostExpansion.confirm(player, post, UUID.fromString(receipt.getProperty(key + ".token"))); throw new IllegalStateException("Restart must invalidate tokens"); }
                catch (IllegalArgumentException expectedFailure) { check(expectedFailure.getMessage().contains("token"), expectedFailure.getMessage()); }
                var before = post.saveWithFullMetadata(level.registryAccess()).copy(); OutpostExpansion.status(player, post);
                check(before.equals(post.saveWithFullMetadata(level.registryAccess())), "Status after restart remains read-only");
                if (i == 0) { check(OutpostExpansion.confirm(player, post, OutpostExpansion.prepare(player, post)).success(), "Prepared/all-before needs and accepts fresh consent, once"); }
                if (i == 1) {
                    try { OutpostExpansion.recover(player, post); throw new IllegalStateException("Mixed restart must require review"); }
                    catch (IllegalArgumentException expectedFailure) { check(expectedFailure.getMessage().contains("not fully matching"), expectedFailure.getMessage()); }
                    check(before.equals(post.saveWithFullMetadata(level.registryAccess())), "Mixed recovery never rewrites receipt or blocks");
                }
                if (i == 3) { check(OutpostExpansion.recover(player, post).contains("metadata only") && post.site().appliedRevision() == 1, "All-after can be explicitly sealed without replay"); }
            }
        } finally { QaPlayer.closeAll(); }
    }
    private static void aim(QaPlayer player, BuildingBlockEntity post) { var pos = post.getBlockPos(); player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16); player.setYHeadRot(180); }
    private static void check(boolean okay, String message) { if (!okay) { throw new IllegalStateException(message); } }
}
