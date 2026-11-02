package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.structure.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;

/** Direct/apron completion and interrupted/mixed/all-after state cross real dedicated JVMs. */
final class RestartYardFixture {
    private static final int[][] CHUNKS={{2,0},{2,1},{3,1}};
    static void seed(ServerLevel level,Properties receipt){
        for(int i=0;i<CHUNKS.length;i++){
            var result=StructurePlacement.place(level,StructureKind.CHECKPOINT,CHUNKS[i][0],CHUNKS[i][1]);check(result.success(),result.message());
            var post=(BuildingBlockEntity)level.getBlockEntity(result.commandPost());post.change(new BuildingState(2,11,2,"",0));
            var lab=(BuildingBlockEntity)level.getBlockEntity(post.getBlockPos().offset(-2,0,-1));lab.change(new BuildingState(1,23,3,"standandhold:field_communications",4,false));
            var plan=LevelTwoGrowth.inspect(level,post);var op=new ExpansionOperation(UUID.randomUUID(),plan.site().id(),plan.changes(),OutpostExpansion.yardDigest(level,post,plan.site(),plan.changes()),ExpansionOperation.Phase.PREPARED,0,"",0,2);
            var access=OutpostExpansion.access(level,post,op);
            if(i==0){check(ExpansionApply.apply(op,access).success(),"Real direct yard before restart");}
            else{
                post.storeYardOperation(op);int count=i==1?7:op.changes().size();
                for(int cell=0;cell<count;cell++){check(access.write(op.changes().get(cell)),"Interrupted real yard write");}
                post.storeYardOperation(op.withPhase(ExpansionOperation.Phase.REVIEW,count,"Restart interruption fixture"));
                OutpostGrowth.setMode(level,post,OutpostGrowth.Mode.AUTOMATIC);OutpostGrowth.tick(level,post);
                check(post.growthSettings().pause()==OutpostGrowth.REVIEW,"Interrupted intent pauses without replay");
            }
            receipt.setProperty("yard."+i+".post",Long.toString(post.getBlockPos().asLong()));receipt.setProperty("yard."+i+".site",post.site().id().toString());
            receipt.setProperty("yard."+i+".operation",post.yardOperation().orElseThrow().id().toString());
        }
    }
    static void verify(ServerLevel level,Properties receipt){
        for(int i=0;i<CHUNKS.length;i++){
            var post=(BuildingBlockEntity)level.getBlockEntity(BlockPos.of(Long.parseLong(receipt.getProperty("yard."+i+".post"))));
            check(post!=null&&post.site().id().toString().equals(receipt.getProperty("yard."+i+".site")),"Same yard site identity after restart");
            check(post.state().equals(new BuildingState(2,11,2,"",0)),"No repeat costs/refill after restart");
            var lab=(BuildingBlockEntity)level.getBlockEntity(post.getBlockPos().offset(-2,0,-1));
            check(lab.state().equals(new BuildingState(1,23,3,"standandhold:field_communications",4,false)),"Paused lab storage/target/work retained");
            var op=post.yardOperation().orElseThrow();check(op.id().toString().equals(receipt.getProperty("yard."+i+".operation")),"Same operation identity");
            var observed=op.observe(pos->NbtUtils.writeBlockState(level.getBlockState(pos))).state();
            check(observed==(i==1?ExpansionOperation.Observed.MIXED:ExpansionOperation.Observed.ALL_AFTER),"Exact saved write prefix/all-after state");
            for(int tick=0;tick<com.liamryan.standandhold.config.ResponseConfig.CHECK_INTERVAL.get();tick++){BuildingBlockEntity.tick(level,post.getBlockPos(),post.getBlockState(),post);}
            check(post.yardOperation().orElseThrow().equals(op),"No automatic receipt replay or repair after restart");
            if(i==2){
                var actor=QaPlayer.join(level);var p=post.getBlockPos();actor.snapTo(p.getX()+.5,p.getY(),p.getZ()+4.5,180,16);
                OutpostExpansion.recover(actor,post);check(post.site().appliedRevision()==2,"Explicit all-after recovery after real restart");QaPlayer.closeAll();
            }else{check(post.site().appliedRevision()==(i==0?2:0),"Applied versus mixed source revision retained");}
        }
    }
    private static void check(boolean ok,String message){if(!ok){throw new IllegalStateException(message);}}
}
