package com.liamryan.standandhold.common.structure;

import java.util.*;

/** Server-scoped transient consent only. No ticker, persistence, or resource reservation. */
public final class ExpansionTokens<T> {
    private record Entry<T>(UUID player, long issued, T value) { }
    private final Map<UUID, Entry<T>> entries = new LinkedHashMap<>();
    public UUID issue(UUID player, long now, T value) {
        expire(now); revoke(player);
        if (entries.size() >= 64) { entries.remove(entries.keySet().iterator().next()); }
        UUID token = UUID.randomUUID(); entries.put(token, new Entry<>(player, now, value)); return token;
    }
    public T take(UUID player, UUID token, long now) {
        expire(now); var entry = entries.get(token);
        if (entry == null || !entry.player().equals(player)) { throw new IllegalArgumentException("Unknown, expired or foreign confirmation token; prepare again."); }
        entries.remove(token); return entry.value();
    }
    public void revoke(UUID player) { entries.values().removeIf(entry -> entry.player().equals(player)); }
    private void expire(long now) { entries.values().removeIf(entry -> now < entry.issued() || now - entry.issued() >= 200); }
    public int size() { return entries.size(); }
}
