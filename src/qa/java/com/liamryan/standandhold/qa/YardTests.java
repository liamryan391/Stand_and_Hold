package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.threat.ThreatWorldData;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;

final class YardTests {
    private record Fixture(BuildingBlockEntity post, QaPlayer player) { }
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("yard_direct_preserves_state", YardTests::direct);
        tests.put("yard_reuses_apron_receipt", YardTests::apron);
        tests.put("yard_edited_wall_container", h -> blocked(h, 0));
        tests.put("yard_missing_wall", h -> blocked(h, 1));
        tests.put("yard_watch_headroom", h -> blocked(h, 2));
        tests.put("yard_damaged_apron", h -> blocked(h, 3));
        tests.put("yard_custom_build_preserved", YardTests::custom);
        tests.put("yard_fault_first", h -> fault(h, 0));
        tests.put("yard_fault_middle", h -> fault(h, 91));
        tests.put("yard_fault_last", h -> fault(h, 182));
        tests.put("yard_fault_apron_middle", h -> fault(h, 23, true));
        tests.put("yard_supported_entity", YardTests::supportedEntity);
        tests.put("yard_protected_callback", YardTests::callback);
        tests.put("yard_anchor_loss", YardTests::anchor);
        tests.put("yard_all_after_recovery", YardTests::recover);
        tests.put("yard_unknown_data", YardTests::unknown);
        tests.put("yard_navigation", YardTests::navigation);
        tests.put("yard_readonly_command", YardTests::command);
        var scoped = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.forEach((name,test) -> scoped.put(name,h -> {
            boolean expansion=StructureConfig.EXPANSION.get(),deploy=NpcConfig.DEPLOY.get(),raids=ResponseConfig.RAIDS.get(),reinforce=ResponseConfig.REINFORCEMENTS.get();
            StructureConfig.EXPANSION.set(true); NpcConfig.DEPLOY.set(false); ResponseConfig.RAIDS.set(false); ResponseConfig.REINFORCEMENTS.set(false);
            h.testInfo.addListener(new GameTestListener(){
                private void restore(){StructureConfig.EXPANSION.set(expansion);NpcConfig.DEPLOY.set(deploy);ResponseConfig.RAIDS.set(raids);ResponseConfig.REINFORCEMENTS.set(reinforce);}
                public void testStructureLoaded(GameTestInfo i){} public void testPassed(GameTestInfo i,GameTestRunner r){restore();}
                public void testFailed(GameTestInfo i,GameTestRunner r){restore();} public void testAddedForRerun(GameTestInfo a,GameTestInfo b,GameTestRunner r){}
            }); test.accept(h);
        })); return scoped;
    }
    private static Fixture begin(GameTestHelper h){
        QaWorld.begin(h); var p=h.absolutePos(new BlockPos(32,2,32)); int radius=StructureConfig.CHECKPOINT_RADIUS.get(); StructurePlacement.Result placed;
        try{StructureConfig.CHECKPOINT_RADIUS.set(4);placed=StructurePlacement.place(h.getLevel(),StructureKind.CHECKPOINT,p.getX()>>4,p.getZ()>>4);}finally{StructureConfig.CHECKPOINT_RADIUS.set(radius);}
        h.assertTrue(placed.success(),placed.message()); var post=(BuildingBlockEntity)h.getLevel().getBlockEntity(placed.commandPost());
        post.change(new BuildingState(2,22,3,"",0));
        tile(h,post.getBlockPos().offset(-2,0,-1)).change(new BuildingState(1,31,4,"standandhold:field_communications",5,false));
        tile(h,post.getBlockPos().offset(3,0,-2)).change(new BuildingState(1,17,2,"",0));
        var player=QaPlayer.join(h.getLevel()); player.snapTo(post.getBlockPos().getX()+.5,post.getBlockPos().getY(),post.getBlockPos().getZ()+4.5,180,16);player.setYHeadRot(180);
        return new Fixture(post,player);
    }
    private static BuildingBlockEntity tile(GameTestHelper h,BlockPos p){return (BuildingBlockEntity)h.getLevel().getBlockEntity(p);}
    private static ExpansionOperation intent(GameTestHelper h,Fixture f){
        var plan=LevelTwoGrowth.inspect(f.player(),f.post());
        return new ExpansionOperation(UUID.randomUUID(),plan.site().id(),plan.changes(),OutpostExpansion.yardDigest(h.getLevel(),f.post(),plan.site(),plan.changes()),ExpansionOperation.Phase.PREPARED,0,"",plan.site().appliedRevision(),2);
    }
    private record Snapshot(Tag human,Tag threat,CompoundTag post,CompoundTag lab,CompoundTag crate,int chunks){}
    private static Snapshot snapshot(GameTestHelper h,BuildingBlockEntity p){
        var level=h.getLevel();return new Snapshot(HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE,HumanWorldData.get(level.getServer())).getOrThrow(),
                ThreatWorldData.CODEC.encodeStart(NbtOps.INSTANCE,ThreatWorldData.get(level.getServer())).getOrThrow(),p.saveWithFullMetadata(level.registryAccess()),
                tile(h,p.getBlockPos().offset(-2,0,-1)).saveWithFullMetadata(level.registryAccess()),tile(h,p.getBlockPos().offset(3,0,-2)).saveWithFullMetadata(level.registryAccess()),level.getChunkSource().getLoadedChunksCount());
    }
    private static void conserved(GameTestHelper h,BuildingBlockEntity post,Snapshot before){
        var now=snapshot(h,post);
        h.assertTrue(now.human().equals(before.human())&&now.threat().equals(before.threat())&&now.lab().equals(before.lab())&&now.crate().equals(before.crate())
                &&now.chunks()==before.chunks()&&post.state().equals(BuildingState.CODEC.parse(NbtOps.INSTANCE,before.post().getCompoundOrEmpty("building")).getOrThrow()),"Buildings/work/resources/rosters/cooldowns/chunks conserved");
    }
    private static void refuse(GameTestHelper h,Runnable action){try{action.run();h.fail("Expected protected yard refusal");}catch(IllegalArgumentException|IllegalStateException expected){}}
    private static void direct(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var home=new BuildingPosition(h.getLevel().dimension().identifier(),f.post().getBlockPos());
            var npc=h.spawn(NpcRegistry.SURVIVOR.get(),h.relativePos(f.post().site().center().offset(2,1,1)));npc.setNoAi(true);npc.assignPost(home);
            h.assertTrue(HumanWorldData.get(h.getLevel().getServer()).reserveDefender(home.key(),npc.getUUID(),5),"Real assigned defender before growth");
            var before=snapshot(h,f.post());var plan=LevelTwoGrowth.inspect(f.player(),f.post());
            h.assertTrue(snapshot(h,f.post()).equals(before)&&plan.changes().size()==183,"Preview is completely read-only");
            OutpostGrowth.expand(f.player(),f.post());conserved(h,f.post(),before);
            h.assertTrue(f.post().site().appliedRevision()==2&&f.post().yardOperation().orElseThrow().phase()==ExpansionOperation.Phase.APPLIED
                    &&f.post().siteOperation().isEmpty()&&npc.isAlive()&&npc.commandPost().orElseThrow().equals(home),"Direct source 0 yard, same real defender/home, no manufactured apron receipt");
            var c=f.post().site().center();for(int z=-3;z<=3;z++){h.assertTrue(h.getLevel().getBlockState(c.offset(4,1,z)).isAir(),"Obsolete east wall opened");}
            refuse(h,()->OutpostGrowth.expand(f.player(),f.post()));h.succeed();
        });
    }
    private static void apron(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            h.assertTrue(OutpostExpansion.applyNow(f.player(),f.post()).success(),"Legacy apron still supported");
            var saved=f.post().saveWithFullMetadata(h.getLevel().registryAccess()); var raw=saved.getCompoundOrEmpty("site").getCompoundOrEmpty("operation");
            raw.putString("future_note","keep");raw.getListOrEmpty("cells").getCompoundOrEmpty(0).putString("future_cell","keep");
            var post=load(h,f.post(),saved);post.change(post.state().supplies(9));var old=post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").get("operation").copy();
            var before=snapshot(h,post);h.assertTrue(LevelTwoGrowth.inspect(f.player(),post).changes().size()==46,"Existing apron reused rather than rewritten");
            OutpostGrowth.expand(f.player(),post);conserved(h,post,before);
            h.assertTrue(post.site().appliedRevision()==2&&post.yardOperation().orElseThrow().sourceRevision()==1
                    &&post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").get("operation").equals(old),"Entire sealed apron/unknown fields preserved byte-for-byte in NBT");h.succeed();
        });
    }
    private static void blocked(GameTestHelper h,int kind){
        var f=begin(h);h.runAfterDelay(3,()->{
            if(kind==3){h.assertTrue(OutpostExpansion.applyNow(f.player(),f.post()).success(),"Source apron");}
            var c=f.post().site().center();var p=kind==2?c.offset(-4,4,0):kind==3?c.offset(-6,0,-6):c.offset(4,1,0);
            h.getLevel().setBlockAndUpdate(p,(kind==0?Blocks.CHEST:kind==2?Blocks.GOLD_BLOCK:Blocks.AIR).defaultBlockState());
            if(kind==0){((ChestBlockEntity)h.getLevel().getBlockEntity(p)).setItem(0,Items.DIAMOND.getDefaultInstance());}
            var before=snapshot(h,f.post()); refuse(h,()->LevelTwoGrowth.applyNow(f.player(),f.post()));
            h.assertTrue(snapshot(h,f.post()).equals(before)&&f.post().yardOperation().isEmpty(),"Refusal has no intent, write, loss or repair");
            if(kind==0){h.assertTrue(((ChestBlockEntity)h.getLevel().getBlockEntity(p)).getItem(0).is(Items.DIAMOND),"Custom wall container contents retained");}h.succeed();
        });
    }
    private static void custom(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var pos=f.post().site().center().offset(2,1,2);h.getLevel().setBlockAndUpdate(pos,Blocks.GOLD_BLOCK.defaultBlockState());
            var before=snapshot(h,f.post());OutpostGrowth.expand(f.player(),f.post());conserved(h,f.post(),before);
            h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.GOLD_BLOCK),"Non-interfering custom build is preserved, not regenerated");h.succeed();
        });
    }
    private static ExpansionApply.Access wrap(ExpansionApply.Access a,java.util.function.Predicate<ExpansionOperation.Change> writer){return new ExpansionApply.Access(){
        public boolean valid(){return a.valid();}public CompoundTag read(BlockPos p){return a.read(p);}public boolean write(ExpansionOperation.Change c){return writer.test(c);}
        public String protectedDigest(){return a.protectedDigest();}public void store(ExpansionOperation o){a.store(o);}
    };}
    private static void fault(GameTestHelper h,int stop){
        fault(h,stop,false);
    }
    private static void fault(GameTestHelper h,int stop,boolean apron){
        var f=begin(h);h.runAfterDelay(3,()->{
            if(apron){h.assertTrue(OutpostExpansion.applyNow(f.player(),f.post()).success(),"Existing apron before yard fault");}
            var before=snapshot(h,f.post());var op=intent(h,f);var access=OutpostExpansion.access(f.player(),f.post(),op);int[] index={0};
            h.assertFalse(ExpansionApply.apply(op,wrap(access,c->index[0]++!=stop&&access.write(c))).success(),"Injected bounded write failure must fail");
            var saved=f.post().yardOperation().orElseThrow();h.assertTrue(saved.phase()==ExpansionOperation.Phase.REVIEW&&saved.index()==stop&&f.post().site().appliedRevision()==(apron?1:0),"Revision-2 failure receipt/source retained");
            if(apron){h.assertTrue(f.post().saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").get("operation").equals(before.post().getCompoundOrEmpty("site").get("operation")),"Old sealed receipt never replaced on partial yard failure");}
            for(int i=0;i<op.changes().size();i++){var cell=op.changes().get(i);h.assertTrue(NbtUtils.writeBlockState(h.getLevel().getBlockState(cell.pos())).equals(i<stop?cell.after():cell.before()),"Exact prefix only; no rollback/replay");}
            conserved(h,f.post(),before);refuse(h,()->OutpostGrowth.expand(f.player(),f.post()));refuse(h,()->OutpostExpansion.recover(f.player(),f.post()));h.succeed();
        });
    }
    private static void supportedEntity(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var c=f.post().site().center();var npc=h.spawn(NpcRegistry.SURVIVOR.get(),h.relativePos(c.offset(4,2,0)));npc.setNoAi(true);
            var before=snapshot(h,f.post());
            try{LevelTwoGrowth.applyNow(f.player(),f.post());h.fail("Occupied wall support must refuse");}
            catch(OutpostPreview.EntityObstruction expected){h.assertTrue(expected.getMessage().contains("survivor_defender"),"Actionable obstruction identifies the real entity");}
            h.assertTrue(npc.isAlive()&&snapshot(h,f.post()).equals(before)&&f.post().yardOperation().isEmpty(),"Do not remove support from an occupied wall or relocate/kill its defender");h.succeed();
        });
    }
    private static void callback(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var op=intent(h,f);var access=OutpostExpansion.access(f.player(),f.post(),op);int[] i={0};var lab=tile(h,f.post().getBlockPos().offset(-2,0,-1));
            h.assertFalse(ExpansionApply.apply(op,wrap(access,c->{boolean ok=access.write(c);if(i[0]++==0){lab.change(lab.state().supplies(99));}return ok;})).success(),"Changed protected NBT cannot seal a yard");
            h.assertTrue(lab.state().supplies()==99&&f.post().yardOperation().orElseThrow().phase()==ExpansionOperation.Phase.REVIEW,"Callback state retained, not destructively rolled back");
            refuse(h,()->OutpostExpansion.recover(f.player(),f.post()));h.succeed();
        });
    }
    private static void anchor(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var op=intent(h,f);var access=OutpostExpansion.access(f.player(),f.post(),op);int[] i={0};
            h.assertFalse(ExpansionApply.apply(op,wrap(access,c->{boolean ok=access.write(c);if(i[0]++==0){h.getLevel().setBlockAndUpdate(f.post().getBlockPos(),Blocks.AIR.defaultBlockState());}return ok;})).success(),"Lost anchor stops immediately");
            h.assertTrue(i[0]==1&&h.getLevel().getBlockEntity(f.post().getBlockPos())==null,"No anchor resurrection or later writes");h.succeed();
        });
    }
    private static void recover(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var op=intent(h,f);f.post().storeYardOperation(op);var access=OutpostExpansion.access(f.player(),f.post(),op);
            for(var cell:op.changes()){h.assertTrue(access.write(cell),"Simulated interrupted all-after write");}
            f.post().storeYardOperation(op.withPhase(ExpansionOperation.Phase.REVIEW,op.changes().size(),"Interrupted after last block"));
            var before=snapshot(h,f.post());OutpostExpansion.recover(f.player(),f.post());conserved(h,f.post(),before);
            h.assertTrue(f.post().site().appliedRevision()==2&&f.post().yardOperation().orElseThrow().phase()==ExpansionOperation.Phase.APPLIED,"Explicit all-after recovery seals only metadata");
            refuse(h,()->OutpostExpansion.recover(f.player(),f.post()));h.succeed();
        });
    }
    private static BuildingBlockEntity load(GameTestHelper h,BuildingBlockEntity old,CompoundTag tag){var p=(BuildingBlockEntity)BlockEntity.loadStatic(old.getBlockPos(),old.getBlockState(),tag,h.getLevel().registryAccess());h.assertTrue(p!=null,"Malformed site extension cannot crash tile load");h.getLevel().setBlockEntity(p);return p;}
    private static void unknown(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var op=intent(h,f);var initial=f.post().saveWithFullMetadata(h.getLevel().registryAccess());
            var future=op.save();future.putInt("operation_version",99);future.putString("unknown","keep");
            for(Tag raw:new Tag[]{future,StringTag.valueOf("opaque"),new CompoundTag()}){
                var saved=initial.copy();saved.getCompoundOrEmpty("site").put("yard_operation",raw.copy());var post=load(h,f.post(),saved);
                refuse(h,()->LevelTwoGrowth.applyNow(h.getLevel(),post));post.change(post.state().supplies(25));
                h.assertTrue(post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").get("yard_operation").equals(raw),"Unknown yard retained during ordinary storage use");
            }
            var known=initial.copy();var tag=op.save();tag.putString("extension","keep");tag.getListOrEmpty("cells").getCompoundOrEmpty(0).putString("cell_extension","keep");known.getCompoundOrEmpty("site").put("yard_operation",tag);
            var post=load(h,f.post(),known);post.storeYardOperation(op.withPhase(ExpansionOperation.Phase.REVIEW,0,"No replay"));
            var after=post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").getCompoundOrEmpty("yard_operation");
            h.assertTrue(after.getStringOr("extension","").equals("keep")&&after.getListOrEmpty("cells").getCompoundOrEmpty(0).getStringOr("cell_extension","").equals("keep"),"Known yard transition retains unknown operation/cell fields");h.succeed();
        });
    }
    private static void navigation(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            OutpostGrowth.expand(f.player(),f.post());var c=f.post().site().center();var npc=h.spawn(NpcRegistry.SURVIVOR.get(),h.relativePos(c.offset(0,1,5)));npc.setNoAi(true);
            // Frozen actors do not run travel/ground physics. Establish the verified floor contact
            // explicitly, then let the real vanilla pathfinder judge every route and staircase.
            var floor=npc.blockPosition().below();
            h.assertTrue(h.getLevel().getBlockState(floor).isCollisionShapeFullBlock(h.getLevel(),floor),"Navigation starts on a real solid yard floor");
            npc.setOnGround(true);
            for(var goal:new BlockPos[]{c.offset(0,1,0),c.offset(-2,1,-1),c.offset(3,1,-2),c.offset(-4,3,0)}){
                var path=npc.getNavigation().createPath(goal,0);h.assertTrue(path!=null&&path.canReach(),"Vanilla human navigation reaches command/lab/crate/watch from entrance: "+goal.toShortString());
            }
            for(int z=3;z<=6;z++){for(int x=-1;x<=1;x++){h.assertTrue(h.getLevel().getBlockState(c.offset(x,1,z)).isAir()&&h.getLevel().getBlockState(c.offset(x,2,z)).isAir(),"Three-wide two-high south access");}}
            h.succeed();
        });
    }
    private static void command(GameTestHelper h){
        var f=begin(h);h.runAfterDelay(3,()->{
            var dispatcher=h.getLevel().getServer().getCommands().getDispatcher();var source=f.player().createCommandSourceStack();var op=source.withPermission(h.getLevel().getServer().createCommandSourceStack().permissions());var before=snapshot(h,f.post());
            try{try{dispatcher.execute("standandhold outpost layout",source);h.fail("Operator-only preview");}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){}
                h.assertTrue(dispatcher.execute("standandhold outpost layout",op)==1&&f.player().received("Level-2 yard / READ ONLY"),"Real read-only layout command gives useful footprint");
            }catch(com.mojang.brigadier.exceptions.CommandSyntaxException error){throw new IllegalStateException(error);}
            h.assertTrue(snapshot(h,f.post()).equals(before),"Read-only command changes no world data");h.succeed();
        });
    }
}
