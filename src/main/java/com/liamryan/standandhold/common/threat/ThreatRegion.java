package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Fixed 4x4-chunk regions; changing config cannot reinterpret saved coordinates. */
public record ThreatRegion(Identifier dimension, int x, int z) {
    public static ThreatRegion at(Identifier dimension, BlockPos pos) { return new ThreatRegion(dimension, Math.floorDiv(pos.getX(), 64), Math.floorDiv(pos.getZ(), 64)); }
    public String key() { return dimension + "@" + x + "," + z; }
    public BlockPos center(int y) { return new BlockPos(x * 64 + 32, y, z * 64 + 32); }
    public static Optional<ThreatRegion> parse(String key) {
        if (key == null || key.length() > 200) { return Optional.empty(); }
        String[] parts = key.split("@", -1);
        if (parts.length != 2) { return Optional.empty(); }
        try {
            FoundationCatalog.requireId(parts[0]);
            String[] coordinates = parts[1].split(",", -1);
            if (coordinates.length != 2) { return Optional.empty(); }
            int x = Integer.parseInt(coordinates[0]), z = Integer.parseInt(coordinates[1]);
            if (Math.abs((long) x) > 468750 || Math.abs((long) z) > 468750) { return Optional.empty(); }
            var result = new ThreatRegion(Identifier.parse(parts[0]), x, z);
            return result.key().equals(key) ? Optional.of(result) : Optional.empty();
        } catch (IllegalArgumentException exception) { return Optional.empty(); }
    }
}
