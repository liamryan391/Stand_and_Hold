package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StructureConfig;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Properties;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Separate height pads keep both layout eras intact alongside the existing restart fixtures. */
final class RestartMainBaseFixture {
    private static final BlockPos OLD = new BlockPos(8, 180, 7), NEW = new BlockPos(56, 90, 55);
    private static final BuildingState POST = new BuildingState(2, 19, 2, "", 0);
    private static final BuildingState LAB = new BuildingState(1, 17, 4, "standandhold:field_communications", 3, false);
    private static final BuildingState CRATE = new BuildingState(1, 1, 2, "", 0);
    static void seed(ServerLevel level, Properties receipt) {
        var data = HumanWorldData.get(level.getServer());
        var center = OLD.south();
        for (int z = -6; z <= 6; z++) {
            for (int x = -6; x <= 6; x++) {
                level.setBlockAndUpdate(center.offset(x, -1, z), Blocks.STONE_BRICKS.defaultBlockState());
                if ((Math.abs(x) == 6 || Math.abs(z) == 6) && (z != 6 || Math.abs(x) > 1)) {
                    level.setBlockAndUpdate(center.offset(x, 0, z), Blocks.COBBLESTONE.defaultBlockState());
                    level.setBlockAndUpdate(center.offset(x, 1, z), Blocks.COBBLESTONE.defaultBlockState());
                }
                if (Math.abs(x) <= 1 && z >= -2 && z <= 0) {
                    level.setBlockAndUpdate(center.offset(x, 3, z), Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
                }
            }
        }
        level.setBlockAndUpdate(OLD, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
        level.setBlockAndUpdate(lab(OLD), BuildingRegistry.LAB.get().defaultBlockState());
        level.setBlockAndUpdate(crate(OLD), BuildingRegistry.CRATE.get().defaultBlockState());
        tile(level, OLD).initializeMainBase();
        data.registerMainBase(new BuildingPosition(level.dimension().identifier(), OLD));
        for (var pos : BlockPos.betweenClosed(50, 88, 50, 62, 89, 62)) {
            level.setBlockAndUpdate(pos, (pos.getY() == 89 ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState());
        }
        int radius = StructureConfig.MAIN_BASE_RADIUS.get();
        try {
            StructureConfig.MAIN_BASE_RADIUS.set(6);
            var result = StructurePlacement.place(level, StructureKind.MAIN_BASE, 3, 3);
            check(result.success() && NEW.equals(result.commandPost()), "New base placement on isolated pad: " + result.message());
            check(tile(level, crate(NEW)).state().supplies() == StructureConfig.MAIN_BASE_SUPPLIES.get(), "New crate stock is seeded once");
        } finally { StructureConfig.MAIN_BASE_RADIUS.set(radius); }
        for (var anchor : new BlockPos[]{OLD, NEW}) {
            var post = tile(level, anchor);
            post.change(POST); tile(level, lab(anchor)).change(LAB); tile(level, crate(anchor)).change(CRATE);
            check(post.claimMainBaseNotice(false) && post.claimMainBaseNotice(true), "Both notices recorded before restart");
            level.setBlockAndUpdate(anchor.offset(-4, -1, 4), Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.RED).defaultBlockState());
            receipt.setProperty("mainbase.blocks." + anchor.getY(), digest(level, anchor));
        }
    }
    static void verify(ServerLevel level, Properties receipt) {
        var data = HumanWorldData.get(level.getServer());
        for (var anchor : new BlockPos[]{OLD, NEW}) {
            var post = tile(level, anchor);
            var home = new BuildingPosition(level.dimension().identifier(), anchor);
            check(post.isMainBase() && data.isMainBase(home) && !post.operational(data)
                    && data.defenderCount(home.key()) == 0, "Registered site remains dormant; no garrison refill");
            check(post.mainBaseLayoutLabel().equals(anchor.equals(OLD) ? "unversioned foundation" : "inhabited layout v1"), "No retrofit or layout downgrade");
            check(post.state().equals(POST) && tile(level, lab(anchor)).state().equals(LAB) && tile(level, crate(anchor)).state().equals(CRATE),
                    "No level change, resource refill or restarted lab work");
            check(!post.claimMainBaseNotice(false) && !post.claimMainBaseNotice(true), "Neither notice repeats after actual restart");
            check(digest(level, anchor).equals(receipt.getProperty("mainbase.blocks." + anchor.getY())), "Every block in the footprint, including custom marker, retained");
        }
    }
    private static String digest(ServerLevel level, BlockPos anchor) {
        var center = anchor.south();
        var states = new StringBuilder();
        for (var pos : BlockPos.betweenClosed(center.offset(-6, -1, -6), center.offset(6, 4, 6))) {
            states.append(level.getBlockState(pos)).append('\n');
        }
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(states.toString().getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    private static BuildingBlockEntity tile(ServerLevel level, BlockPos pos) { return (BuildingBlockEntity) level.getBlockEntity(pos); }
    private static BlockPos lab(BlockPos anchor) { return anchor.offset(-4, 0, -3); }
    private static BlockPos crate(BlockPos anchor) { return anchor.offset(5, 0, -4); }
    private static void check(boolean condition, String message) { if (!condition) { throw new IllegalStateException(message); } }
}
