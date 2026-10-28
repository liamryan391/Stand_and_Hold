package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingPositions;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.structure.StructureKind;
import com.liamryan.standandhold.common.structure.StructurePlan;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;

final class StructureChecks {
    private static int assertions;
    static void run() throws Exception {
        candidates(); layoutsAndTerrain(); registry(); cleanup();
        System.out.println("Structure checks passed: " + assertions + " assertions.");
    }
    private static void candidates() {
        for (StructureKind kind : StructureKind.values()) {
            for (int region : new int[]{-2, -1, 0, 1}) {
                int count = 0;
                for (int x = region * 8; x < (region + 1) * 8; x++) {
                    for (int z = -8; z < 0; z++) {
                        boolean a = StructurePlan.candidate(123, x, z, 8, 1, kind.salt());
                        check(a == StructurePlan.candidate(123, x, z, 8, 1, kind.salt()), "deterministic negative/positive candidate");
                        if (a) { count++; }
                    }
                }
                check(count == 1, "exactly one candidate per region when chance is one");
            }
        }
        check(StructurePlan.candidate(1, -1, 2, 1, 1, 0), "dense QA config has one attempt per chunk");
        expect(() -> StructurePlan.candidate(0, 0, 0, 0, 1, 0));
        expect(() -> StructurePlan.candidate(0, 0, 0, 1, 0, 0));
    }
    private static class Terrain implements StructurePlan.Terrain {
        int reads;
        public int surface(int x, int z) { reads++; return 64; }
        public boolean ground(BlockPos p) { reads++; return true; }
        public boolean clear(BlockPos p) { reads++; return true; }
        public boolean writable(BlockPos p) { return true; }
    }
    private static void layoutsAndTerrain() {
        for (StructureKind kind : StructureKind.values()) {
            for (int radius : kind == StructureKind.CHECKPOINT ? new int[]{3, 4, 5} : new int[]{5, 6}) {
                var terrain = new Terrain();
                var plan = StructurePlan.plan(kind, -1, 2, radius, 2, -64, 319, terrain);
                check(terrain.reads < 2000, "bounded full-footprint preflight");
                check(plan.commandPost().equals(new BlockPos(-8, 65, 39)), "central protected command position");
                check(plan.cells().size() <= 169 * 9, "bounded placement count");
                check(plan.cells().stream().allMatch(c -> c.pos().getX() >> 4 == -1 && c.pos().getZ() >> 4 == 2), "all writes stay in generating chunk");
                check(new HashSet<>(plan.cells().stream().map(StructurePlan.Cell::pos).toList()).size() == plan.cells().size(), "no duplicate cell writes");
                check(plan.cells().stream().filter(c -> c.material() == StructurePlan.Material.COMMAND_POST).count() == 1, "exactly one command post");
                check(plan.cells().stream().filter(c -> c.material() == StructurePlan.Material.CRATE).count() == 1, "exactly one crate");
                check(plan.cells().stream().filter(c -> c.material() == StructurePlan.Material.LAB).count() == 1, "exactly one lab in every checkpoint and main foundation");
                var lab = plan.cells().stream().filter(c -> c.material() == StructurePlan.Material.LAB).findFirst().orElseThrow().pos();
                check(!lab.equals(plan.commandPost()), "lab does not overwrite command post");
                check(plan.cells().stream().anyMatch(c -> c.pos().equals(lab.below()) && (c.material() == StructurePlan.Material.FLOOR
                        || c.material() == StructurePlan.Material.RESEARCH_MARKER)), "lab has a solid planned floor");
                check(plan.cells().stream().anyMatch(c -> c.pos().equals(lab.above()) && c.material() == StructurePlan.Material.AIR), "lab has clear headroom");
                check(plan.cells().stream().anyMatch(c -> c.pos().equals(lab.south()) && c.material() == StructurePlan.Material.AIR), "lab has an accessible south interaction space at every radius");
                for (int dx = -1; dx <= 1; dx++) {
                    BlockPos entrance = new BlockPos(-8 + dx, 65, 40 + radius);
                    check(plan.cells().stream().anyMatch(c -> c.pos().equals(entrance) && c.material() == StructurePlan.Material.AIR), "clear three-wide south entrance");
                }
            }
        }
        expect(() -> plan(new Terrain() { public int surface(int x, int z) { return x == 4 ? 64 : 68; } }));
        expect(() -> plan(new Terrain() { public boolean ground(BlockPos p) { return false; } }));
        expect(() -> plan(new Terrain() { public boolean clear(BlockPos p) { return false; } }));
        expect(() -> plan(new Terrain() { public int surface(int x, int z) { return 317; } }));
        expect(() -> plan(new Terrain() { public int surface(int x, int z) { return -64; } }));
        var inaccessible = new Terrain() { public boolean writable(BlockPos p) { return false; } };
        expect(() -> plan(inaccessible)); check(inaccessible.reads == 0, "unavailable chunk/border rejected before terrain read");
        var gentle = new Terrain() { public int surface(int x, int z) { return x == 4 ? 64 : 66; } };
        check(plan(gentle).cells().stream().allMatch(c -> c.pos().getY() >= 64 && c.pos().getY() <= 71), "bounded foundation fills gentle slope without digging");
        expect(() -> StructurePlan.plan(StructureKind.MAIN_BASE, 0, 0, 7, 2, -64, 319, new Terrain()));
    }
    private static StructurePlan plan(StructurePlan.Terrain terrain) { return StructurePlan.plan(StructureKind.CHECKPOINT, 0, 0, 4, 2, -64, 319, terrain); }
    private static void registry() throws Exception {
        var post = BuildingPosition.parse("minecraft:overworld@8,65,7").orElseThrow();
        var data = new HumanWorldData();
        check(data.registerMainBase(post) && data.commandPostKeys().contains(post.key()) && data.isMainBase(post), "base registration also registers command post");
        data.setDirty(false);
        check(!data.registerMainBase(post) && !data.isDirty(), "duplicate registration does not dirty or replenish storage");
        check(!data.hasDiscoveredMainBase(), "generation alone is not discovery");
        check(data.discoverMainBase(post) && data.hasDiscoveredMainBase(), "interaction records discovery");
        data.setDirty(false);
        check(!data.discoverMainBase(post) && !data.isDirty(), "discovery idempotent");
        var restored = decode(encode(data));
        check(restored.isMainBase(post) && restored.hasDiscoveredMainBase(), "base and discovery survive codec reload");
        var catalog = FoundationChecks.defaults();
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        for (String id : new String[]{"recover_parasite_samples", "establish_field_command", "stockpile_supplies", "complete_first_research", "early_army_response", "defend_outpost"}) {
            MissionManager.start(restored, catalog, thresholds, id);
            MissionManager.advance(restored, catalog, thresholds, id, 1000000);
        }
        long before = restored.points(), supplies = restored.supplies();
        var completed = MissionManager.start(restored, catalog, thresholds, "establish_main_base");
        check(completed.state().completed() && restored.points() == before + 50 && restored.supplies() == supplies + 25, "discovery mission uses persistent gameplay receipt");
        restored.removeMainBase(post.key()); restored.removeCommandPost(post.key());
        restored = decode(encode(restored));
        long awarded = restored.points();
        check(restored.hasDiscoveredMainBase() && restored.mainBaseKeys().isEmpty(), "removing structure preserves discovery history");
        check(!MissionManager.start(restored, catalog, thresholds, "establish_main_base").changed() && restored.points() == awarded, "cleanup/reload never resets completed mission reward");
        var old = encode(data); old.putInt("data_version", 4); old.remove("main_bases"); old.remove("discovered_main_base"); old.putString("future", "keep");
        var migrated = decode(old);
        check(migrated.dataVersion() == 5 && migrated.mainBaseKeys().isEmpty() && !migrated.hasDiscoveredMainBase()
                && migrated.commandPostKeys().contains(post.key()), "schema four migration keeps command posts/earlier fields");
        check(encode(migrated).getStringOr("future", "").equals("keep"), "unknown fields retained");
        var missing = encode(data); missing.remove("main_bases");
        var blocked = decode(missing); expect(blocked::mainBaseKeys);
        var malformed = encode(data); malformed.putString("main_bases", "preserve");
        var bad = decode(malformed); expect(bad::mainBaseKeys);
        check(encode(bad).getStringOr("main_bases", "").equals("preserve"), "malformed registry preserved, not reset");
        var flag = encode(data); flag.putString("discovered_main_base", "bad"); var invalid = decode(flag); expect(invalid::hasDiscoveredMainBase);
        check(encode(invalid).getStringOr("discovered_main_base", "").equals("bad"), "malformed receipt preserved");
        check(!new HumanWorldData().discoverMainBase(post), "unregistered position cannot mark discovery");
    }
    private static void cleanup() {
        var dimension = Identifier.parse("minecraft:overworld");
        var reads = new AtomicInteger();
        check(BuildingPositions.stale("bad", dimension, p -> { throw new AssertionError("loaded lookup for bad key"); }, p -> false), "malformed key safely removed");
        check(!BuildingPositions.stale("minecraft:the_nether@1,65,1", dimension, p -> { throw new AssertionError("other dimension read"); }, p -> false), "other dimension preserved");
        check(!BuildingPositions.stale("minecraft:overworld@1,65,1", dimension, p -> false, p -> { throw new AssertionError("unloaded chunk read"); }), "unloaded positions retained without inspecting blocks");
        check(BuildingPositions.stale("minecraft:overworld@1,65,1", dimension, p -> true, p -> { reads.incrementAndGet(); return false; }), "loaded missing tile removed");
        check(!BuildingPositions.stale("minecraft:overworld@1,65,1", dimension, p -> true, p -> true) && reads.get() == 1, "loaded valid tile retained");
        check(BuildingPosition.parse(null).isEmpty(), "null position string safe");
    }
    private static CompoundTag encode(HumanWorldData data) { return (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(); }
    private static HumanWorldData decode(CompoundTag tag) { return HumanWorldData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(); }
    private static void check(boolean value, String message) { assertions++; if (!value) { throw new AssertionError(message); } }
    private static void expect(Runnable action) { assertions++; try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; } throw new AssertionError("Expected rejected structure operation"); }
}
