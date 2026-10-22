package com.liamryan.standandhold.common.threat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public record ResponseRecord(long nextRaid, long nextReinforcement, long raids, long reinforcements, Set<UUID> raiders) {
    public ResponseRecord {
        raiders = Set.copyOf(raiders);
        if (nextRaid < 0 || nextReinforcement < 0 || raids < 0 || reinforcements < 0 || raiders.size() > 8) { throw new IllegalArgumentException("Invalid response record."); }
    }
    public static long after(long now, int ticks) { return ThreatRecord.credit(now, ticks); }
    public static ResponseRecord initial(long now, int grace) { return new ResponseRecord(after(now, grace), after(now, grace), 0, 0, Set.of()); }
    public ResponseRecord timers(long raid, long reinforcement) { return new ResponseRecord(raid, reinforcement, raids, reinforcements, raiders); }
    public ResponseRecord counted(boolean raid) { return new ResponseRecord(nextRaid, nextReinforcement, ThreatRecord.credit(raids, raid ? 1 : 0), ThreatRecord.credit(reinforcements, raid ? 0 : 1), raiders); }
    public ResponseRecord member(UUID id, boolean add) {
        var copy = new HashSet<>(raiders);
        if (add) { copy.add(id); } else { copy.remove(id); }
        return new ResponseRecord(nextRaid, nextReinforcement, raids, reinforcements, copy);
    }
    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putLong("next_raid", nextRaid); tag.putLong("next_reinforcement", nextReinforcement);
        tag.putLong("raids", raids); tag.putLong("reinforcements", reinforcements);
        var members = new CompoundTag(); raiders.forEach(id -> members.putBoolean(id.toString(), true)); tag.put("raiders", members);
        return tag;
    }
    public static ResponseRecord load(CompoundTag tag) {
        if (!(tag.get("raiders") instanceof CompoundTag members) || members.size() > 8) { throw new IllegalStateException("Malformed raid roster."); }
        Set<UUID> ids = new HashSet<>();
        for (String key : members.keySet()) {
            UUID id = UUID.fromString(key);
            if (!id.toString().equals(key) || ThreatWorldData.number(members, key) != 1) { throw new IllegalStateException("Malformed raider reservation."); }
            ids.add(id);
        }
        return new ResponseRecord(ThreatWorldData.number(tag, "next_raid"), ThreatWorldData.number(tag, "next_reinforcement"),
                ThreatWorldData.number(tag, "raids"), ThreatWorldData.number(tag, "reinforcements"), ids);
    }
}
