package com.liamryan.standandhold;

import com.liamryan.standandhold.common.structure.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

final class LevelThreeChecks {
    private static int assertions;
    static void run() {
        for (int chunk : new int[]{-12, -1, 0, 1, 12}) { for (int level = 3; level <= 5; level++) {
            var site = site(chunk, 2); var terrain = new Terrain(site);
            var plan = LevelThreePlan.inspect(site, level, -64, 320, terrain);
            check(plan.changes().size() == 63, "Fixed 63-cell delta, not an enlarged work ceiling");
            check(plan.reads() == 1521 && plan.reads() == terrain.reads && terrain.seen.size() == 1521, "Fixed cached envelope, each block read exactly once");
            check(plan.changes().stream().filter(c -> c.before().getStringOr("id", "").equals("minecraft:smooth_stone_slab")).count() == 9, "Exactly nine roof slab replacements");
            check(plan.changes().stream().filter(c -> c.material() == ExpansionPlan.Material.STEP).count() == 8, "Two-wide four-rise stairs");
            check(plan.changes().stream().noneMatch(c -> c.material() == ExpansionPlan.Material.AIR), "No old wall or building demolition");
            var positions = new HashSet<BlockPos>();
            for (var change : plan.changes()) {
                check(positions.add(change.pos()) && change.pos().getX() >> 4 == site.anchor().getX() >> 4
                        && change.pos().getZ() >> 4 == site.anchor().getZ() >> 4, "Unique same-chunk changes including negative chunks");
                check(!change.before().equals(change.after()) && !change.before().getStringOr("id", "").startsWith("standandhold:"), "No no-ops or building writes");
                check(change.pos().getY() > site.center().getY(), "No terrain/floor replacement");
            }
            for (int z = -2; z <= 0; z++) { for (int x = -1; x <= 1; x++) {
                check(positions.contains(site.center().offset(x, 4, z)), "Every original roof slab enumerated");
            } }
            // A candidate is not a serializable/applicable receipt; existing receipt versions stay closed.
            expect(() -> new ExpansionOperation(UUID.randomUUID(), site.id(), plan.changes(), "a".repeat(64), ExpansionOperation.Phase.PREPARED, 0, "", 2, 3));
        } }
        var site = site(0, 2);
        for (var cell : LevelThreePlan.layout(site)) {
            var terrain = new Terrain(site); terrain.states.put(cell.pos(), state("minecraft:chest"));
            expect(() -> LevelThreePlan.inspect(site, 3, -64, 320, terrain));
        }
        for (int y : new int[]{-1, 0, 3, 5, 6, 7}) {
            var terrain = new Terrain(site); terrain.states.put(site.center().offset(0, y, 0), state("minecraft:gold_block"));
            expect(() -> LevelThreePlan.inspect(site, 3, -64, 320, terrain));
        }
        for (String id : new String[]{"minecraft:water", "minecraft:lava", "minecraft:chest"}) {
            var terrain = new Terrain(site); terrain.states.put(site.center().offset(2, 6, 2), state(id));
            expect(() -> LevelThreePlan.inspect(site, 3, -64, 320, terrain));
        }
        for (int revision : new int[]{0, 1, 3, 99}) { var s = site(0, revision); expect(() -> LevelThreePlan.inspect(s, 3, -64, 320, new Terrain(s))); }
        for (int level : new int[]{0, 1, 2, 6}) { expect(() -> LevelThreePlan.inspect(site, level, -64, 320, new Terrain(site))); }
        var unloaded = new Terrain(site); unloaded.loaded = false;
        expect(() -> LevelThreePlan.inspect(site, 3, -64, 320, unloaded)); check(unloaded.reads == 0, "Unloaded fails before first read");
        var border = new Terrain(site); border.border = false;
        expect(() -> LevelThreePlan.inspect(site, 3, -64, 320, border)); check(border.reads == 0, "Border fails before first read");
        expect(() -> LevelThreePlan.inspect(site, 3, site.center().getY(), 320, new Terrain(site)));
        expect(() -> LevelThreePlan.inspect(site, 3, -64, site.center().getY()+7, new Terrain(site)));
        System.out.println("Level-3 preview checks passed: " + assertions + " assertions.");
    }
    private static SiteProvenance site(int chunk, int revision) { return new SiteProvenance(UUID.randomUUID(), "minecraft:overworld", new BlockPos(chunk*16+8,64,chunk*16+7),4,"debug",revision); }
    private static CompoundTag state(String id) { var tag = new CompoundTag(); tag.putString("id", id); return tag; }
    private static final class Terrain implements LevelTwoPlan.Terrain {
        private final SiteProvenance site; private final Map<BlockPos, CompoundTag> states;
        private final Set<BlockPos> seen = new HashSet<>(); private int reads; private boolean loaded = true, border = true;
        private Terrain(SiteProvenance site) { this.site = site; states = LevelThreePlan.sourceStates(site); }
        public boolean loaded(BlockPos pos) { return loaded; }
        public boolean insideBorder(BlockPos pos) { return border; }
        public LevelTwoPlan.Sample read(BlockPos pos) {
            if (!seen.add(pos)) { throw new AssertionError("Duplicate world read"); } reads++;
            var tag = states.getOrDefault(pos, state(pos.getY() < site.center().getY() ? "minecraft:dirt" : "minecraft:air"));
            var id = tag.getStringOr("id", "");
            return new LevelTwoPlan.Sample(tag, id.equals("minecraft:dirt"), id.equals("minecraft:air"), id.startsWith("standandhold:") || id.equals("minecraft:chest"), id.equals("minecraft:lava") || id.equals("minecraft:water"));
        }
    }
    private static void check(boolean ok, String message) { assertions++; if (!ok) { throw new AssertionError(message); } }
    private static void expect(Runnable run) { assertions++; try { run.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; } throw new AssertionError("Expected safe Level-3 refusal"); }
}
