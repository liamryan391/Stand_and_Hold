package com.liamryan.standandhold.common.npc;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;

/** Small post-owned policy, separate from recruitment and temporary response orders. */
public record DefenderDuty(Mode mode, UUID watch) {
    public enum Mode { GUARD, PATROL }
    public static final DefenderDuty DEFAULT = new DefenderDuty(Mode.GUARD, null);
    public static final int RESOLVE_TICKS = 40, PATH_TICKS = 40, MAX_ATTEMPTS = 3, BACKOFF_TICKS = 200;
    public DefenderDuty { java.util.Objects.requireNonNull(mode); }
    public DefenderDuty mode(Mode next) { return new DefenderDuty(next, next == Mode.GUARD ? null : watch); }
    public DefenderDuty watch(UUID next) { return new DefenderDuty(mode, next); }
    public CompoundTag save() {
        var tag = new CompoundTag(); tag.putInt("version", 1); tag.putInt("mode", mode.ordinal());
        if (watch != null) { tag.putString("watch", watch.toString()); }
        return tag;
    }
    public static DefenderDuty read(CompoundTag tag) {
        if (!(tag.get("version") instanceof IntTag) || tag.getIntOr("version", -1) != 1
                || !(tag.get("mode") instanceof IntTag) || tag.getIntOr("mode", -1) < 0 || tag.getIntOr("mode", -1) > 1) {
            throw new IllegalArgumentException("Unsupported duty policy; retained without automatic repair.");
        }
        UUID watch = null;
        if (tag.contains("watch")) {
            if (!(tag.get("watch") instanceof StringTag)) { throw new IllegalArgumentException("Malformed watch reservation."); }
            String id = tag.getStringOr("watch", ""); watch = UUID.fromString(id);
            if (!watch.toString().equals(id)) { throw new IllegalArgumentException("Malformed watch UUID."); }
        }
        return new DefenderDuty(Mode.values()[tag.getIntOr("mode", 0)], watch);
    }
}
