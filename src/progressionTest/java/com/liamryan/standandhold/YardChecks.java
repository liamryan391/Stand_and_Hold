package com.liamryan.standandhold;

import com.liamryan.standandhold.common.structure.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

final class YardChecks {
    private static int assertions;
    static void run() {
        for (int chunk : new int[]{-12,-1,0,1,12}) {
            for (int source : new int[]{0,1}) { for (int level=2; level<=5; level++) {
                var site = site(chunk,source); var terrain = new Terrain(site); var plan = LevelTwoPlan.inspect(site,level,-64,320,terrain);
                check(plan.reads() == terrain.reads && plan.reads() <= ExpansionPlan.MAX_READS, "Cached reads bounded with no hidden second terrain read");
                check(plan.changes().size() == (source == 0 ? 183 : 46), "Exact direct/apron delta count");
                var seen = new HashSet<BlockPos>();
                for (var change : plan.changes()) {
                    check(seen.add(change.pos()) && change.pos().getX() >> 4 == site.anchor().getX() >> 4
                            && change.pos().getZ() >> 4 == site.anchor().getZ() >> 4, "Unique same-chunk changes");
                    check(!change.pos().equals(site.anchor()) && !change.pos().equals(site.anchor().offset(-2,0,-1))
                            && !change.pos().equals(site.anchor().offset(3,0,-2)), "No original building rewritten");
                    check(!change.before().equals(change.after()), "No redundant/no-op writes");
                }
                var op = new ExpansionOperation(UUID.randomUUID(),site.id(),plan.changes(),"a".repeat(64),ExpansionOperation.Phase.PREPARED,0,"",source,2);
                check(ExpansionOperation.read(op.save(),site).equals(op), "Revision-2 source/target/cells round trip");
                var appliedSite = new SiteProvenance(site.id(),site.dimension(),site.anchor(),4,"debug",2);
                check(ExpansionOperation.read(op.withPhase(ExpansionOperation.Phase.APPLIED,plan.changes().size(),"").save(),appliedSite).targetRevision()==2, "Applied receipt reloads against target site revision");
                var before = new HashMap<BlockPos,CompoundTag>(); plan.changes().forEach(c -> before.put(c.pos(),c.before()));
                check(op.observe(before::get).state()==ExpansionOperation.Observed.ALL_BEFORE, "All-before is observable, not replay permission");
                for (var cell : plan.changes()) { before.put(cell.pos(),cell.after()); }
                check(op.observe(before::get).state()==ExpansionOperation.Observed.ALL_AFTER, "All-after matching exact authored changes");
                var future=op.save(); future.putInt("operation_version",99); expect(() -> ExpansionOperation.read(future,site));
                var reorder=op.save(); var list=reorder.getListOrEmpty("cells"); Tag first=list.removeFirst(); list.add(first); expect(() -> ExpansionOperation.read(reorder,site));
            } }
        }
        var site=site(0,0);
        for (int level : new int[]{0,1,6}) { expect(() -> LevelTwoPlan.inspect(site,level,-64,320,new Terrain(site))); }
        for (int source : new int[]{0,1}) {
            var from=site(0,source); var initial=new Terrain(from); var plan=LevelTwoPlan.inspect(from,2,-64,320,initial);
            for (var cell : plan.changes()) {
                var terrain=new Terrain(from); terrain.states.put(cell.pos(),state("minecraft:chest"));
                expect(() -> LevelTwoPlan.inspect(from,2,-64,320,terrain));
            }
            var headroom=new Terrain(from); headroom.states.put(from.center().offset(-4,4,0),state("minecraft:gold_block"));
            expect(() -> LevelTwoPlan.inspect(from,2,-64,320,headroom));
            var supported=new Terrain(from); supported.states.put(from.center().offset(4,2,1),state("minecraft:gold_block"));
            expect(() -> LevelTwoPlan.inspect(from,2,-64,320,supported));
        }
        var unavailable=new Terrain(site); unavailable.loaded=false; expect(() -> LevelTwoPlan.inspect(site,2,-64,320,unavailable)); check(unavailable.reads==0,"Unloaded fails before reads");
        var border=new Terrain(site); border.border=false; expect(() -> LevelTwoPlan.inspect(site,2,-64,320,border)); check(border.reads==0,"Border fails before reads");
        expect(() -> LevelTwoPlan.inspect(site,2,site.center().getY(),320,new Terrain(site)));
        System.out.println("Yard checks passed: " + assertions + " assertions.");
    }
    private static SiteProvenance site(int chunk,int revision) { return new SiteProvenance(UUID.randomUUID(),"minecraft:overworld",new BlockPos(chunk*16+8,64,chunk*16+7),4,"debug",revision); }
    private static CompoundTag state(String id) { var tag=new CompoundTag(); tag.putString("id",id); return tag; }
    private static final class Terrain implements LevelTwoPlan.Terrain {
        private final SiteProvenance site; private final Map<BlockPos,CompoundTag> states=new HashMap<>(); private int reads; private boolean loaded=true,border=true;
        private Terrain(SiteProvenance site) {
            this.site=site; int y=site.center().getY();
            var original=StructurePlan.plan(StructureKind.CHECKPOINT,site.anchor().getX()>>4,site.anchor().getZ()>>4,4,0,-64,320,new StructurePlan.Terrain(){
                public int surface(int x,int z){return y;} public boolean ground(BlockPos p){return true;} public boolean clear(BlockPos p){return true;} public boolean writable(BlockPos p){return true;}
            });
            original.cells().forEach(cell -> states.put(cell.pos(),state(switch(cell.material()){
                case AIR -> "minecraft:air"; case FLOOR -> "minecraft:stone_bricks"; case WALL -> "minecraft:cobblestone"; case LIGHT -> "minecraft:torch";
                case ROOF -> "minecraft:smooth_stone"; case COMMAND_POST -> "standandhold:field_command_post"; case LAB -> "standandhold:research_lab"; case CRATE -> "standandhold:supply_crate";
                default -> throw new AssertionError("No base markers in checkpoint");
            })));
            if(site.appliedRevision()==1){ExpansionPlan.layout(site).forEach(c -> states.put(c.pos(),ExpansionOperation.afterState(c.material())));}
        }
        public boolean loaded(BlockPos p){return loaded;} public boolean insideBorder(BlockPos p){return border;}
        public LevelTwoPlan.Sample read(BlockPos p){
            reads++; var tag=states.getOrDefault(p,state(p.getY()<=site.center().getY()?"minecraft:dirt":"minecraft:air")); var id=tag.getStringOr("id","");
            return new LevelTwoPlan.Sample(tag,id.equals("minecraft:dirt"),id.equals("minecraft:air"),id.contains("standandhold:")||id.equals("minecraft:chest"),id.equals("minecraft:lava"));
        }
    }
    private static void check(boolean ok,String message){assertions++;if(!ok){throw new AssertionError(message);}}
    private static void expect(Runnable action){assertions++;try{action.run();}catch(IllegalArgumentException|IllegalStateException expected){return;}throw new AssertionError("Expected safe yard refusal");}
}
