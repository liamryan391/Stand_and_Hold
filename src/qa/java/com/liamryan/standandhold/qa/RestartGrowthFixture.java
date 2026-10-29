package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.config.ResponseConfig;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Saved choices and interrupted work cross two real dedicated JVMs, without a player online. */
final class RestartGrowthFixture {
    private static final List<String> STATES = List.of("manual", "waiting_level", "paused", "applied", "interrupted");
    private static final int[][] CHUNKS = {{0, 3}, {1, 3}, {2, 3}, {0, 1}, {1, 1}};
    static void seed(ServerLevel level, Properties receipt) {
        for (int i = 0; i < STATES.size(); i++) {
            var placed = StructurePlacement.place(level, StructureKind.CHECKPOINT, CHUNKS[i][0], CHUNKS[i][1]); check(placed.success(), placed.message());
            var post = (BuildingBlockEntity) level.getBlockEntity(placed.commandPost()); post.change(new BuildingState(i == 1 ? 1 : 2, 13, 2, "", 0));
            if (i > 0) { OutpostGrowth.setMode(level, post, OutpostGrowth.Mode.AUTOMATIC); }
            if (i == 2) {
                var blocked = post.site().center().offset(-6, 1, -6); level.setBlockAndUpdate(blocked, Blocks.CHEST.defaultBlockState()); OutpostGrowth.tick(level, post);
                check(post.growthSettings().pause() == OutpostGrowth.TERRAIN, "Actual obstruction saved a pause");
            }
            if (i == 3) {
                check(OutpostExpansion.applyNow(level, post).success(), "Actual apron before save"); post.change(post.state().supplies(9));
            }
            if (i == 4) {
                var plan = OutpostPreview.inspectDetails(level, post);
                post.storeSiteOperation(new ExpansionOperation(UUID.randomUUID(), post.site().id(), plan.changes(), OutpostExpansion.protectedDigest(level, post, post.site()), ExpansionOperation.Phase.PREPARED, 0, ""));
            }
            String key = "growth." + STATES.get(i); receipt.setProperty(key + ".post", Long.toString(post.getBlockPos().asLong()));
            receipt.setProperty(key + ".site", post.site().id().toString());
            receipt.setProperty(key + ".policy", post.growthSettings().toString());
        }
    }
    static void verify(ServerLevel level, Properties receipt) {
        for (int i = 0; i < STATES.size(); i++) {
            String key = "growth." + STATES.get(i); var post = (BuildingBlockEntity) level.getBlockEntity(BlockPos.of(Long.parseLong(receipt.getProperty(key + ".post"))));
            check(post != null && post.site().id().toString().equals(receipt.getProperty(key + ".site")) && post.growthSettings().toString().equals(receipt.getProperty(key + ".policy")), "Same growth choice/pause/site across JVMs: " + key);
            check(post.state().level() == (i == 1 ? 1 : 2) && post.state().supplies() == (i == 3 ? 9 : 13) && post.state().samples() == 2, "No growth-driven spending, upgrade or refill after restart");
            if (i == 3) {
                check(OutpostGrowth.status(level, post) == OutpostGrowth.EXPANDED, "Applied stays Expanded after legitimate storage change/restart");
                var operation = post.siteOperation().orElseThrow(); var broken = operation.changes().getFirst().pos(); level.setBlockAndUpdate(broken, Blocks.AIR.defaultBlockState()); drive(level, post);
                check(level.getBlockState(broken).isAir() && post.siteOperation().orElseThrow().id().equals(operation.id()), "Applied mode never repairs/repeats after restart");
            } else if (i == 4) {
                drive(level, post);
                check(post.growthSettings().pause() == OutpostGrowth.REVIEW && post.siteOperation().orElseThrow().observe(pos -> NbtUtils.writeBlockState(level.getBlockState(pos))).state() == ExpansionOperation.Observed.ALL_BEFORE, "Interrupted automatic receipt pauses, no replay after restart");
            } else {
                if (i == 2) { level.setBlockAndUpdate(post.site().center().offset(-6, 1, -6), Blocks.AIR.defaultBlockState()); }
                drive(level, post); check(post.siteOperation().isEmpty(), "Manual/low-level/paused policy cannot cause restart growth: " + key);
                if (i == 1) {
                    post.change(new BuildingState(2, 13, 2, "", 0)); drive(level, post);
                    check(post.site().appliedRevision() == 1 && post.state().supplies() == 13, "Retained Automatic preference uses loaded cadence after level becomes eligible");
                }
            }
        }
    }
    private static void drive(ServerLevel level, BuildingBlockEntity post) {
        for (int tick = 0; tick < ResponseConfig.CHECK_INTERVAL.get(); tick++) { BuildingBlockEntity.tick(level, post.getBlockPos(), post.getBlockState(), post); }
    }
    private static void check(boolean value, String message) { if (!value) { throw new IllegalStateException(message); } }
}
