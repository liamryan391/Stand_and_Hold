package com.liamryan.standandhold.common.structure;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import static com.liamryan.standandhold.common.structure.ExpansionPlan.Material.*;

/** Authored revision-2 delta; never regenerates buildings or infers ownership from a matching floor. */
public record LevelTwoPlan(SiteProvenance site, List<ExpansionOperation.Change> changes, int reads) {
    public record Sample(CompoundTag state, boolean ground, boolean clear, boolean tile, boolean wet) { }
    public interface Terrain {
        boolean loaded(BlockPos pos);
        boolean insideBorder(BlockPos pos);
        Sample read(BlockPos pos);
    }
    public LevelTwoPlan { changes = List.copyOf(changes); }
    public static List<ExpansionPlan.Cell> layout(SiteProvenance site, int source) {
        if (site.radius() != 4 || source < 0 || source > 1) { throw new IllegalArgumentException("Unsupported Level-2 source layout."); }
        var cells = new LinkedHashMap<BlockPos, ExpansionPlan.Material>();
        if (source == 0) { ExpansionPlan.layout(site).forEach(cell -> cells.put(cell.pos(), cell.material())); }
        var c = site.center();
        for (int z = -4; z <= 4; z++) { for (int x = -4; x <= 4; x++) {
            if ((Math.abs(x) == 4 || Math.abs(z) == 4) && !(z == 4 && Math.abs(x) <= 1)) { cells.put(c.offset(x, 1, z), AIR); }
        } }
        // A small west watch deck, approached from the south; original lab/command/crate stay clear.
        for (int z = -1; z <= 0; z++) { for (int x = -5; x <= -3; x++) {
            cells.put(c.offset(x, 1, z), WALL); cells.put(c.offset(x, 2, z), FLOOR);
            if (z == -1 || x != -4) { cells.put(c.offset(x, 3, z), WALL); }
        } }
        cells.put(c.offset(-4, 1, 2), STEP); cells.put(c.offset(-4, 1, 1), WALL); cells.put(c.offset(-4, 2, 1), STEP);
        for (int x : new int[]{-2, 2}) { cells.put(c.offset(x, 2, 6), WALL); cells.put(c.offset(x, 3, 6), LIGHT); }
        // Do not store no-op writes: original wall blocks already support two deck columns and a stair.
        cells.entrySet().removeIf(entry -> original(site, entry.getKey()).equals(ExpansionOperation.afterState(entry.getValue())));
        if (cells.size() > ExpansionPlan.MAX_CELLS) { throw new IllegalArgumentException("Level-2 write budget exceeded."); }
        return cells.entrySet().stream().map(e -> new ExpansionPlan.Cell(e.getKey(), e.getValue())).toList();
    }
    public static LevelTwoPlan inspect(SiteProvenance site, int level, int minY, int maxY, Terrain terrain) {
        int source = site.appliedRevision();
        if (site.radius() != 4 || source < 0 || source > 1 || level < 2 || level > 5) {
            throw new IllegalArgumentException("Level-2 yard needs a proven radius-4 checkpoint at local level 2+ and source revision 0/1.");
        }
        var reader = new Reader(site.center(), minY, maxY, terrain); var c = site.center();
        var apron = new HashMap<BlockPos, ExpansionPlan.Material>();
        ExpansionPlan.layout(site).forEach(cell -> apron.put(cell.pos(), cell.material()));
        for (int z = -6; z <= 6; z++) { for (int x = -6; x <= 6; x++) {
            if (Math.abs(x) <= 4 && Math.abs(z) <= 4) { continue; }
            var floor = c.offset(x, 0, z);
            if (source == 0) { reader.ground(floor); } else { reader.exact(floor, ExpansionOperation.afterState(FLOOR)); }
            reader.ground(floor.below());
            for (int y = 1; y <= 5; y++) {
                var pos = floor.above(y); var expected = apron.get(pos);
                if (source == 1 && expected != null) { reader.exact(pos, ExpansionOperation.afterState(expected)); }
                else { reader.clear(pos); }
            }
        } }
        // Refuse customised/supporting wall columns and watch headroom rather than dropping player builds.
        for (int z = -4; z <= 4; z++) { for (int x = -4; x <= 4; x++) {
            if ((Math.abs(x) != 4 && Math.abs(z) != 4) || z == 4 && Math.abs(x) <= 1) { continue; }
            for (int y = 0; y <= 5; y++) { reader.exact(c.offset(x, y, z), original(site, c.offset(x, y, z))); }
        } }
        for (int z = -1; z <= 2; z++) { for (int x = -5; x <= -3; x++) {
            if (x < -4) { continue; } // Outside columns already checked above.
            for (int y = 0; y <= 5; y++) { reader.exact(c.offset(x, y, z), original(site, c.offset(x, y, z))); }
        } }
        var changes = layout(site, source).stream().map(cell -> new ExpansionOperation.Change(cell.pos(), reader.at(cell.pos()).state(), cell.material())).toList();
        return new LevelTwoPlan(site, changes, reader.cache.size());
    }
    private static CompoundTag original(SiteProvenance site, BlockPos pos) {
        int x = pos.getX() - site.center().getX(), y = pos.getY() - site.center().getY(), z = pos.getZ() - site.center().getZ();
        if (Math.abs(x) > 4 || Math.abs(z) > 4) { return new CompoundTag(); }
        if (y == 0) { return ExpansionOperation.afterState(FLOOR); }
        return ExpansionOperation.afterState(y == 1 && (Math.abs(x) == 4 || Math.abs(z) == 4)
                && !(z == 4 && Math.abs(x) <= 1) ? WALL : AIR);
    }
    private static final class Reader {
        private final BlockPos center; private final int minY, maxY; private final Terrain terrain;
        private final Map<BlockPos, Sample> cache = new HashMap<>();
        private Reader(BlockPos center, int minY, int maxY, Terrain terrain) { this.center = center; this.minY = minY; this.maxY = maxY; this.terrain = terrain; }
        private Sample at(BlockPos pos) {
            if (pos.getX() >> 4 != center.getX() >> 4 || pos.getZ() >> 4 != center.getZ() >> 4 || pos.getY() < minY || pos.getY() >= maxY || !terrain.loaded(pos)) {
                throw new IllegalArgumentException("Yard chunk/height unavailable; no chunk requested.");
            }
            if (!terrain.insideBorder(pos)) { throw new IllegalArgumentException("Yard outside world border."); }
            return cache.computeIfAbsent(pos.immutable(), key -> {
                if (cache.size() >= ExpansionPlan.MAX_READS) { throw new IllegalArgumentException("Yard read budget exceeded."); }
                var value = terrain.read(key);
                if (value.tile() || value.wet()) { throw new IllegalArgumentException("Protected container/fluid at " + key.toShortString() + "; yard changed nothing."); }
                return value;
            });
        }
        private void ground(BlockPos pos) { if (!at(pos).ground()) { throw new IllegalArgumentException("Yard needs flat dry natural support at " + pos.toShortString()); } }
        private void clear(BlockPos pos) { if (!at(pos).clear()) { throw new IllegalArgumentException("Occupied yard space at " + pos.toShortString() + "; move obstruction, then Retry."); } }
        private void exact(BlockPos pos, CompoundTag expected) {
            if (!at(pos).state().equals(expected)) { throw new IllegalArgumentException("Original wall/watch/apron changed at " + pos.toShortString() + "; no overwrite or automatic repair."); }
        }
    }
}
