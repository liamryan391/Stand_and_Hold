package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;

final class DutyTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    static Map<String, Consumer<GameTestHelper>> cases() {
        var cases = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        cases.put("duty_patrol_arrivals", DutyTests::patrol);
        cases.put("duty_watch_arrival_contention", DutyTests::watch);
        cases.put("duty_watch_reassignment", DutyTests::reassign);
        cases.put("duty_watch_combat_return", DutyTests::watchCombat);
        cases.put("duty_dormant_mainbase", DutyTests::dormant);
        cases.put("duty_blocked_backoff", DutyTests::blocked);
        cases.put("duty_combat_zombie_return", h -> combat(h, EntityTypes.ZOMBIE));
        cases.put("duty_combat_skeleton_return", h -> combat(h, EntityTypes.SKELETON));
        cases.put("duty_combat_creeper_return", h -> combat(h, EntityTypes.CREEPER));
        cases.put("duty_missing_post", DutyTests::missing);
        cases.put("duty_unloaded_home", DutyTests::unloaded);
        cases.put("duty_unresolved_watch_owner", DutyTests::unresolved);
        cases.put("duty_menu_authority_save", DutyTests::menu);
        cases.put("duty_response_priority", DutyTests::response);
        var scoped = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        cases.forEach((name, test) -> scoped.put(name, h -> {
            boolean deploy = NpcConfig.DEPLOY.get(), raids = ResponseConfig.RAIDS.get(), reinforce = ResponseConfig.REINFORCEMENTS.get();
            boolean expansion = StructureConfig.EXPANSION.get(), hostile = NpcConfig.HOSTILE_TARGETS.get();
            NpcConfig.DEPLOY.set(false); ResponseConfig.RAIDS.set(false); ResponseConfig.REINFORCEMENTS.set(false);
            StructureConfig.EXPANSION.set(true); NpcConfig.HOSTILE_TARGETS.set(true);
            h.testInfo.addListener(new GameTestListener() {
                private void restore() { NpcConfig.DEPLOY.set(deploy); ResponseConfig.RAIDS.set(raids); ResponseConfig.REINFORCEMENTS.set(reinforce); StructureConfig.EXPANSION.set(expansion); NpcConfig.HOSTILE_TARGETS.set(hostile); }
                public void testStructureLoaded(GameTestInfo i) { }
                public void testPassed(GameTestInfo i, GameTestRunner r) { restore(); }
                public void testFailed(GameTestInfo i, GameTestRunner r) { restore(); }
                public void testAddedForRerun(GameTestInfo a, GameTestInfo b, GameTestRunner r) { }
            });
            test.accept(h);
        }));
        return scoped;
    }
    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static BuildingPosition home(GameTestHelper h, BuildingBlockEntity post) { return new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()); }
    private static BuildingBlockEntity begin(GameTestHelper h, boolean yard) {
        QaWorld.begin(h);
        BuildingBlockEntity post;
        if (yard) {
            var p = h.absolutePos(HOME); int radius = StructureConfig.CHECKPOINT_RADIUS.get(); StructurePlacement.Result placed;
            try { StructureConfig.CHECKPOINT_RADIUS.set(4); placed = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, p.getX() >> 4, p.getZ() >> 4); }
            finally { StructureConfig.CHECKPOINT_RADIUS.set(radius); }
            h.assertTrue(placed.success(), placed.message()); post = (BuildingBlockEntity) h.getLevel().getBlockEntity(placed.commandPost());
            post.change(new BuildingState(2, 0, 0, "", 0));
            var actor = QaPlayer.join(h.getLevel()); actor.snapTo(post.getBlockPos().getX() + .5, post.getBlockPos().getY(), post.getBlockPos().getZ() + 4.5, 180, 10);
            OutpostGrowth.expand(actor, post); actor.closeContainer(); QaPlayer.closeAll();
        } else {
            h.setBlock(HOME, BuildingRegistry.COMMAND_POST.get()); post = h.getBlockEntity(HOME, BuildingBlockEntity.class);
        }
        data(h).registerCommandPost(home(h, post));
        DefenderDuties.setMode(h.getLevel(), post, DefenderDuty.Mode.PATROL);
        return post;
    }
    private static HumanNpc assigned(GameTestHelper h, BuildingBlockEntity post, BlockPos position) {
        var npc = h.spawn(NpcRegistry.SURVIVOR.get(), h.relativePos(position)); npc.assignPost(home(h, post)); npc.getRandom().setSeed(918273);
        h.assertTrue(data(h).reserveDefender(home(h, post).key(), npc.getUUID(), 16), "Existing ledger member assigned without new deployment path");
        return npc;
    }
    private static void patrol(GameTestHelper h) {
        var post = begin(h, false); var npc = assigned(h, post, post.getBlockPos().offset(0, 0, 6)); boolean[] visited = new boolean[2];
        var egg = h.spawn(NpcRegistry.SURVIVOR.get(), HOME.offset(7, 0, 7));
        h.succeedWhen(() -> {
            for (int i = 0; i < 2; i++) { visited[i] |= DefenderDuties.waypoint(post, i).distToCenterSqr(npc.position()) < 1.5; }
            h.assertTrue(visited[0] && visited[1], "Real movement reaches both local waypoints");
            h.assertTrue(npc.localDuty().requests() > 0 && egg.localDuty().requests() == 0 && egg.commandPost().isEmpty(), "Egg is not adopted; only reserved defender receives orders");
            h.assertTrue(data(h).defenderCount(home(h, post).key()) == 1, "No quota changes or recruitment");
        });
    }
    private static void watch(GameTestHelper h) {
        var post = begin(h, true); var c = post.site().center();
        var a = assigned(h, post, c.offset(0, 1, 5)); var b = assigned(h, post, c.offset(2, 1, 5));
        h.succeedWhen(() -> {
            var id = post.duty().watch(); h.assertTrue(id != null, "A real assigned defender claims the watch slot");
            var owner = id.equals(a.getUUID()) ? a : b; var other = owner == a ? b : a;
            h.assertTrue(DefenderDuties.watchPosition(post).distToCenterSqr(owner.position()) < 1, "Vanilla AI physically arrives on west watch deck");
            h.assertTrue(owner.localDuty().watch() && !other.localDuty().watch() && data(h).defenderCount(home(h, post).key()) == 2, "Exactly one watch owner; no stacking or quota changes");
        });
    }
    private static void blocked(GameTestHelper h) {
        var post = begin(h, false); var goal = DefenderDuties.waypoint(post, 0);
        for (int x = -1; x <= 1; x++) { for (int z = -1; z <= 1; z++) {
            if (x == 0 && z == 0) { continue; }
            for (int y = 0; y < 4; y++) { h.getLevel().setBlockAndUpdate(goal.offset(x, y, z), Blocks.STONE.defaultBlockState()); }
        } }
        var npc = assigned(h, post, post.getBlockPos().offset(0, 0, 7)); int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
        h.runAfterDelay(240, () -> {
            h.assertTrue(npc.localDuty().requests() > 0 && npc.localDuty().requests() <= 3, "Unreachable path uses at most three requests then backoff, not per-tick retries");
            h.assertTrue(npc.position().distanceToSqr(goal.getX() + .5, goal.getY(), goal.getZ() + .5) > 1, "No teleport into unreachable enclosure");
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "Stuck duty requests no chunks (before=" + chunks + ", after=" + h.getLevel().getChunkSource().getLoadedChunksCount() + ")"); h.succeed();
        });
    }
    private static void reassign(GameTestHelper h) {
        var post = begin(h, true); var c = post.site().center();
        var a = assigned(h, post, c.offset(0, 1, 5)); var b = assigned(h, post, c.offset(2, 1, 5));
        HumanNpc[] former = {null}, replacement = {null}; boolean[] removed = {false};
        h.succeedWhen(() -> {
            if (former[0] == null && (a.localDuty().watch() || b.localDuty().watch())) {
                former[0] = a.localDuty().watch() ? a : b; replacement[0] = former[0] == a ? b : a;
                DefenderDuties.setMode(h.getLevel(), post, DefenderDuty.Mode.GUARD);
                DefenderDuties.setMode(h.getLevel(), post, DefenderDuty.Mode.PATROL);
                h.assertTrue(DefenderDuties.claimWatch(h.getLevel(), post, replacement[0]), "Rapid policy reset reserves exactly one replacement");
                former[0].localDuty().canUse();
                h.assertFalse(former[0].localDuty().watch(), "Revoked cached watch cannot overlap its replacement until the next periodic resolve");
            }
            h.assertTrue(former[0] != null, "First owner must resolve naturally");
            var watch = DefenderDuties.watchPosition(post);
            if (!removed[0] && watch.distToCenterSqr(replacement[0].position()) < 1) {
                replacement[0].remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED); removed[0] = true;
            }
            h.assertTrue(removed[0], "Replacement reaches deck before confirmed removal");
            h.assertTrue(post.duty().watch().equals(former[0].getUUID()) && watch.distToCenterSqr(former[0].position()) < 1
                    && data(h).defenderCount(home(h, post).key()) == 1, "Confirmed removal releases one slot and survivor returns without spawning or teleporting");
        });
    }
    private static void watchCombat(GameTestHelper h) {
        var post = begin(h, true); var c = post.site().center(); var npc = assigned(h, post, c.offset(0, 1, 5));
        net.minecraft.world.entity.monster.zombie.Zombie[] enemy = {null}; boolean[] left = {false};
        h.succeedWhen(() -> {
            var watch = DefenderDuties.watchPosition(post);
            if (enemy[0] == null && watch.distToCenterSqr(npc.position()) < 1) {
                enemy[0] = h.spawn(EntityTypes.ZOMBIE, h.relativePos(c.offset(4, 1, 3))); enemy[0].setNoAi(true); enemy[0].setHealth(1);
            }
            h.assertTrue(enemy[0] != null, "Watch owner first physically reaches its deck");
            left[0] |= watch.distToCenterSqr(npc.position()) > 9;
            h.assertTrue(left[0] && !enemy[0].isAlive(), "Watch owner leaves its station for real combat");
            h.assertTrue(watch.distToCenterSqr(npc.position()) < 1 && post.duty().watch().equals(npc.getUUID()), "Same owner returns to the watch after combat");
        });
    }
    private static void dormant(GameTestHelper h) {
        var post = begin(h, false); post.initializeMainBase(); data(h).registerMainBase(home(h, post));
        var npc = assigned(h, post, post.getBlockPos().offset(0, 0, 5));
        h.runAfterDelay(100, () -> {
            h.assertTrue(npc.localDuty().requests() == 0 && DefenderDuties.watchStatus(h.getLevel(), post) == 3 && npc.commandPost().isPresent(), "Dormant base suspends duties without losing its units");
            com.liamryan.standandhold.common.progression.HumanPointManager.setStage(h.getLevel().getServer(), com.liamryan.standandhold.common.progression.HumanStage.SPECIAL_PARASITE_DIVISION);
        });
        h.runAfterDelay(240, () -> { h.assertTrue(npc.localDuty().requests() > 0, "Duties resume after ordinary base activation"); h.succeed(); });
    }
    private static <T extends Mob> void combat(GameTestHelper h, net.minecraft.world.entity.EntityType<T> type) {
        var post = begin(h, false); var npc = assigned(h, post, post.getBlockPos().offset(-2, 0, 3));
        var enemy = h.spawn(type, h.relativePos(post.getBlockPos().offset(7, 0, -3))); enemy.setNoAi(true); enemy.setHealth(1); enemy.setPersistenceRequired();
        var animal = h.spawn(EntityTypes.COW, HOME.offset(-7, 0, -7)); animal.setNoAi(true); var actor = QaPlayer.join(h.getLevel());
        actor.snapTo(post.getBlockPos().getX() - 8, post.getBlockPos().getY(), post.getBlockPos().getZ() + 8, 0, 0);
        boolean[] left = {false};
        h.succeedWhen(() -> {
            left[0] |= npc.blockPosition().distSqr(post.getBlockPos().offset(-2, 0, 3)) > 16;
            h.assertTrue(!enemy.isAlive() && left[0], "Independent duty NPC pursues and kills " + type);
            h.assertTrue(npc.getTarget() == null && (DefenderDuties.waypoint(post, 0).distToCenterSqr(npc.position()) < 2
                    || DefenderDuties.waypoint(post, 1).distToCenterSqr(npc.position()) < 2), "Returns to actual local work after combat");
            h.assertFalse(npc.canAttack(animal) || npc.canAttack(actor), "Players and animals remain protected while on duty");
        });
    }
    private static void missing(GameTestHelper h) {
        var post = begin(h, false); var npc = assigned(h, post, post.getBlockPos().offset(0, 0, 5)); var key = home(h, post).key();
        h.runAfterDelay(60, () -> h.getLevel().setBlockAndUpdate(post.getBlockPos(), Blocks.AIR.defaultBlockState()));
        h.runAfterDelay(120, () -> {
            h.assertTrue(npc.commandPost().isEmpty() && data(h).defenderCount(key) == 0 && npc.isAlive(), "Missing loaded post releases duty/home, not the surviving unit"); h.succeed();
        });
    }
    private static void unloaded(GameTestHelper h) {
        QaWorld.begin(h); var npc = h.spawn(NpcRegistry.SURVIVOR.get(), HOME); var pos = h.absolutePos(HOME).offset(4096, 0, 4096);
        var home = new BuildingPosition(h.getLevel().dimension().identifier(), pos); data(h).registerCommandPost(home); npc.assignPost(home); data(h).reserveDefender(home.key(), npc.getUUID(), 16);
        h.assertFalse(h.getLevel().hasChunkAt(pos), "Far home deliberately unloaded"); int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
        h.runAfterDelay(100, () -> {
            h.assertTrue(npc.commandPost().orElseThrow().equals(home) && data(h).hasDefender(home.key(), npc.getUUID()) && npc.localDuty().requests() == 0, "Unloaded home is suspended, not treated as death");
            h.assertFalse(h.getLevel().hasChunkAt(pos), "Duty never loads absent home"); h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "No chunk growth"); h.succeed();
        });
    }
    private static void unresolved(GameTestHelper h) {
        var post = begin(h, true); var key = home(h, post).key(); var absent = UUID.randomUUID(); data(h).reserveDefender(key, absent, 16);
        post.changeDuty(post.duty().watch(absent)); var npc = assigned(h, post, post.getBlockPos().offset(2, 0, 4));
        h.runAfterDelay(100, () -> {
            h.assertTrue(post.duty().watch().equals(absent) && !npc.localDuty().watch() && data(h).defenderCount(key) == 2, "Missing entity lookup retains saved watch/quota rather than double assignment");
            data(h).releaseDefender(key, absent);
            h.assertTrue(DefenderDuties.claimWatch(h.getLevel(), post, npc) && post.duty().watch().equals(npc.getUUID()), "Confirmed roster release allows one replacement watch owner"); h.succeed();
        });
    }
    private static void menu(GameTestHelper h) {
        var post = begin(h, false); var actor = QaPlayer.join(h.getLevel()); var other = QaPlayer.join(h.getLevel()); var p = post.getBlockPos();
        actor.snapTo(p.getX() + .5, p.getY(), p.getZ() + 4.5, 180, 20);
        var menu = new BuildingMenu(1, actor.getInventory(), post, FoundationResources.get(h.getLevel().getServer())); actor.containerMenu = menu;
        h.assertFalse(menu.clickMenuButton(other, BuildingMenu.DUTY_GUARD), "Foreign menu packet refused");
        actor.snapTo(p.getX() + 20, p.getY(), p.getZ(), 0, 0);
        h.assertFalse(menu.clickMenuButton(actor, BuildingMenu.DUTY_GUARD), "Out-of-range duty packet refused");
        actor.snapTo(p.getX() + .5, p.getY(), p.getZ() + 4.5, 180, 20);
        actor.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
        h.assertFalse(menu.clickMenuButton(actor, BuildingMenu.DUTY_GUARD), "Spectator duty packet refused");
        actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var global = HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data(h)).getOrThrow(); var state = post.state();
        h.assertTrue(menu.clickMenuButton(actor, BuildingMenu.DUTY_GUARD), "Ordinary local player can choose Guard");
        h.getLevel().getChunkAt(p).tryMarkSaved();
        h.assertTrue(menu.clickMenuButton(actor, BuildingMenu.DUTY_GUARD) && !h.getLevel().getChunkAt(p).isUnsaved(), "Duplicate policy selection does not dirty every tick");
        h.assertTrue(menu.clickMenuButton(actor, BuildingMenu.DUTY_PATROL), "Patrol is reversible through authoritative menu");
        var dispatcher = h.getLevel().getServer().getCommands().getDispatcher(); var source = actor.createCommandSourceStack();
        try {
            boolean denied = false;
            try { dispatcher.execute("standandhold commandpost duty guard", source); } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { denied = true; }
            h.assertTrue(denied && post.duty().mode() == DefenderDuty.Mode.PATROL, "Non-admin command cannot change policy");
            var op = source.withPermission(h.getLevel().getServer().createCommandSourceStack().permissions());
            h.assertTrue(dispatcher.execute("standandhold commandpost duty status", op) == 1 && actor.received("Duties at"), "Admin can inspect bounded local duty status");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) { throw new IllegalStateException(failure); }
        var saved = post.saveWithFullMetadata(h.getLevel().registryAccess()); saved.getCompoundOrEmpty("duties").putString("future_note", "keep"); post = reload(h, post, saved);
        h.assertTrue(post.duty().mode() == DefenderDuty.Mode.PATROL && post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("duties").getStringOr("future_note", "").equals("keep"), "Known policy saves and retains extensions");
        var raw = saved.getCompoundOrEmpty("duties"); raw.putInt("version", 99); post = reload(h, post, saved);
        menu = new BuildingMenu(2, actor.getInventory(), post, FoundationResources.get(h.getLevel().getServer())); actor.containerMenu = menu;
        h.assertFalse(post.dutySupported() || menu.clickMenuButton(actor, BuildingMenu.DUTY_GUARD), "Future policy disables only duty actions");
        h.assertTrue(post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("duties").equals(raw) && post.state().equals(state)
                && global.equals(HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data(h)).getOrThrow()), "Future duty/state/resources retained without rewrite"); h.succeed();
    }
    private static BuildingBlockEntity reload(GameTestHelper h, BuildingBlockEntity old, CompoundTag tag) {
        var post = (BuildingBlockEntity) net.minecraft.world.level.block.entity.BlockEntity.loadStatic(old.getBlockPos(), old.getBlockState(), tag, h.getLevel().registryAccess());
        h.assertTrue(post != null, "Duty extension must load without losing the building"); h.getLevel().setBlockEntity(post); return post;
    }
    private static void response(GameTestHelper h) {
        var post = begin(h, false); var npc = assigned(h, post, post.getBlockPos().offset(0, 0, 5)); var target = post.getBlockPos().offset(6, 0, 5);
        npc.assignPatrol(target, h.getLevel().getGameTime() + 160);
        h.runAfterDelay(120, () -> h.assertTrue(npc.hasResponseOrder() && npc.localDuty().requests() == 0 && target.distToCenterSqr(npc.position()) < 5, "Temporary response gets exclusive movement and actually arrives"));
        h.runAfterDelay(300, () -> {
            h.assertTrue(!npc.hasResponseOrder() && npc.localDuty().requests() > 0, "Local duty resumes after response expiry"); h.succeed();
        });
    }
}
