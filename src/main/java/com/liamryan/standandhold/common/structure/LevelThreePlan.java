package com.liamryan.standandhold.common.structure;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import static com.liamryan.standandhold.common.structure.ExpansionPlan.Material.*;

/** Read-only revision-2 -> 3 candidate. No operation receipt or placement API exists here. */
public record LevelThreePlan(SiteProvenance site, List<ExpansionOperation.Change> changes, int reads) {
    public static final int MIN_Y = -1, MAX_Y = 7, ENVELOPE_CELLS = 1521;
    public LevelThreePlan { changes = List.copyOf(changes); }

    public static List<ExpansionPlan.Cell> layout(SiteProvenance site) {
        if (site.radius() != 4) { throw new IllegalArgumentException("Unsupported Level-3 footprint."); }
        var cells = new LinkedHashMap<BlockPos, ExpansionPlan.Material>(); var c = site.center();
        for (int z = -2; z <= 1; z++) { for (int x = -1; x <= 3; x++) { cells.put(c.offset(x, 4, z), FLOOR); } }
        for (int[] column : new int[][]{{-1, 1}, {3, 1}, {3, -2}}) {
            for (int y = 1; y <= 3; y++) { cells.put(c.offset(column[0], y, column[1]), WALL); }
        }
        for (int z = 5; z >= 2; z--) { for (int x = 2; x <= 3; x++) {
            int height = 6 - z;
            for (int y = 1; y < height; y++) { cells.put(c.offset(x, y, z), WALL); }
            cells.put(c.offset(x, height, z), STEP);
        } }
        for (int z = -2; z <= 1; z++) { for (int x = -1; x <= 3; x++) {
            if ((x == -1 || x == 3 || z == -2 || z == 1) && !(z == 1 && x >= 2)) { cells.put(c.offset(x, 5, z), WALL); }
        } }
        cells.put(c.offset(-1, 6, -2), LIGHT); cells.put(c.offset(3, 6, -2), LIGHT);
        if (cells.size() > ExpansionPlan.MAX_CELLS) { throw new IllegalArgumentException("Level-3 write budget exceeded."); }
        return cells.entrySet().stream().map(e -> new ExpansionPlan.Cell(e.getKey(), e.getValue())).toList();
    }

    /** Exact known source geometry, independent of current world blocks and historical balances. */
    public static Map<BlockPos, CompoundTag> sourceStates(SiteProvenance site) {
        var c = site.center(); var states = new HashMap<BlockPos, CompoundTag>();
        var original = StructurePlan.plan(StructureKind.CHECKPOINT, c.getX() >> 4, c.getZ() >> 4, 4, 0, c.getY() - 2, c.getY() + 10,
                new StructurePlan.Terrain() {
                    public int surface(int x, int z) { return c.getY(); }
                    public boolean ground(BlockPos pos) { return true; }
                    public boolean clear(BlockPos pos) { return true; }
                    public boolean writable(BlockPos pos) { return true; }
                });
        for (var cell : original.cells()) {
            CompoundTag state = switch (cell.material()) {
                case AIR -> ExpansionOperation.afterState(AIR); case FLOOR -> ExpansionOperation.afterState(FLOOR);
                case WALL -> ExpansionOperation.afterState(WALL); case LIGHT -> ExpansionOperation.afterState(LIGHT);
                case ROOF -> roof(); case COMMAND_POST -> named("standandhold:field_command_post");
                case LAB -> named("standandhold:research_lab"); case CRATE -> named("standandhold:supply_crate");
                default -> throw new IllegalArgumentException("Unsupported checkpoint source material.");
            };
            states.put(cell.pos(), state);
        }
        LevelTwoPlan.layout(site, 0).forEach(cell -> states.put(cell.pos(), ExpansionOperation.afterState(cell.material())));
        return states;
    }
    private static CompoundTag named(String id) { var tag = new CompoundTag(); tag.putString("id", id); return tag; }
    private static CompoundTag roof() {
        var tag = named("minecraft:smooth_stone_slab"); var properties = new CompoundTag();
        properties.putString("type", "bottom"); properties.putString("waterlogged", "false"); tag.put("properties", properties); return tag;
    }
    public static LevelThreePlan inspect(SiteProvenance site, int level, int minY, int maxY, LevelTwoPlan.Terrain terrain) {
        if (site.radius() != 4 || site.appliedRevision() != 2) { throw new IllegalArgumentException("Level-3 preview requires a proven revision-2 radius-4 yard."); }
        if (level < 3 || level > 5) { throw new IllegalArgumentException("Level-3 preview requires Command Post local level 3+; world stage is separate."); }
        var c = site.center();
        if (c.getY() + MIN_Y < minY || c.getY() + MAX_Y >= maxY) { throw new IllegalArgumentException("Level-3 preview outside safe build height."); }
        var expected = sourceStates(site); var cache = new HashMap<BlockPos, LevelTwoPlan.Sample>();
        var buildings = Set.of(site.anchor(), c.offset(-2, 1, -2), c.offset(3, 1, -3));
        for (int y = MIN_Y; y <= MAX_Y; y++) { for (int z = -6; z <= 6; z++) { for (int x = -6; x <= 6; x++) {
            var pos = c.offset(x, y, z);
            if (pos.getX() >> 4 != c.getX() >> 4 || pos.getZ() >> 4 != c.getZ() >> 4 || !terrain.loaded(pos)) {
                throw new IllegalArgumentException("Level-3 chunk unavailable; no chunk requested.");
            }
            if (!terrain.insideBorder(pos)) { throw new IllegalArgumentException("Level-3 outside world border."); }
            if (cache.size() >= ExpansionPlan.MAX_READS) { throw new IllegalArgumentException("Level-3 read budget exceeded."); }
            var sample = terrain.read(pos); cache.put(pos, sample);
            if (sample.wet() || sample.tile() && !buildings.contains(pos)) {
                throw new IllegalArgumentException("Level-3 protected container/fluid at " + pos.toShortString() + "; preview changed nothing.");
            }
            if (y == MIN_Y) {
                if (!sample.ground() || sample.tile()) { throw new IllegalArgumentException("Level-3 unsafe natural support at " + pos.toShortString()); }
            } else if (!sample.state().equals(expected.getOrDefault(pos, ExpansionOperation.afterState(AIR)))) {
                throw new IllegalArgumentException("Level-3 source/headroom changed at " + pos.toShortString() + "; no overwrite or repair.");
            }
        } } }
        var changes = layout(site).stream().map(cell -> new ExpansionOperation.Change(cell.pos(), cache.get(cell.pos()).state(), cell.material())).toList();
        return new LevelThreePlan(site, changes, cache.size());
    }
}
