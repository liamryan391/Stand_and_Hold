package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Four bounded raised pads in an already forced chunk; all sixteen older fixtures stay intact. */
final class RestartDeckFixture {
    static void seed(ServerLevel level, Properties receipt) {
        for (int i=0;i<4;i++) {
            int floor=90+i*20;
            for (var pos : BlockPos.betweenClosed(2,floor-1,2,14,floor,14)) {
                level.setBlockAndUpdate(pos,(pos.getY()==floor?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState());
            }
            var placed=StructurePlacement.place(level,StructureKind.CHECKPOINT,0,0);check(placed.success(),placed.message());
            var post=(BuildingBlockEntity)level.getBlockEntity(placed.commandPost());check(post.site().center().getY()==floor,"Separate bounded deck pad height");post.change(new BuildingState(3,71,3,"",0));
            var lab=(BuildingBlockEntity)level.getBlockEntity(post.getBlockPos().offset(-2,0,-1));lab.change(new BuildingState(1,23,4,"standandhold:field_communications",5,false));
            check(LevelTwoGrowth.applyNow(level,post).success(),"Same authored revision-2 source before deck receipt");
            var attempt=LevelThreeGrowth.prepare(level,post);var op=attempt.operation();
            if(i==0){
                check(ExpansionApply.apply(op,attempt.access()).success(),"Actual sealed deck before JVM shutdown");
                var home=new BuildingPosition(level.dimension().identifier(),post.getBlockPos());var npc=NpcRegistry.SURVIVOR.get().create(level,EntitySpawnReason.COMMAND);check(npc!=null,"Deck watch factory");
                npc.assignPost(home);npc.setNoAi(true);npc.setHealth(12);npc.setItemSlot(EquipmentSlot.MAINHAND,Items.STICK.getDefaultInstance());var watch=DefenderDuties.watchPosition(post);
                check(watch!=null,"Original west watch stays available above a sealed deck receipt");npc.snapTo(watch.getX()+.5,watch.getY(),watch.getZ()+.5,0,0);
                check(HumanWorldData.get(level.getServer()).reserveDefender(home.key(),npc.getUUID(),6)&&level.addFreshEntity(npc),"Existing bounded watch reservation");DefenderDuties.setMode(level,post,DefenderDuty.Mode.PATROL);
                check(DefenderDuties.claimWatch(level,post,npc),"Same single watch slot");receipt.setProperty("deck.npc",npc.getUUID().toString());
            }else{
                post.storeDeckOperation(op);int writes=i==1?0:i==2?31:63;
                for(int at=0;at<writes;at++){check(attempt.access().write(op.changes().get(at)),"Exact interrupted deck prefix");}
                if(i!=1){post.storeDeckOperation(op.withPhase(ExpansionOperation.Phase.REVIEW,writes,"Restart interruption fixture"));}
                OutpostGrowth.setMode(level,post,OutpostGrowth.Mode.AUTOMATIC);OutpostGrowth.tick(level,post);check(post.growthSettings().pause()==OutpostGrowth.REVIEW,"Unsealed deck pauses, never replays");
            }
            receipt.setProperty("deck."+i+".post",Long.toString(post.getBlockPos().asLong()));receipt.setProperty("deck."+i+".site",post.site().id().toString());receipt.setProperty("deck."+i+".op",op.id().toString());
        }
    }
    static void verify(ServerLevel level, Properties receipt) {
        for(int i=0;i<4;i++){
            var post=(BuildingBlockEntity)level.getBlockEntity(BlockPos.of(Long.parseLong(receipt.getProperty("deck."+i+".post"))));check(post!=null,"Saved deck post loads");
            check(post.site().id().toString().equals(receipt.getProperty("deck."+i+".site"))&&post.state().equals(new BuildingState(3,71,3,"",0)),"Same site/level/supplies/samples, no replay cost/refill");
            var lab=(BuildingBlockEntity)level.getBlockEntity(post.getBlockPos().offset(-2,0,-1));check(lab.state().equals(new BuildingState(1,23,4,"standandhold:field_communications",5,false)),"Original lab samples/storage/paused work retained");
            var op=post.deckOperation().orElseThrow();check(op.id().toString().equals(receipt.getProperty("deck."+i+".op")),"Same deck operation UUID");
            var observed=op.observe(pos->NbtUtils.writeBlockState(level.getBlockState(pos))).state();check(observed==(i==1?ExpansionOperation.Observed.ALL_BEFORE:i==2?ExpansionOperation.Observed.MIXED:ExpansionOperation.Observed.ALL_AFTER),"Exact persisted all-before/mixed/all-after state");
            var before=post.saveWithFullMetadata(level.registryAccess());for(int tick=0;tick<10;tick++){OutpostGrowth.tick(level,post);}check(before.equals(post.saveWithFullMetadata(level.registryAccess())),"Loaded automatic checks never replay any existing deck operation");
            if(i==0){
                var id=UUID.fromString(receipt.getProperty("deck.npc"));check(level.getEntity(id) instanceof HumanNpc npc&&npc.isNoAi()&&npc.getHealth()==12&&npc.getMainHandItem().is(Items.STICK)
                        &&npc.commandPost().map(p->p.pos().equals(post.getBlockPos())).orElse(false)&&id.equals(post.duty().watch()),"Deck/watch human UUID, health, gear and home persisted");
                check(OutpostGrowth.status(level,post)==OutpostGrowth.EXPANDED,"Sealed deck is not a repair request after legitimate duty changes");
            }else if(i==3){
                var actor=QaPlayer.join(level);var p=post.getBlockPos();actor.snapTo(p.getX()+.5,p.getY(),p.getZ()+4.5,180,16);OutpostExpansion.recover(actor,post);
                check(post.site().appliedRevision()==3&&post.deckOperation().orElseThrow().phase()==ExpansionOperation.Phase.APPLIED,"Explicit all-after recovery after real restart changes only metadata");QaPlayer.closeAll();
            }else{check(post.site().appliedRevision()==2&&post.growthSettings().pause()==OutpostGrowth.REVIEW,"Unsealed source/pause retained after restart");}
        }
    }
    private static void check(boolean value,String message){if(!value){throw new IllegalStateException(message);}}
}
