package com.liamryan.standandhold;

import com.liamryan.standandhold.common.structure.*;
import java.util.*;
import net.minecraft.core.BlockPos;

final class ExpansionChecks {
    private static int assertions;
    static void run() {
        var site = site(0, 0);
        check(SiteProvenance.read(site.save()).equals(site), "site fields round-trip");
        for (String key : List.of("site_version", "layout_revision", "anchor_x", "anchor_y", "anchor_z", "floor_y", "radius", "applied_revision")) {
            var tag = site.save(); tag.remove(key); expect(() -> SiteProvenance.read(tag));
            var wrong = site.save(); wrong.putLong(key, 1L); expect(() -> SiteProvenance.read(wrong));
        }
        for (String key : List.of("uuid", "dimension", "kind", "layout", "origin", "orientation")) {
            var tag = site.save(); tag.putString(key, "invalid value"); expect(() -> SiteProvenance.read(tag));
        }
        for (String key : List.of("site_version", "layout_revision", "floor_y", "radius", "anchor_x", "anchor_z")) {
            var tag = site.save(); tag.putInt(key, key.equals("anchor_z") ? 998 : 999); expect(() -> SiteProvenance.read(tag));
        }
        var aligned = site.save(); aligned.putInt("anchor_z", 999);
        check(SiteProvenance.read(aligned).anchor().getZ() == 999, "aligned anchor z=999 is valid; actual tile mismatch is checked by the service");
        for (int cx = -2; cx <= 2; cx++) { for (int cz = -2; cz <= 2; cz++) {
            var receipt = site(cx, cz); var terrain = new Ground(receipt);
            var plan = ExpansionPlan.inspect(receipt, 2, -64, 320, terrain);
            check(plan.cells().size() == 137 && new HashSet<>(plan.cells().stream().map(ExpansionPlan.Cell::pos).toList()).size() == 137, "exact unique delta");
            for (var material : ExpansionPlan.Material.values()) {
                long expected = switch (material) { case FLOOR -> 88; case WALL -> 45; case LIGHT -> 4; };
                check(plan.cells().stream().filter(c -> c.material() == material).count() == expected, "exact material count");
            }
            check(plan.reads() == 616 && terrain.reads == plan.reads() && plan.columns() == 169, "actual read/column budgets");
            var center = receipt.center();
            for (var cell : plan.cells()) {
                check(Math.abs(cell.pos().getX() - center.getX()) > 4 || Math.abs(cell.pos().getZ() - center.getZ()) > 4, "original footprint excluded");
                check(cell.pos().getX() >> 4 == cx && cell.pos().getZ() >> 4 == cz, "negative/positive chunks remain local");
                if (cell.material() == ExpansionPlan.Material.LIGHT) {
                    check(plan.cells().contains(new ExpansionPlan.Cell(cell.pos().below(), ExpansionPlan.Material.WALL)), "torches have planned wall support");
                }
            }
            for (int x = -1; x <= 1; x++) {
                check(!plan.cells().contains(new ExpansionPlan.Cell(center.offset(x, 1, 6), ExpansionPlan.Material.WALL)), "three-wide aligned south entrance");
            }
            check(ExpansionPlan.inspect(receipt, 5, -64, 320, new Ground(receipt)).cells().equals(plan.cells()), "world/unit tier not a new physical shape");
        } }
        var untouched = new Ground(site);
        expect(() -> ExpansionPlan.inspect(site, 1, -64, 320, untouched)); check(untouched.reads == 0, "level refusal reads no terrain");
        for (int radius : new int[]{3, 5}) {
            var other = new SiteProvenance(site.id(), site.dimension(), site.anchor(), radius, "worldgen", 0);
            expect(() -> ExpansionPlan.inspect(other, 2, -64, 320, untouched));
        }
        expect(() -> ExpansionPlan.inspect(new SiteProvenance(site.id(), site.dimension(), site.anchor(), 4, "debug", 1), 2, -64, 320, untouched));
        expect(() -> ExpansionPlan.inspect(site, 2, 64, 320, untouched));
        expect(() -> ExpansionPlan.inspect(site, 2, -64, 69, untouched));
        var unloaded = new Ground(site) { public boolean loaded(BlockPos pos) { return false; } };
        expect(() -> ExpansionPlan.inspect(site, 2, -64, 320, unloaded)); check(unloaded.reads == 0, "unloaded refused before read");
        var border = new Ground(site) { public boolean insideBorder(BlockPos pos) { return false; } };
        expect(() -> ExpansionPlan.inspect(site, 2, -64, 320, border)); check(border.reads == 0, "border refused before read");
        for (var sample : ExpansionPlan.Sample.values()) {
            for (int y : new int[]{-1, 0, 1, 5}) {
                if (sample == (y <= 0 ? ExpansionPlan.Sample.GROUND : ExpansionPlan.Sample.CLEAR)) { continue; }
                var blocked = new Ground(site) {
                    public ExpansionPlan.Sample read(BlockPos pos) {
                        var value = super.read(pos);
                        return pos.equals(site.center().offset(-6, y, -6)) ? sample : value;
                    }
                };
                expect(() -> ExpansionPlan.inspect(site, 2, -64, 320, blocked));
                check(blocked.reads <= ExpansionPlan.MAX_READS, "failed plans stay bounded");
            }
        }
        System.out.println("Expansion checks passed: " + assertions + " assertions.");
    }
    private static SiteProvenance site(int cx, int cz) { return new SiteProvenance(UUID.randomUUID(), "minecraft:overworld", new BlockPos(cx * 16 + 8, 65, cz * 16 + 7), 4, "debug", 0); }
    private static class Ground implements ExpansionPlan.Terrain {
        final SiteProvenance site; int reads;
        Ground(SiteProvenance site) { this.site = site; }
        public boolean loaded(BlockPos pos) { return true; }
        public boolean insideBorder(BlockPos pos) { return true; }
        public ExpansionPlan.Sample read(BlockPos pos) {
            if (!loaded(pos) || !insideBorder(pos)) { throw new AssertionError("Read without availability/border guard"); }
            reads++; return pos.getY() <= site.center().getY() ? ExpansionPlan.Sample.GROUND : ExpansionPlan.Sample.CLEAR;
        }
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
    private static void expect(Runnable action) {
        assertions++; try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("Expected safely refused expansion/provenance");
    }
}
