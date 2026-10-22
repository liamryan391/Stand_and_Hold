package com.liamryan.standandhold.common.threat;

import net.minecraft.nbt.CompoundTag;

public record ThreatRecord(long score, long deaths, long kills, long losses, long attacks, long lastDecay, long lastAttack) {
    public static final long MAX_SCORE = 1_000_000;
    public ThreatRecord {
        if (score < 0 || score > MAX_SCORE || deaths < 0 || kills < 0 || losses < 0 || attacks < 0 || lastDecay < 0 || lastAttack < 0) {
            throw new IllegalArgumentException("Invalid threat record.");
        }
    }
    public static ThreatRecord empty(long now) { return new ThreatRecord(0, 0, 0, 0, 0, now, 0); }
    public static long credit(long old, long amount) { return old + Math.min(Math.max(0, amount), Long.MAX_VALUE - old); }
    public ThreatRecord decay(long now, int interval, int amount) {
        if (now < 0 || interval < 1 || amount < 0) { throw new IllegalArgumentException("Invalid decay settings."); }
        if (now < lastDecay) { return new ThreatRecord(score, deaths, kills, losses, attacks, now, Math.min(lastAttack, now)); }
        long periods = (now - lastDecay) / interval;
        if (periods == 0 || amount == 0 || score == 0) { return this; }
        long remove = Math.min(periods, (score + amount - 1) / amount) * amount;
        return new ThreatRecord(Math.max(0, score - remove), deaths, kills, losses, attacks, lastDecay + periods * interval, lastAttack);
    }
    public ThreatRecord observe(long now, int weight, boolean death, boolean humanKill, boolean loss, boolean attack) {
        // Attack counters represent one window per region, not every rapid damage callback.
        if (attack && attacks > 0 && now >= lastAttack && now - lastAttack < 20) { return this; }
        return new ThreatRecord(Math.min(MAX_SCORE, credit(score, weight)), credit(deaths, death ? 1 : 0), credit(kills, humanKill ? 1 : 0),
                credit(losses, loss ? 1 : 0), credit(attacks, attack ? 1 : 0), score == 0 ? now : lastDecay, attack ? now : lastAttack);
    }
    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putLong("score", score); tag.putLong("deaths", deaths); tag.putLong("kills", kills); tag.putLong("losses", losses);
        tag.putLong("attacks", attacks); tag.putLong("last_decay", lastDecay); tag.putLong("last_attack", lastAttack);
        return tag;
    }
    public static ThreatRecord load(CompoundTag tag) {
        return new ThreatRecord(ThreatWorldData.number(tag, "score"), ThreatWorldData.number(tag, "deaths"), ThreatWorldData.number(tag, "kills"),
                ThreatWorldData.number(tag, "losses"), ThreatWorldData.number(tag, "attacks"), ThreatWorldData.number(tag, "last_decay"), ThreatWorldData.number(tag, "last_attack"));
    }
}
