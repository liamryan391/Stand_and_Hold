package com.liamryan.standandhold.common.building;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record BuildingPosition(Identifier dimension, BlockPos pos) {
    public BuildingPosition { pos = pos.immutable(); }
    public String key() { return dimension + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ(); }
    public static Optional<BuildingPosition> parse(String key) {
        if (key == null) { return Optional.empty(); }
        if (key.length() > 200) { return Optional.empty(); }
        String[] parts = key.split("@", -1);
        if (parts.length != 2) { return Optional.empty(); }
        try { com.liamryan.standandhold.common.foundation.FoundationCatalog.requireId(parts[0]); }
        catch (IllegalArgumentException exception) { return Optional.empty(); }
        Identifier dimension = Identifier.tryParse(parts[0]);
        String[] coords = parts[1].split(",", -1);
        if (dimension == null || coords.length != 3) { return Optional.empty(); }
        try {
            int x = Integer.parseInt(coords[0]), y = Integer.parseInt(coords[1]), z = Integer.parseInt(coords[2]);
            if (Math.abs((long) x) > 30000000 || Math.abs((long) z) > 30000000 || y < -2048 || y > 2047) { return Optional.empty(); }
            return Optional.of(new BuildingPosition(dimension, new BlockPos(x, y, z)));
        } catch (NumberFormatException exception) { return Optional.empty(); }
    }
}
