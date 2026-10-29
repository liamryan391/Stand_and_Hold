package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.support.SupportCallIn;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.threat.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.NpcConfig;
import java.nio.file.*;
import java.util.Properties;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Only loaded by the QA mod in two separate, self-stopping dedicated JVMs. */
final class RestartFixture {
    private static final BlockPos POST = new BlockPos(8, 64, 8), BASE = new BlockPos(16, 64, 8),
            LAB = new BlockPos(8, 64, 12), CRATE = new BlockPos(12, 64, 12);
    private static final String WORLD = System.getProperty("standandhold.qa.world"), PHASE = System.getProperty("standandhold.qa.phase");
    private static final Properties receipt = new Properties();
    private static int ticks;
    private static boolean finished;
    private static HumanNpc npc;
    static void tick(ServerTickEvent.Post event) {
        if (finished) { return; }
        var server = event.getServer(); var level = server.overworld();
        try {
            ticks++;
            if (ticks == 1) {
                NpcConfig.POST_CAP.set(16); NpcConfig.POST_LEVELS.set(DefenderCapacity.DEFAULT_LEVELS);
                com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS.set(4);
                level.getGameRules().set(GameRules.SPAWN_MOBS, false, server);
                level.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
                server.setDifficulty(Difficulty.NORMAL, true);
                for (int x = 0; x < 4; x++) { for (int z = 0; z < 4; z++) { level.setChunkForced(x, z, true); } }
                if (PHASE.equals("write")) { seed(server, level); }
                else { try (var in = Files.newInputStream(path("receipt"))) { receipt.load(in); } }
            }
            if (PHASE.equals("write") && ticks == 5) { support(level); }
            if (PHASE.equals("write") && ticks == 80) {
                check(npc.hasIssuedBlade(), "Stage 1 blade was actually issued before restart");
                npc.setNoAi(true);
                RestartTierFixture.freeze();
                HumanPointManager.setStage(server, HumanStage.SURVIVORS);
                HumanPointManager.addPoints(server, 42);
                receipt.setProperty("npc", npc.getUUID().toString());
                receipt.setProperty("response", Long.toString(ThreatResponse.state(level, tile(level, POST)).nextRaid()));
                receipt.setProperty("supportCooldown", Long.toString(ThreatResponse.state(level, tile(level, POST)).nextReinforcement()));
                try (var out = Files.newOutputStream(path("receipt"), StandardOpenOption.CREATE_NEW)) { receipt.store(out, "Expected values from the first JVM"); }
                finish(server, true, "Seeded persistent state; clean shutdown requested");
            } else if (PHASE.equals("read") && ticks == 100) {
                verify(server, level);
                finish(server, true, "Second JVM verified progression/research/mission/storage/registries/notice receipts/NPC blade/event cooldown");
            }
            if (ticks > 200) { throw new IllegalStateException("Restart fixture exceeded its bounded tick budget"); }
        } catch (Exception failure) { finish(server, false, failure.toString()); }
    }
    private static void seed(MinecraftServer server, ServerLevel level) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 62, 0, 63, 68, 63)) {
            level.setBlockAndUpdate(pos, (pos.getY() == 62 ? Blocks.DIRT : pos.getY() == 63 ? Blocks.GRASS_BLOCK : Blocks.AIR).defaultBlockState());
        }
        level.clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(), 18000);
        level.setBlockAndUpdate(POST, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
        level.setBlockAndUpdate(BASE, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
        level.setBlockAndUpdate(LAB, BuildingRegistry.LAB.get().defaultBlockState());
        level.setBlockAndUpdate(CRATE, BuildingRegistry.CRATE.get().defaultBlockState());
        var data = HumanWorldData.get(server); var base = tile(level, BASE);
        base.initializeMainBase(); data.registerMainBase(new BuildingPosition(level.dimension().identifier(), BASE));
        base.claimMainBaseNotice(false); base.claimMainBaseNotice(true);
        tile(level, LAB).change(new BuildingState(1, 55, 4, "standandhold:field_communications", 5, false));
        tile(level, CRATE).change(new BuildingState(1, 27, 3, "", 0));
        tile(level, POST).change(new BuildingState(2, 17, 2, "", 0));
        var checkpoint = StructurePlacement.place(level, StructureKind.CHECKPOINT, 2, 2);
        check(checkpoint.success(), "Actual checkpoint before restart: " + checkpoint.message());
        var checkpointPost = tile(level, checkpoint.commandPost());
        checkpointPost.change(new BuildingState(2, 11, 1, "", 0));
        receipt.setProperty("siteUuid", checkpointPost.site().id().toString());
        int radius = com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS.get();
        BlockPos checkpointLab = checkpoint.commandPost().offset(-radius + 2, 0, -radius + 3);
        BlockPos checkpointCrate = checkpoint.commandPost().offset(radius - 1, 0, -radius + 2);
        check(tile(level, checkpointLab).kind() == BuildingKind.LAB && tile(level, checkpointLab).state().equals(BuildingState.EMPTY), "New checkpoint lab starts empty/idle");
        tile(level, checkpointLab).change(new BuildingState(1, 31, 2, "standandhold:field_communications", 3, false));
        tile(level, checkpointCrate).change(tile(level, checkpointCrate).state().supplies(1));
        receipt.setProperty("checkpoint", Long.toString(checkpoint.commandPost().asLong()));
        receipt.setProperty("checkpointLab", Long.toString(checkpointLab.asLong()));
        receipt.setProperty("checkpointCrate", Long.toString(checkpointCrate.asLong()));
        data.setSupplies(150);
        ResearchManager.complete(data, FoundationResources.get(server), HumanProgressionConfig.thresholds(), "standandhold:parasite_samples");
        HumanPointManager.setStage(server, HumanStage.LOCAL_ARMY_RESPONSE);
        ResearchManager.complete(data, FoundationResources.get(server), HumanProgressionConfig.thresholds(), "standandhold:field_communications");
        MissionManager.start(data, FoundationResources.get(server), HumanProgressionConfig.thresholds(), "standandhold:recover_parasite_samples");
        MissionManager.advance(data, FoundationResources.get(server), HumanProgressionConfig.thresholds(), "standandhold:recover_parasite_samples", 2);
        HumanPointManager.setStage(server, HumanStage.LOCAL_ARMY_RESPONSE);
        npc = NpcRegistry.SURVIVOR.get().create(level, EntitySpawnReason.COMMAND);
        check(npc != null, "NPC factory"); npc.snapTo(10.5, 64, 10.5, 0, 0); level.addFreshEntity(npc);
        String raid = ThreatResponse.raid(level, tile(level, POST));
        check(raid.startsWith("Outpost raid"), "Actual persisted raid: " + raid);
        // Keep the reserved real raider stable while the second JVM loads its entity chunk.
        var response = ThreatResponse.state(level, tile(level, POST));
        for (UUID id : response.raiders()) {
            var entity = level.getEntity(id); if (entity instanceof net.minecraft.world.entity.Mob mob) { mob.setNoAi(true); }
        }
    }
    private static void support(ServerLevel level) {
        var caller = QaPlayer.join(level); caller.snapTo(8.5, 64, 12.5, 180, 16);
        check(SupportCallIn.request(caller, tile(level, POST)).success(), "Paid reinforcement before restart");
        QaPlayer.closeAll();
        var deployed = level.getEntitiesOfClass(HumanNpc.class, new net.minecraft.world.phys.AABB(POST).inflate(12), human -> human.commandPost().isPresent());
        check(deployed.size() == 1, "One actual paid defender");
        deployed.getFirst().setNoAi(true); receipt.setProperty("supportNpc", deployed.getFirst().getUUID().toString());
        String home = new BuildingPosition(level.dimension().identifier(), POST).key();
        for (int i = 0; i < 4; i++) {
            var extra = DefenderDeployment.attempt(level, tile(level, POST));
            check(extra != null, "Upgraded post extra defender " + i);
            extra.setNoAi(true); receipt.setProperty("extraNpc" + i, extra.getUUID().toString());
        }
        check(data(level).defenderCount(home) == 5 && DefenderDeployment.attempt(level, tile(level, POST)) == null, "Level 2 fills exactly five slots");
        receipt.setProperty("preview", preview(level));
        RestartExpansionFixture.seed(level, receipt);
        RestartGrowthFixture.seed(level, receipt);
        RestartTierFixture.seed(level, receipt);
    }
    private static void verify(MinecraftServer server, ServerLevel level) {
        var data = HumanWorldData.get(server); var base = tile(level, BASE); var lab = tile(level, LAB);
        check(data.points() == 42 && data.stage() == HumanStage.SURVIVORS && data.supplies() == 110, "Global progression/supply balances including once-only support debit");
        check(data.isResearchComplete("standandhold:parasite_samples"), "Research receipt");
        check(data.mission("standandhold:recover_parasite_samples").orElseThrow().progress() == 2, "Partial mission");
        check(data.isMainBase(new BuildingPosition(level.dimension().identifier(), BASE)) && data.commandPostKeys().size() == 13, "All thirteen original/generated/operation/growth/tier site registrations");
        var checkpoint = BlockPos.of(Long.parseLong(receipt.getProperty("checkpoint")));
        check(data.commandPostKeys().contains(new BuildingPosition(level.dimension().identifier(), checkpoint).key()), "Generated checkpoint registration survived");
        check(tile(level, checkpoint).site().id().toString().equals(receipt.getProperty("siteUuid"))
                && tile(level, checkpoint).state().equals(new BuildingState(2, 11, 1, "", 0)), "Same provenance UUID/layout/level/storage after restart");
        check(preview(level).equals(receipt.getProperty("preview")), "Restarted read-only preview returns the identical ordered delta");
        var checkpointLab = tile(level, BlockPos.of(Long.parseLong(receipt.getProperty("checkpointLab"))));
        check(checkpointLab.kind() == BuildingKind.LAB && checkpointLab.state().equals(new BuildingState(1, 31, 2, "standandhold:field_communications", 3, false)), "Generated lab's paused work/target/storage persist unchanged");
        check(tile(level, BlockPos.of(Long.parseLong(receipt.getProperty("checkpointCrate")))).state().supplies() == 1, "Checkpoint crate does not refill on restart");
        check(base.isMainBase() && !base.claimMainBaseNotice(false) && !base.claimMainBaseNotice(true), "Both one-time notices");
        check(lab.state().equals(new BuildingState(1, 55, 4, "standandhold:field_communications", 5, false)), "Paused lab target/work/storage");
        check(tile(level, CRATE).state().supplies() == 27 && tile(level, CRATE).state().samples() == 3, "Crate storage");
        check(level.getEntity(UUID.fromString(receipt.getProperty("npc"))) instanceof HumanNpc saved && saved.hasIssuedBlade(), "Same NPC retains issued blade at Stage 0");
        var response = ThreatResponse.state(level, tile(level, POST));
        check(response.raids() == 1 && response.raiders().size() == 1 && response.nextRaid() == Long.parseLong(receipt.getProperty("response")), "Raid counters/reservation/cooldown, no restart reset");
        String home = new BuildingPosition(level.dimension().identifier(), POST).key();
        check(response.reinforcements() == 1 && response.nextReinforcement() == Long.parseLong(receipt.getProperty("supportCooldown")) && data.defenderCount(home) == 5, "Support cooldown/counter/upgraded roster survives restart");
        check(level.getEntity(UUID.fromString(receipt.getProperty("supportNpc"))) instanceof HumanNpc unit && unit.commandPost().map(p -> p.key().equals(home)).orElse(false), "Same paid defender/home survived, no duplicate on restart");
        var post = tile(level, POST);
        check(post.state().equals(new BuildingState(2, 17, 2, "", 0)), "Upgraded level and local storage survive unchanged");
        check(DefenderDeployment.status(level, post).equals(new DefenderDeployment.Status(5, 5, 4)), "Saved full capacity status at lower world stage");
        for (int i = 0; i < 4; i++) {
            check(level.getEntity(UUID.fromString(receipt.getProperty("extraNpc" + i))) instanceof HumanNpc saved
                    && saved.commandPost().map(p -> p.key().equals(home)).orElse(false), "Same upgraded defender UUID/home " + i);
        }
        NpcConfig.POST_CAP.set(2);
        check(DefenderDeployment.attempt(level, post) == null && data.defenderCount(home) == 5, "Lowering ceiling after real restart preserves excess reservations");
        NpcConfig.POST_CAP.set(16);
        RestartExpansionFixture.verify(level, receipt);
        RestartGrowthFixture.verify(level, receipt);
        RestartTierFixture.verify(level, receipt);
    }
    private static HumanWorldData data(ServerLevel level) { return HumanWorldData.get(level.getServer()); }
    private static String preview(ServerLevel level) {
        var post = tile(level, BlockPos.of(Long.parseLong(receipt.getProperty("checkpoint"))));
        var player = QaPlayer.join(level); var pos = post.getBlockPos();
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16);
        var before = post.saveWithFullMetadata(level.registryAccess());
        var global = HumanWorldData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, data(level)).getOrThrow();
        boolean dirty = data(level).isDirty();
        try {
            var plan = OutpostPreview.inspect(player, post);
            check(plan.cells().size() == 137 && plan.reads() == 616, "Exact preview before/after real restart");
            check(post.saveWithFullMetadata(level.registryAccess()).equals(before) && data(level).isDirty() == dirty
                    && HumanWorldData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, data(level)).getOrThrow().equals(global), "Restart preview changes no post/global data or dirty flag");
            return plan.cells().stream().map(cell -> cell.pos().asLong() + ":" + cell.material().name()).collect(java.util.stream.Collectors.joining(";"));
        } finally { QaPlayer.closeAll(); }
    }
    private static BuildingBlockEntity tile(ServerLevel level, BlockPos pos) { return (BuildingBlockEntity) level.getBlockEntity(pos); }
    private static void check(boolean valid, String message) { if (!valid) { throw new IllegalStateException(message); } }
    private static Path path(String suffix) { return Path.of(WORLD + "-" + suffix + ".properties"); }
    private static void finish(MinecraftServer server, boolean success, String detail) {
        finished = true;
        try {
            var result = new Properties(); result.setProperty("success", Boolean.toString(success)); result.setProperty("detail", detail);
            try (var out = Files.newOutputStream(path(PHASE), StandardOpenOption.CREATE_NEW)) { result.store(out, "QA result"); }
            com.mojang.logging.LogUtils.getLogger().info("QA_RESTART {} {}: {}", PHASE, success ? "PASS" : "FAIL", detail);
        } catch (Exception failure) { com.mojang.logging.LogUtils.getLogger().error("QA result could not be written", failure); }
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "stop");
    }
}
