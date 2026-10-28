package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.threat.ThreatWorldData;
import com.liamryan.standandhold.common.world.HumanWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

final class QaWorld {
    private QaWorld() { }
    static void begin(GameTestHelper h) {
        QaPlayer.closeAll();
        h.getLevel().getServer().getDataStorage().set(HumanWorldData.TYPE, new HumanWorldData());
        h.getLevel().getServer().getDataStorage().set(ThreatWorldData.TYPE, new ThreatWorldData());
        h.setTime(18000);
        h.getLevel().getRandom().setSeed(918273L);
        for (int x = 0; x < 64; x++) { for (int z = 0; z < 64; z++) {
            h.getLevel().setBlock(h.absolutePos(new BlockPos(x, 0, z)), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            h.getLevel().setBlock(h.absolutePos(new BlockPos(x, 1, z)), Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        } }
        h.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo info) { }
            public void testPassed(GameTestInfo info, GameTestRunner runner) { cleanup(h); }
            public void testFailed(GameTestInfo info, GameTestRunner runner) { cleanup(h); }
            public void testAddedForRerun(GameTestInfo old, GameTestInfo copy, GameTestRunner runner) { }
        });
    }
    private static void cleanup(GameTestHelper h) {
        QaPlayer.closeAll();
        h.killAllEntities();
        // Only this test's fixed 64x8x64 arena; no world/global scan or leftover ticking buildings.
        for (BlockPos relative : BlockPos.betweenClosed(0, 0, 0, 63, 7, 63)) {
            BlockPos pos = h.absolutePos(relative);
            if (h.getLevel().getBlockState(pos).hasBlockEntity() && h.getLevel().getBlockEntity(pos) instanceof BuildingBlockEntity) {
                h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }
}
