package com.liamryan.standandhold.common.structure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/** Pure, read-only apron planner. Its terrain adapter deliberately has no mutation API. */
public record ExpansionPlan(SiteProvenance site, List<Cell> cells, int reads, int columns) {
    public static final int RADIUS = 6, MAX_READS = 2048, MAX_CELLS = 256, MAX_ENTITIES = 128;
    public enum Material { FLOOR, WALL, LIGHT }
    public enum Sample { GROUND, CLEAR, WET, TILE, OCCUPIED }
    public record Cell(BlockPos pos, Material material) { }
    public interface Terrain {
        boolean loaded(BlockPos pos);
        boolean insideBorder(BlockPos pos);
        Sample read(BlockPos pos);
    }
    public ExpansionPlan { cells = List.copyOf(cells); }
    public static ExpansionPlan inspect(SiteProvenance site, int level, int minY, int maxY, Terrain terrain) {
        requireEligible(site, level, minY, maxY);
        BlockPos center = site.center();
        var reader = new Reader(terrain, center, minY, maxY);
        int columns = 0;
        for (int z = -RADIUS; z <= RADIUS; z++) { for (int x = -RADIUS; x <= RADIUS; x++) {
            columns++;
            if (Math.abs(x) <= 4 && Math.abs(z) <= 4) { continue; }
            BlockPos floor = center.offset(x, 0, z);
            require(reader.at(floor), Sample.GROUND, floor);
            require(reader.at(floor.below()), Sample.GROUND, floor.below());
            for (int y = 1; y <= 5; y++) { require(reader.at(floor.above(y)), Sample.CLEAR, floor.above(y)); }
        } }
        return new ExpansionPlan(site, layout(site), reader.reads, columns);
    }
    /** Exact authored geometry, also used to reject altered/reordered persisted operation deltas. */
    public static List<Cell> layout(SiteProvenance site) {
        if (site.radius() != 4) { throw new IllegalArgumentException("Unsupported apron radius."); }
        var cells = new ArrayList<Cell>();
        for (int z = -RADIUS; z <= RADIUS; z++) { for (int x = -RADIUS; x <= RADIUS; x++) {
            if (Math.abs(x) <= 4 && Math.abs(z) <= 4) { continue; }
            var floor = site.center().offset(x, 0, z);
            cells.add(new Cell(floor, Material.FLOOR));
            if ((Math.abs(x) == RADIUS || Math.abs(z) == RADIUS) && !(z == RADIUS && Math.abs(x) <= 1)) { cells.add(new Cell(floor.above(), Material.WALL)); }
            if (Math.abs(x) == RADIUS && Math.abs(z) == RADIUS) { cells.add(new Cell(floor.above(2), Material.LIGHT)); }
        } }
        return List.copyOf(cells);
    }
    static void requireEligible(SiteProvenance site, int level, int minY, int maxY) {
        if (site.radius() != 4 || site.appliedRevision() != 0) { throw new IllegalArgumentException("Unsupported site radius/applied revision; only unexpanded radius-4 checkpoints qualify."); }
        if (level < 2 || level > 5) { throw new IllegalArgumentException("Requires Command Post local level 2 or higher; world stage alone is not an upgrade."); }
        BlockPos center = site.center();
        if (center.getY() - 1 < minY || center.getY() + 5 >= maxY) { throw new IllegalArgumentException("Expansion outside safe build height."); }
    }
    private static void require(Sample actual, Sample expected, BlockPos pos) {
        if (actual == expected) { return; }
        String reason = switch (actual) {
            case WET -> "Wet/fluid terrain"; case TILE -> "Protected block entity/container";
            default -> expected == Sample.GROUND ? "Uneven/unsafe support" : "Occupied overhead space";
        };
        throw new IllegalArgumentException(reason + " at " + pos.toShortString() + "; preview changed nothing.");
    }
    private static final class Reader {
        private final Terrain terrain;
        private final BlockPos center;
        private final int minY, maxY;
        private int reads;
        private Reader(Terrain terrain, BlockPos center, int minY, int maxY) { this.terrain = terrain; this.center = center; this.minY = minY; this.maxY = maxY; }
        private Sample at(BlockPos pos) {
            if (pos.getX() >> 4 != center.getX() >> 4 || pos.getZ() >> 4 != center.getZ() >> 4
                    || pos.getY() < minY || pos.getY() >= maxY || !terrain.loaded(pos)) {
                throw new IllegalArgumentException("Expansion chunk unavailable; no chunk requested.");
            }
            if (!terrain.insideBorder(pos)) { throw new IllegalArgumentException("Expansion outside world border."); }
            if (++reads > MAX_READS) { throw new IllegalArgumentException("Expansion read budget exceeded."); }
            return terrain.read(pos);
        }
    }
}
