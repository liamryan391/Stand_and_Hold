package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.world.HumanWorldData;
import net.minecraft.server.level.ServerLevel;

public final class BuildingPositions {
    private BuildingPositions() { }
    /** Explicit command-time maintenance only. Never request chunks or inspect another dimension. */
    public static int cleanLoaded(ServerLevel world, HumanWorldData data) {
        return clean(data.commandPostKeys(), world, false, data::removeCommandPost)
                + clean(data.mainBaseKeys(), world, true, data::removeMainBase);
    }
    private static int clean(java.util.List<String> keys, ServerLevel world, boolean mainBase, java.util.function.Consumer<String> remove) {
        int cleaned = 0;
        for (String key : keys.stream().limit(4096).toList()) {
            boolean stale = stale(key, world.dimension().identifier(), world::hasChunkAt, pos ->
                    world.getBlockEntity(pos) instanceof BuildingBlockEntity building && building.kind() == BuildingKind.COMMAND_POST
                            && (!mainBase || building.isMainBase()));
            if (stale) { remove.accept(key); cleaned++; }
        }
        return cleaned;
    }
    public static boolean stale(String key, net.minecraft.resources.Identifier dimension,
                                java.util.function.Predicate<net.minecraft.core.BlockPos> loaded,
                                java.util.function.Predicate<net.minecraft.core.BlockPos> valid) {
        var parsed = BuildingPosition.parse(key);
        if (parsed.isEmpty()) { return true; }
        var position = parsed.get();
        return position.dimension().equals(dimension) && loaded.test(position.pos()) && !valid.test(position.pos());
    }
}
