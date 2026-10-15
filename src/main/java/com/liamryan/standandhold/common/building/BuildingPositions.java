package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.world.HumanWorldData;
import net.minecraft.server.level.ServerLevel;

public final class BuildingPositions {
    private BuildingPositions() { }
    /** Explicit command-time maintenance only. Never request chunks or inspect another dimension. */
    public static int cleanLoaded(ServerLevel world, HumanWorldData data) {
        int cleaned = 0;
        for (String key : data.commandPostKeys().stream().limit(4096).toList()) {
            var parsed = BuildingPosition.parse(key);
            boolean stale = parsed.isEmpty();
            if (parsed.isPresent()) {
                var position = parsed.get();
                if (!position.dimension().equals(world.dimension().identifier()) || !world.hasChunkAt(position.pos())) { continue; }
                stale = !(world.getBlockEntity(position.pos()) instanceof BuildingBlockEntity building)
                        || building.kind() != BuildingKind.COMMAND_POST;
            }
            if (stale) { data.removeCommandPost(key); cleaned++; }
        }
        return cleaned;
    }
}
