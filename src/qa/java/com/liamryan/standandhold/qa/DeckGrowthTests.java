package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StructureConfig;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

final class DeckGrowthTests {
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("deck_apply_direct_preserves", h -> applied(h, false));
        tests.put("deck_apply_apron_preserves", h -> applied(h, true));
        tests.put("deck_apply_fault_first", h -> fault(h, 0));
        tests.put("deck_apply_fault_middle", h -> fault(h, 31));
        tests.put("deck_apply_fault_last", h -> fault(h, 62));
        tests.put("deck_apply_late_expected_conflict", DeckGrowthTests::conflict);
        tests.put("deck_apply_late_entity", DeckGrowthTests::entity);
        tests.put("deck_apply_protected_callback", DeckGrowthTests::protectedCallback);
        tests.put("deck_apply_anchor_loss", DeckGrowthTests::anchor);
        tests.put("deck_apply_all_after_recovery", DeckGrowthTests::recover);
        tests.put("deck_apply_unknown_receipts", DeckGrowthTests::unknown);
        tests.put("deck_apply_manual_automatic", DeckGrowthTests::automatic);
        tests.put("deck_apply_pause_retry", DeckGrowthTests::retry);
        tests.put("deck_apply_stage_level_config_actor", DeckGrowthTests::gates);
        tests.put("deck_apply_stale_two_actors", DeckGrowthTests::stale);
        tests.put("deck_apply_command_menu", DeckGrowthTests::commands);
        tests.put("deck_apply_navigation", DeckGrowthTests::navigation);
        tests.put("deck_apply_unloaded", DeckGrowthTests::unloaded);
        return DeckPreviewTests.scope(tests);
    }
    private static BuildingBlockEntity tile(GameTestHelper h, BlockPos pos) { return (BuildingBlockEntity) h.getLevel().getBlockEntity(pos); }
    private static void refuse(GameTestHelper h, Runnable action) {
        try { action.run(); h.fail("Expected refusal without repair/replay"); } catch (IllegalArgumentException | IllegalStateException expected) { }
    }
    private static void conserved(GameTestHelper h, BuildingBlockEntity post, DeckPreviewTests.Snapshot before) {
        var after = DeckPreviewTests.snapshot(h, post);
        h.assertTrue(before.human().equals(after.human()) && before.threat().equals(after.threat()) && before.entities().equals(after.entities()) && before.chunks()==after.chunks(), "Global resources/rosters/threat and full entity state/loaded chunks conserved");
        h.assertTrue(before.tiles().keySet().equals(after.tiles().keySet()), "No original building moved or regenerated");
        for (var pos : before.tiles().keySet()) {
            var prior = before.tiles().get(pos).copy(); var now = after.tiles().get(pos).copy();
            if (pos.equals(post.getBlockPos())) {
                prior.getCompoundOrEmpty("site").remove("deck_operation"); now.getCompoundOrEmpty("site").remove("deck_operation");
                prior.getCompoundOrEmpty("site").putInt("applied_revision", 2); now.getCompoundOrEmpty("site").putInt("applied_revision", 2);
            }
            h.assertTrue(prior.equals(now), "All building data/old receipts/unknown fields retained except exact deck receipt/revision");
        }
    }
    private static void applied(GameTestHelper h, boolean apron) {
        var f = DeckPreviewTests.begin(h, apron); h.runAfterDelay(3, () -> {
            var post = f.post(); var home = new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos());
            var watch = DefenderDuties.watchPosition(post); var npc = h.spawn(NpcRegistry.SURVIVOR.get(), h.relativePos(watch));
            npc.setNoAi(true); npc.assignPost(home); npc.setHealth(13); npc.setItemSlot(EquipmentSlot.MAINHAND, Items.STICK.getDefaultInstance());
            var data = HumanWorldData.get(h.getLevel().getServer()); h.assertTrue(data.reserveDefender(home.key(), npc.getUUID(), 6), "Existing watch identity");
            DefenderDuties.setMode(h.getLevel(), post, DefenderDuty.Mode.PATROL); h.assertTrue(DefenderDuties.claimWatch(h.getLevel(), post, npc), "Existing west watch reservation");
            var before = DeckPreviewTests.snapshot(h, post); var plan = LevelThreePreview.inspect(h.getLevel(), post);
            OutpostGrowth.expand(f.player(), post); h.assertTrue(h.getLevel().getChunkAt(post.getBlockPos()).isUnsaved(),"Applied layout/receipt marks its chunk dirty"); conserved(h, post, before);
            var op = post.deckOperation().orElseThrow(); h.assertTrue(post.site().appliedRevision()==3 && op.phase()==ExpansionOperation.Phase.APPLIED && op.changes().equals(plan.changes()), "Exact planned delta applied and sealed once");
            for (var entry : before.blocks().entrySet()) {
                var edit = op.changes().stream().filter(c -> c.pos().equals(entry.getKey())).findFirst();
                h.assertTrue(edit.isPresent() ? NbtUtils.writeBlockState(h.getLevel().getBlockState(entry.getKey())).equals(edit.get().after())
                        : h.getLevel().getBlockState(entry.getKey()).equals(entry.getValue()), "Only the 63 authored cells change");
            }
            h.assertTrue(watch.equals(DefenderDuties.watchPosition(post)) && post.duty().watch().equals(npc.getUUID()), "West watch support/reservation remains valid");
            var sealed = DeckPreviewTests.snapshot(h, post); refuse(h, () -> OutpostGrowth.expand(f.player(), post)); DeckPreviewTests.unchanged(h, post, sealed);
            post.change(post.state().supplies(91)); post.change(new BuildingState(1,91,3,"",0));
            h.assertTrue(OutpostGrowth.status(h.getLevel(), post)==OutpostGrowth.EXPANDED && post.site().appliedRevision()==3, "Legitimate storage/stage/local-level drop never demolishes or requests repair"); h.succeed();
        });
    }
    private static ExpansionApply.Access wrap(ExpansionApply.Access base, Predicate<ExpansionOperation.Change> write) {
        return new ExpansionApply.Access() {
            public boolean valid() { return base.valid(); } public CompoundTag read(BlockPos pos) { return base.read(pos); }
            public boolean write(ExpansionOperation.Change cell) { return write.test(cell); }
            public String protectedDigest() { return base.protectedDigest(); } public void store(ExpansionOperation op) { base.store(op); }
        };
    }
    private static void fault(GameTestHelper h, int at) {
        var f = DeckPreviewTests.begin(h, false); h.runAfterDelay(3, () -> {
            var attempt = LevelThreeGrowth.prepare(f.player(), f.post()); var before = DeckPreviewTests.snapshot(h, f.post()); int[] index={0};
            var result = ExpansionApply.apply(attempt.operation(), wrap(attempt.access(), cell -> index[0]++ != at && attempt.access().write(cell)));
            h.assertFalse(result.success(), "Injected refusal must fail at " + at); conserved(h, f.post(), before);
            var op=f.post().deckOperation().orElseThrow(); h.assertTrue(op.phase()==ExpansionOperation.Phase.REVIEW && op.index()==at && f.post().site().appliedRevision()==2, "Exact diagnostic prefix retained, source revision unchanged");
            for (int i=0;i<63;i++) { var cell=op.changes().get(i); h.assertTrue(NbtUtils.writeBlockState(h.getLevel().getBlockState(cell.pos())).equals(i<at?cell.after():cell.before()), "No writes after refused cell or destructive rollback"); }
            var partial=DeckPreviewTests.snapshot(h,f.post()); refuse(h,()->LevelThreeGrowth.applyNow(f.player(),f.post())); refuse(h,()->OutpostExpansion.recover(f.player(),f.post()));
            OutpostGrowth.tick(h.getLevel(),f.post()); DeckPreviewTests.unchanged(h,f.post(),partial); h.succeed();
        });
    }
    private static void conflict(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false); h.runAfterDelay(3,()->{
            var a=LevelThreeGrowth.prepare(f.player(),f.post()); int[] count={0}; var target=a.operation().changes().get(1).pos();
            var result=ExpansionApply.apply(a.operation(),wrap(a.access(),cell->{boolean ok=a.access().write(cell);if(count[0]++==0){h.getLevel().setBlockAndUpdate(target,Blocks.CHEST.defaultBlockState());((ChestBlockEntity)h.getLevel().getBlockEntity(target)).setItem(0,Items.DIAMOND.getDefaultInstance());}return ok;}));
            h.assertTrue(!result.success() && count[0]==1 && f.post().deckOperation().orElseThrow().index()==1,"Late expected-state conflict stops before overwrite");
            h.assertTrue(((ChestBlockEntity)h.getLevel().getBlockEntity(target)).getItem(0).is(Items.DIAMOND),"Late player container/items not rolled back"); h.succeed();
        });
    }
    private static void entity(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false); h.runAfterDelay(3,()->{
            var c=f.post().site().center(); var npc=h.spawn(NpcRegistry.SURVIVOR.get(),h.relativePos(c.offset(0,1,3))); npc.setNoAi(true);npc.setHealth(11);
            var a=LevelThreeGrowth.prepare(f.player(),f.post());int[] count={0};var target=a.operation().changes().get(1).pos();
            var result=ExpansionApply.apply(a.operation(),wrap(a.access(),cell->{boolean ok=a.access().write(cell);if(count[0]++==0){npc.snapTo(target.getX()+.5,target.getY()+.5,target.getZ()+.5,0,0);}return ok;}));
            h.assertTrue(!result.success() && count[0]==1 && npc.isAlive() && npc.getHealth()==11 && npc.getX()==target.getX()+.5,"Observed actor entering a roof edit is not buried/relocated; writes stop");
            h.assertTrue(NbtUtils.writeBlockState(h.getLevel().getBlockState(target)).equals(a.operation().changes().get(1).before()),"Occupied roof slab retained"); h.succeed();
        });
    }
    private static void protectedCallback(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var a=LevelThreeGrowth.prepare(f.player(),f.post());int[] i={0};var lab=tile(h,f.post().getBlockPos().offset(-2,0,-1));
            var result=ExpansionApply.apply(a.operation(),wrap(a.access(),cell->{boolean ok=a.access().write(cell);if(i[0]++==0){lab.change(lab.state().supplies(99));}return ok;}));
            h.assertTrue(!result.success() && f.post().deckOperation().orElseThrow().phase()==ExpansionOperation.Phase.REVIEW && lab.state().supplies()==99,"Changed protected lab cannot seal; callback state not overwritten");
            refuse(h,()->OutpostExpansion.recover(f.player(),f.post()));h.succeed();
        });
    }
    private static void anchor(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var a=LevelThreeGrowth.prepare(f.player(),f.post());int[] i={0};var pos=f.post().getBlockPos();
            var result=ExpansionApply.apply(a.operation(),wrap(a.access(),cell->{boolean ok=a.access().write(cell);if(i[0]++==0){h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());}return ok;}));
            h.assertTrue(!result.success() && i[0]==1 && h.getLevel().getBlockEntity(pos)==null,"Anchor loss stops immediately without reconstruction");h.succeed();
        });
    }
    private static void recover(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var a=LevelThreeGrowth.prepare(f.player(),f.post());int[] i={0};
            var result=ExpansionApply.apply(a.operation(),wrap(a.access(),cell->{boolean ok=a.access().write(cell);return i[0]++==62?false:ok;}));
            h.assertFalse(result.success(),"Interrupted callback after last actual write remains unsealed");
            var before=DeckPreviewTests.snapshot(h,f.post()); var blocks=before.blocks(); OutpostExpansion.recover(f.player(),f.post());
            h.assertTrue(DeckPreviewTests.snapshot(h,f.post()).blocks().equals(blocks),"Recovery writes no blocks");conserved(h,f.post(),before);
            h.assertTrue(f.post().site().appliedRevision()==3 && f.post().deckOperation().orElseThrow().phase()==ExpansionOperation.Phase.APPLIED,"Explicit all-after recovery seals exact metadata once");
            refuse(h,()->OutpostExpansion.recover(f.player(),f.post()));h.succeed();
        });
    }
    private static BuildingBlockEntity load(GameTestHelper h, BuildingBlockEntity post, CompoundTag tag) {
        var loaded=(BuildingBlockEntity)BlockEntity.loadStatic(post.getBlockPos(),post.getBlockState(),tag,h.getLevel().registryAccess());h.assertTrue(loaded!=null,"Unknown extensions do not crash ordinary tile load");h.getLevel().setBlockEntity(loaded);return loaded;
    }
    private static void unknown(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var a=LevelThreeGrowth.prepare(f.player(),f.post());var saved=f.post().saveWithFullMetadata(h.getLevel().registryAccess());var future=a.operation().save();future.putInt("operation_version",4);
            for(Tag raw:List.of(future,StringTag.valueOf("opaque"),new CompoundTag())){
                var tag=saved.copy();tag.getCompoundOrEmpty("site").put("deck_operation",raw.copy());var post=load(h,f.post(),tag);
                refuse(h,()->LevelThreeGrowth.applyNow(h.getLevel(),post));OutpostGrowth.setMode(h.getLevel(),post,OutpostGrowth.Mode.AUTOMATIC);OutpostGrowth.tick(h.getLevel(),post);
                h.assertTrue(post.growthSettings().pause()==OutpostGrowth.REVIEW && post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").get("deck_operation").equals(raw),"Future/malformed intent retained and paused, never replayed");
                post.change(post.state().supplies(93));h.assertTrue(post.state().supplies()==93,"Unknown growth does not disable normal storage");
            }
            var tag=saved.copy();var op=a.operation().save();op.putString("future_note","keep");op.getListOrEmpty("cells").getCompoundOrEmpty(0).putString("future_cell","keep");tag.getCompoundOrEmpty("site").put("deck_operation",op);
            var post=load(h,f.post(),tag);post.storeDeckOperation(a.operation().withPhase(ExpansionOperation.Phase.REVIEW,0,"review"));var kept=post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").getCompoundOrEmpty("deck_operation");
            h.assertTrue(kept.getStringOr("future_note","").equals("keep")&&kept.getListOrEmpty("cells").getCompoundOrEmpty(0).getStringOr("future_cell","").equals("keep"),"Known receipt transitions retain unknown operation/cell fields");h.succeed();
        });
    }
    private static void automatic(GameTestHelper h) {
        DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var p=h.absolutePos(new BlockPos(48,2,32));var placed=StructurePlacement.place(h.getLevel(),StructureKind.CHECKPOINT,p.getX()>>4,p.getZ()>>4);h.assertTrue(placed.success(),placed.message());var post=tile(h,placed.commandPost());post.change(new BuildingState(5,88,4,"",0));
            OutpostGrowth.setMode(h.getLevel(),post,OutpostGrowth.Mode.AUTOMATIC);OutpostGrowth.tick(h.getLevel(),post);
            h.assertTrue(post.site().appliedRevision()==2&&post.deckOperation().isEmpty(),"One automatic attempt applies yard only, even at local level 5");
            OutpostGrowth.tick(h.getLevel(),post);h.assertTrue(post.site().appliedRevision()==3&&post.state().equals(new BuildingState(5,88,4,"",0)),"Separate automatic cadence applies next eligible deck without purchase/refill");
            var before=DeckPreviewTests.snapshot(h,post);for(int i=0;i<10;i++){OutpostGrowth.tick(h.getLevel(),post);}DeckPreviewTests.unchanged(h,post,before);h.succeed();
        });
    }
    private static void retry(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var post=f.post();OutpostGrowth.setMode(h.getLevel(),post,OutpostGrowth.Mode.AUTOMATIC);var pos=post.site().center().offset(2,6,3);h.getLevel().setBlockAndUpdate(pos,Blocks.GOLD_BLOCK.defaultBlockState());OutpostGrowth.tick(h.getLevel(),post);
            h.assertTrue(post.growthSettings().pause()==OutpostGrowth.TERRAIN&&post.deckOperation().isEmpty(),"Obstructed preflight saves pause without intent/writes");h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
            var before=DeckPreviewTests.snapshot(h,post);OutpostGrowth.tick(h.getLevel(),post);DeckPreviewTests.unchanged(h,post,before);
            OutpostGrowth.expand(f.player(),post);h.assertTrue(post.site().appliedRevision()==3&&post.growthSettings().pause()==0,"Explicit Retry resumes after obstruction removed");h.succeed();
        });
    }
    private static void gates(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var post=f.post();post.change(new BuildingState(2,71,3,"",0));var before=DeckPreviewTests.snapshot(h,post);refuse(h,()->OutpostGrowth.expand(f.player(),post));DeckPreviewTests.unchanged(h,post,before);
            post.change(new BuildingState(3,71,3,"",0));StructureConfig.EXPANSION.set(false);before=DeckPreviewTests.snapshot(h,post);refuse(h,()->LevelThreeGrowth.applyNow(f.player(),post));DeckPreviewTests.unchanged(h,post,before);StructureConfig.EXPANSION.set(true);
            f.player().setGameMode(GameType.SPECTATOR);before=DeckPreviewTests.snapshot(h,post);refuse(h,()->LevelThreeGrowth.applyNow(f.player(),post));DeckPreviewTests.unchanged(h,post,before);f.player().setGameMode(GameType.SURVIVAL);
            var disabled=LevelThreeGrowth.prepare(f.player(),post);StructureConfig.EXPANSION.set(false);before=DeckPreviewTests.snapshot(h,post);refuse(h,()->ExpansionApply.apply(disabled.operation(),disabled.access()));DeckPreviewTests.unchanged(h,post,before);StructureConfig.EXPANSION.set(true);
            var staged=LevelThreeGrowth.prepare(f.player(),post);com.liamryan.standandhold.common.progression.HumanPointManager.setStage(h.getLevel().getServer(),com.liamryan.standandhold.common.progression.HumanStage.LOCAL_ARMY_RESPONSE);before=DeckPreviewTests.snapshot(h,post);refuse(h,()->ExpansionApply.apply(staged.operation(),staged.access()));DeckPreviewTests.unchanged(h,post,before);
            var remote=LevelThreeGrowth.prepare(f.player(),post);var p=post.getBlockPos();f.player().snapTo(p.getX()+15,p.getY(),p.getZ(),0,0);before=DeckPreviewTests.snapshot(h,post);refuse(h,()->ExpansionApply.apply(remote.operation(),remote.access()));DeckPreviewTests.unchanged(h,post,before);f.player().snapTo(p.getX()+.5,p.getY(),p.getZ()+4.5,180,16);
            var a=LevelThreeGrowth.prepare(f.player(),post);post.change(new BuildingState(2,71,3,"",0));before=DeckPreviewTests.snapshot(h,post);refuse(h,()->ExpansionApply.apply(a.operation(),a.access()));DeckPreviewTests.unchanged(h,post,before);h.succeed();
        });
    }
    private static void stale(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var other=QaPlayer.join(h.getLevel());var p=f.post().getBlockPos();other.snapTo(p.getX()-.5,p.getY(),p.getZ()+3.5,180,16);
            var first=LevelThreeGrowth.prepare(f.player(),f.post());var second=LevelThreeGrowth.prepare(other,f.post());
            h.assertTrue(ExpansionApply.apply(first.operation(),first.access()).success(),"First real actor wins");var before=DeckPreviewTests.snapshot(h,f.post());refuse(h,()->ExpansionApply.apply(second.operation(),second.access()));DeckPreviewTests.unchanged(h,f.post(),before);h.succeed();
        });
    }
    private static void commands(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            var server=h.getLevel().getServer();var menu=new BuildingMenu(71,f.player().getInventory(),f.post(),FoundationResources.get(server));f.player().containerMenu=menu;
            h.assertTrue(menu.clickMenuButton(f.player(),BuildingMenu.EXPAND)&&f.post().site().appliedRevision()==3,"Ordinary authoritative menu action applies deck");
            h.assertFalse(menu.clickMenuButton(f.player(),BuildingMenu.EXPAND),"Duplicate menu action cannot rebuild");
            var dispatcher=server.getCommands().getDispatcher();var source=f.player().createCommandSourceStack().withPermission(server.createCommandSourceStack().permissions());var before=DeckPreviewTests.snapshot(h,f.post());
            try{h.assertTrue(dispatcher.execute("standandhold outpost layout",source)==1&&f.player().received("already applied"),"Admin layout reports sealed deck instead of older yard");dispatcher.execute("standandhold outpost status",source);}catch(com.mojang.brigadier.exceptions.CommandSyntaxException failure){throw new IllegalStateException(failure);}
            DeckPreviewTests.unchanged(h,f.post(),before);h.succeed();
        });
    }
    private static void navigation(GameTestHelper h) {
        var f=DeckPreviewTests.begin(h,false);h.runAfterDelay(3,()->{
            OutpostGrowth.expand(f.player(),f.post());var c=f.post().site().center();var npc=h.spawn(NpcRegistry.SURVIVOR.get(),h.relativePos(c.offset(0,1,5)));npc.setNoAi(true);npc.setOnGround(true);
            for(var goal:List.of(c.offset(0,1,0),c.offset(-2,1,-1),c.offset(2,1,-3),c.offset(-4,3,0),c.offset(2,5,0),DefenderDuties.waypoint(f.post(),0),DefenderDuties.waypoint(f.post(),1))){var path=npc.getNavigation().createPath(goal,0);h.assertTrue(path!=null&&path.canReach(),"Vanilla navigation reaches lower buildings/watch/upper deck/duties: "+goal.toShortString());}
            for(int index=0;index<2;index++){h.assertTrue(LocalDutyGoal.safeDestination(h.getLevel(),npc,DefenderDuties.waypoint(f.post(),index)),"Existing duty destinations remain physically clear of the new stairs");}
            npc.removeAllGoals(goal->true);npc.targetSelector.removeAllGoals(goal->true);npc.setNoAi(false);
            h.assertTrue(npc.getNavigation().moveTo(npc.getNavigation().createPath(c.offset(2,5,0),0),1),"Actual zero-range vanilla upper-floor navigation accepted");boolean[] upper={false};
            h.succeedWhen(()->{
                if(!upper[0]){h.assertTrue(npc.position().distanceToSqr(c.getX()+2.5,c.getY()+5,c.getZ()+.5)<1,"Real human physically walks up the authored stairs: offset="+npc.position().subtract(c.getX(),c.getY(),c.getZ())+", onGround="+npc.onGround()+", done="+npc.getNavigation().isDone()+", noAi="+npc.isNoAi());upper[0]=true;h.assertTrue(npc.getNavigation().moveTo(npc.getNavigation().createPath(c.offset(0,1,5),0),1),"Actual zero-range vanilla return path accepted");h.assertTrue(false,"Return downstairs");}
                h.assertTrue(npc.position().distanceToSqr(c.getX()+.5,c.getY()+1,c.getZ()+5.5)<1,"Same human physically returns to the entrance");
            });
        });
    }
    private static void unloaded(GameTestHelper h) {
        QaWorld.begin(h);var pos=new BlockPos(29000008,65,29000007);h.assertFalse(h.getLevel().hasChunkAt(pos),"Unloaded fixture");var post=new BuildingBlockEntity(pos,BuildingRegistry.COMMAND_POST.get().defaultBlockState());post.setLevel(h.getLevel());int chunks=h.getLevel().getChunkSource().getLoadedChunksCount();
        refuse(h,()->LevelThreeGrowth.applyNow(h.getLevel(),post));h.assertTrue(chunks==h.getLevel().getChunkSource().getLoadedChunksCount(),"No unloaded chunk requested by application");h.succeed();
    }
}
