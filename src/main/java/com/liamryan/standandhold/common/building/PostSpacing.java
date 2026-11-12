package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Placement-time spacing for player-placed Command Posts, so crafted posts cannot be stacked into a point/supply farm.
 * Generated sites and admin block commands never call this. One bounded pass over the 4,096-entry post registry.
 */
public final class PostSpacing {
    private PostSpacing() { }

    /** Horizontal distance to the nearest registered post or Main Base in the same dimension; empty when none. */
    public static OptionalDouble nearest(Iterable<String> keys, Identifier dimension, BlockPos pos) {
        double best = Double.MAX_VALUE;
        boolean found = false;
        for (String key : keys) {
            var parsed = BuildingPosition.parse(key);
            if (parsed.isEmpty() || !parsed.get().dimension().equals(dimension) || parsed.get().pos().equals(pos)) { continue; }
            double dx = parsed.get().pos().getX() - pos.getX(), dz = parsed.get().pos().getZ() - pos.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < best) { best = distance; found = true; }
        }
        return found ? OptionalDouble.of(best) : OptionalDouble.empty();
    }

    /** Whole-block distance to a post that is too close, or empty when placement is allowed. Spacing 0 disables the rule. */
    public static OptionalInt tooClose(HumanWorldData data, Identifier dimension, BlockPos pos, int spacing) {
        if (spacing <= 0) { return OptionalInt.empty(); }
        var nearest = nearest(data.commandPostKeys(), dimension, pos);
        return nearest.isPresent() && nearest.getAsDouble() < spacing ? OptionalInt.of((int) Math.floor(nearest.getAsDouble())) : OptionalInt.empty();
    }
}
