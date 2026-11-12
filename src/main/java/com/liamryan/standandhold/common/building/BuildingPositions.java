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
    /**
     * Removes one registered record only when its chunk is loaded and no Command Post is there (the same rule as
     * {@code commandpost nearest}). Unloaded or other-dimension records are kept: never request chunks.
     */
    public static boolean cleanIfStale(ServerLevel world, HumanWorldData data, String key) {
        if (!stale(key, world.dimension().identifier(), world::hasChunkAt,
                pos -> world.getBlockEntity(pos) instanceof BuildingBlockEntity building && building.kind() == BuildingKind.COMMAND_POST)) { return false; }
        data.removeCommandPost(key);
        if (data.mainBaseKeys().contains(key)) { data.removeMainBase(key); }
        return true;
    }

    /**
     * First-time post credit needs a loaded, real Command Post at a registered position. Loaded stale records met on
     * the way are cleaned; unloaded records neither count nor are removed. Bounded by the 4,096-entry registry.
     */
    public static boolean anyLivePost(net.minecraft.server.MinecraftServer server, HumanWorldData data) {
        for (String key : data.commandPostKeys()) {
            var parsed = BuildingPosition.parse(key);
            if (parsed.isEmpty()) { continue; }
            var world = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, parsed.get().dimension()));
            if (world == null || !world.hasChunkAt(parsed.get().pos())) { continue; }
            if (world.getBlockEntity(parsed.get().pos()) instanceof BuildingBlockEntity building && building.kind() == BuildingKind.COMMAND_POST
                    && !building.isRemoved()) { return true; }
            cleanIfStale(world, data, key);
        }
        return false;
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
