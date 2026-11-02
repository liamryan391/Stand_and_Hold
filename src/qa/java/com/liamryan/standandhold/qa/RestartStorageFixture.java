package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.BuildingConfig;
import java.util.Properties;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class RestartStorageFixture {
    static void seed(ServerLevel level, BlockPos pos, Properties receipt) {
        var post = (BuildingBlockEntity) level.getBlockEntity(pos);
        var data = HumanWorldData.get(level.getServer()); long points = data.points(); var stage = data.stage();
        post.change(new BuildingState(3, 6101, 4, "", 0));
        data.setProgression(1200, com.liamryan.standandhold.common.progression.HumanStage.ELITE_UNITS);
        try { post.change(BuildingTransactions.upgrade(post.state(), data, post.upgradeCost())); }
        finally { data.setProgression(points, stage); }
        check(post.state().equals(new BuildingState(4, 6001, 2, "", 0)), "Existing upgrade costs actually spent before restart; extra capacity is empty");
        receipt.setProperty("storage.post", Long.toString(pos.asLong()));
        receipt.setProperty("storage.global", Long.toString(HumanWorldData.get(level.getServer()).supplies()));
    }
    static void verify(ServerLevel level, Properties receipt) {
        var pos = BlockPos.of(Long.parseLong(receipt.getProperty("storage.post")));
        var post = (BuildingBlockEntity) level.getBlockEntity(pos); var data = HumanWorldData.get(level.getServer());
        check(post != null && post.state().equals(new BuildingState(4, 6001, 2, "", 0)) && post.supplyCapacity() == 6000, "Restart retains over-cap supplies, samples and purchased local level without refill/clamp");
        var saved = post.saveWithFullMetadata(level.registryAccess());
        BuildingConfig.LEVEL_STORAGE.set(false);
        check(post.supplyCapacity() == BuildingConfig.CAPACITY.get() && post.saveWithFullMetadata(level.registryAccess()).equals(saved), "Disabling derived capacity does not rewrite persistent building data");
        var actor = QaPlayer.join(level); actor.snapTo(pos.getX()+.5, pos.getY(), pos.getZ()+4.5, 180, 16);
        try {
            var menu = new BuildingMenu(1, actor.getInventory(), post, FoundationResources.get(level.getServer())); actor.containerMenu = menu;
            long global = Long.parseLong(receipt.getProperty("storage.global"));
            check(data.supplies() == global && menu.clickMenuButton(actor, BuildingMenu.EXPORT), "Restarted over-cap storage exports through the existing server menu");
            check(post.state().supplies() == 5991 && data.supplies() == global + 10 && post.state().level() == 4, "Restart export conserves supplies without changing level");
            BuildingConfig.LEVEL_STORAGE.set(true);
            check(!menu.clickMenuButton(actor, BuildingMenu.IMPORT) && post.state().supplies() == 5991 && data.supplies() == global + 10, "Nine free spaces cannot accept a whole ten-supply batch after restart");
        } finally { BuildingConfig.LEVEL_STORAGE.set(true); actor.closeContainer(); QaPlayer.closeAll(); }
    }
    private static void check(boolean value, String message) { if (!value) { throw new IllegalStateException(message); } }
}
