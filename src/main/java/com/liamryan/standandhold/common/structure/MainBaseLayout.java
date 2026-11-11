package com.liamryan.standandhold.common.structure;

import static com.liamryan.standandhold.common.structure.StructurePlan.Material.*;

import com.liamryan.standandhold.common.structure.StructurePlan.Material;
import java.util.Map;
import net.minecraft.core.BlockPos;

/** New-site furnishings only. Every cell fits inside the existing radius and clearance. */
final class MainBaseLayout {
    private MainBaseLayout() { }

    static void furnish(Map<BlockPos, Material> cells, BlockPos origin, int radius) {
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                if (Math.abs(x) == radius || Math.abs(z) == radius) {
                    if (z != radius || Math.abs(x) > 1) {
                        cells.put(origin.offset(x, 0, z), BASE_WALL);
                        cells.put(origin.offset(x, 1, z), WALL);
                    }
                }
                if (Math.abs(x) <= 1 && z >= 0
                        || Math.abs(z - 1) <= 1 && Math.abs(x) < radius) {
                    cells.put(origin.offset(x, -1, z), PATH);
                }
            }
        }
        rectangle(cells, origin, -2, 2, radius, radius, 3, BASE_ROOF);
        for (int x : new int[]{-2, 2}) { cells.put(origin.offset(x, 2, radius), BASE_WALL); }

        rectangle(cells, origin, -1, 1, -3, 0, 3, BASE_ROOF);
        rectangle(cells, origin, -1, 1, -2, -1, -1, COMMAND_MARKER);
        for (int y = 0; y <= 2; y++) { rectangle(cells, origin, -1, 1, -3, -3, y, BASE_WALL); }
        cells.put(origin.north(), COMMAND_POST);

        bay(cells, origin.offset(2 - radius, 0, 2 - radius), LAB, RESEARCH_MARKER, -1);
        bay(cells, origin.offset(radius - 1, 0, 1 - radius), CRATE, SUPPLY_MARKER, 1);

        rectangle(cells, origin, 1 - radius, 3 - radius, radius - 3, radius - 1, 3, BASE_ROOF);
        rectangle(cells, origin, 1 - radius, 3 - radius, radius - 3, radius - 1, -1, BARRACKS_MARKER);
        rectangle(cells, origin, 1 - radius, 3 - radius, radius - 2, radius - 2, 0, ROOF);
        for (int y = 0; y <= 2; y++) { cells.put(origin.offset(1 - radius, y, radius - 1), BASE_WALL); }
        for (int x : new int[]{1 - radius, radius - 1}) {
            cells.put(origin.offset(x, 0, 0), LIGHT);
            cells.put(origin.offset(x, 2, radius), LIGHT);
        }
        cells.put(origin.offset(-2, 0, -1), LIGHT);
        cells.put(origin.offset(2, 0, -1), LIGHT);
    }

    private static void bay(Map<BlockPos, Material> cells, BlockPos tile, Material building, Material marker, int outerSide) {
        rectangle(cells, tile, -1, 1, -1, 1, 3, BASE_ROOF);
        rectangle(cells, tile, -1, 1, 0, 1, -1, marker);
        for (int y = 0; y <= 2; y++) { cells.put(tile.offset(outerSide, y, -1), BASE_WALL); }
        cells.put(tile, building);
    }

    private static void rectangle(Map<BlockPos, Material> cells, BlockPos origin,
                                  int minX, int maxX, int minZ, int maxZ, int y, Material material) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) { cells.put(origin.offset(x, y, z), material); }
        }
    }
}
