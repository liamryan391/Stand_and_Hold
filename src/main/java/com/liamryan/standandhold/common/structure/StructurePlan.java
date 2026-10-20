package com.liamryan.standandhold.common.structure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

/** Original fixed-height layouts and bounded terrain planning, independent of registry bootstrap. */
public record StructurePlan(BlockPos commandPost, List<Cell> cells) {
    public static final int CLEARANCE = 5;
    public StructurePlan { commandPost = commandPost.immutable(); cells = List.copyOf(cells); }
    public enum Material { AIR, FLOOR, WALL, ROOF, LIGHT, COMMAND_POST, LAB, CRATE, COMMAND_MARKER, RESEARCH_MARKER, BARRACKS_MARKER, SUPPLY_MARKER }
    public record Cell(BlockPos pos, Material material) { }
    public interface Terrain {
        int surface(int x, int z);
        boolean ground(BlockPos pos);
        boolean clear(BlockPos pos);
        boolean writable(BlockPos pos);
    }

    public static boolean candidate(long seed, int chunkX, int chunkZ, int spacing, int chance, long salt) {
        if (spacing < 1 || spacing > 256 || chance < 1 || chance > 10000) { throw new IllegalArgumentException("Invalid structure spacing/chance."); }
        int regionX = Math.floorDiv(chunkX, spacing), regionZ = Math.floorDiv(chunkZ, spacing);
        RandomSource random = RandomSource.create(seed ^ (regionX * 341873128712L) ^ (regionZ * 132897987541L) ^ salt);
        return Math.floorMod(chunkX, spacing) == random.nextInt(spacing)
                && Math.floorMod(chunkZ, spacing) == random.nextInt(spacing) && random.nextInt(chance) == 0;
    }

    public static StructurePlan plan(StructureKind kind, int chunkX, int chunkZ, int radius, int maxSlope,
                                     int minY, int maxY, Terrain terrain) {
        if (radius < (kind == StructureKind.CHECKPOINT ? 3 : 5) || radius > (kind == StructureKind.CHECKPOINT ? 5 : 6)
                || maxSlope < 0 || maxSlope > 3 || Math.abs((long) chunkX) > 1874998 || Math.abs((long) chunkZ) > 1874998) {
            throw new IllegalArgumentException("Invalid structure footprint/terrain settings.");
        }
        int cx = chunkX * 16 + 8, cz = chunkZ * 16 + 8, size = 2 * radius + 1;
        int[] heights = new int[size * size];
        int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE, i = 0;
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                BlockPos column = new BlockPos(cx + x, minY, cz + z);
                if (!terrain.writable(column)) { throw new IllegalArgumentException("Footprint outside loaded/generating chunk or world border."); }
                int y = terrain.surface(cx + x, cz + z);
                if (y < minY + 1 || y > maxY - CLEARANCE) { throw new IllegalArgumentException("Surface outside safe build height."); }
                heights[i++] = y; low = Math.min(low, y); high = Math.max(high, y);
            }
        }
        if ((long) high - low > maxSlope) { throw new IllegalArgumentException("Terrain too steep (height difference " + ((long) high - low) + ")."); }
        Map<BlockPos, Material> cells = new LinkedHashMap<>();
        i = 0;
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                int y = heights[i++];
                BlockPos surface = new BlockPos(cx + x, y, cz + z);
                if (!terrain.ground(surface) || !terrain.ground(surface.below())) {
                    throw new IllegalArgumentException("Unsuitable support at " + surface.toShortString()
                            + ": surface and block below must be dry full natural ground (grass/dirt/stone/sand); no fluids/containers.");
                }
                for (int h = y + 1; h <= high + CLEARANCE; h++) {
                    BlockPos pos = new BlockPos(cx + x, h, cz + z);
                    if (!terrain.writable(pos) || !terrain.clear(pos)) { throw new IllegalArgumentException("Occupied or wet space above terrain; no blocks placed."); }
                    cells.put(pos, Material.AIR);
                }
                for (int h = y; h <= high; h++) { cells.put(new BlockPos(cx + x, h, cz + z), Material.FLOOR); }
            }
        }
        BlockPos origin = new BlockPos(cx, high + 1, cz);
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                if (Math.abs(x) != radius && Math.abs(z) != radius) { continue; }
                if (z == radius && Math.abs(x) <= 1) { continue; } // Three-wide south entrance.
                cells.put(origin.offset(x, 0, z), Material.WALL);
                if (kind == StructureKind.MAIN_BASE) { cells.put(origin.offset(x, 1, z), Material.WALL); }
            }
        }
        // A protected command nook; open sides preserve visibility and walking routes.
        for (int x = -1; x <= 1; x++) {
            for (int z = -2; z <= 0; z++) { cells.put(origin.offset(x, 3, z), Material.ROOF); }
        }
        for (int x : new int[]{-1, 1}) {
            for (int y = 0; y < 3; y++) { cells.put(origin.offset(x, y, -2), Material.WALL); }
        }
        BlockPos command = origin.offset(0, 0, -1);
        cells.put(command, Material.COMMAND_POST);
        cells.put(origin.offset(radius - 1, 0, -radius + 1), Material.CRATE);
        for (int x : new int[]{-radius + 1, radius - 1}) {
            for (int z : new int[]{-radius + 1, radius - 1}) {
                if (x == radius - 1 && z == -radius + 1) { continue; }
                cells.put(origin.offset(x, 0, z), Material.LIGHT);
            }
        }
        if (kind == StructureKind.MAIN_BASE) {
            cells.put(origin.offset(-radius + 2, 0, -radius + 2), Material.LAB);
            cells.put(origin.offset(0, -1, -1), Material.COMMAND_MARKER);
            cells.put(origin.offset(-radius + 2, -1, -radius + 2), Material.RESEARCH_MARKER);
            cells.put(origin.offset(-radius + 2, -1, radius - 2), Material.BARRACKS_MARKER);
            cells.put(origin.offset(radius - 1, -1, -radius + 1), Material.SUPPLY_MARKER);
            for (int x = -radius + 2; x <= -radius + 4; x++) { cells.put(origin.offset(x, 0, radius - 2), Material.ROOF); }
        }
        return new StructurePlan(command, cells.entrySet().stream().map(e -> new Cell(e.getKey(), e.getValue())).toList());
    }
}
