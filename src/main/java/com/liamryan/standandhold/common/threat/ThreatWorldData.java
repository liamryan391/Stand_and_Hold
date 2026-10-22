package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.building.BuildingPosition;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Independent modern save: adding response data never reinterprets progression/quota receipts. */
public final class ThreatWorldData extends SavedData {
    public static final int CURRENT_VERSION = 1;
    public static final Codec<ThreatWorldData> CODEC = CompoundTag.CODEC.xmap(ThreatWorldData::load, ThreatWorldData::save);
    public static final SavedDataType<ThreatWorldData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "threat_response"), ThreatWorldData::new, CODEC);
    private CompoundTag retained = new CompoundTag(), regions = new CompoundTag(), responses = new CompoundTag();
    private boolean blocked;
    public static ThreatWorldData get(MinecraftServer server) {
        if (!server.isSameThread()) { throw new IllegalStateException("Threat response requires the server thread."); }
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
    public void requireWritable() { if (blocked) { throw new IllegalStateException("Unsupported/malformed threat response save; restore a compatible whole-world backup."); } }
    public List<String> regionKeys() { requireWritable(); return regions.keySet().stream().sorted().toList(); }
    public ThreatRecord region(ThreatRegion key, long now, int interval, int amount) {
        requireWritable();
        if (!(regions.get(key.key()) instanceof CompoundTag tag)) { return ThreatRecord.empty(now); }
        var old = ThreatRecord.load(tag); var next = old.decay(now, interval, amount);
        if (!next.equals(old)) { putRegion(key, next, 1024); }
        return next;
    }
    public boolean putRegion(ThreatRegion key, ThreatRecord value, int limit) {
        requireWritable();
        if (limit < 1 || limit > 1024) { throw new IllegalArgumentException("Invalid region limit."); }
        if (!regions.contains(key.key()) && regions.size() >= limit) { return false; }
        CompoundTag old = regions.getCompoundOrEmpty(key.key());
        CompoundTag next = merge(old, value.save());
        if (!old.equals(next)) { regions.put(key.key(), next); setDirty(); }
        return true;
    }
    public boolean resetRegion(ThreatRegion key) { requireWritable(); if (!regions.contains(key.key())) { return false; } regions.remove(key.key()); setDirty(); return true; }
    public ResponseRecord response(String post) {
        requireWritable(); requirePost(post);
        return responses.contains(post) ? ResponseRecord.load(responses.getCompoundOrEmpty(post)) : null;
    }
    public void putResponse(String post, ResponseRecord value) {
        requireWritable(); requirePost(post);
        if (!responses.contains(post) && responses.size() >= 4096) { throw new IllegalStateException("Response ledger full; no new event sites can be registered."); }
        CompoundTag old = responses.getCompoundOrEmpty(post), next = merge(old, value.save());
        if (!old.equals(next)) { responses.put(post, next); setDirty(); }
    }
    public boolean releaseRaider(String post, java.util.UUID id) {
        var record = response(post);
        if (record == null || !record.raiders().contains(id)) { return false; }
        putResponse(post, record.member(id, false)); return true;
    }
    private static void requirePost(String post) { if (BuildingPosition.parse(post).filter(p -> p.key().equals(post)).isEmpty()) { throw new IllegalArgumentException("Invalid response post."); } }
    private static CompoundTag merge(CompoundTag old, CompoundTag values) { var result = old.copy(); for (String key : values.keySet()) { result.put(key, values.get(key)); } return result; }
    static long number(CompoundTag tag, String key) {
        Tag value = tag.get(key);
        if (!(value instanceof ByteTag || value instanceof ShortTag || value instanceof IntTag || value instanceof LongTag) || ((NumericTag) value).longValue() < 0) {
            throw new IllegalStateException("Malformed response number: " + key);
        }
        return ((NumericTag) value).longValue();
    }
    private static ThreatWorldData load(CompoundTag tag) {
        var data = new ThreatWorldData(); data.retained = tag.copy();
        try {
            long version = tag.contains("data_version") ? number(tag, "data_version") : 0;
            if (version > CURRENT_VERSION || !(tag.get("regions") instanceof CompoundTag) || !(tag.get("responses") instanceof CompoundTag)) {
                // A completely empty pre-version save can acquire the new empty maps, not a damaged current save.
                if (version == 0 && !tag.contains("regions") && !tag.contains("responses")) { data.setDirty(); return data; }
                throw new IllegalStateException("Unsupported threat schema/maps.");
            }
            data.regions = tag.getCompoundOrEmpty("regions").copy(); data.responses = tag.getCompoundOrEmpty("responses").copy();
            if (data.regions.size() > 1024 || data.responses.size() > 4096) { throw new IllegalStateException("Response record limit exceeded."); }
            for (String key : data.regions.keySet()) {
                if (ThreatRegion.parse(key).isEmpty() || !(data.regions.get(key) instanceof CompoundTag entry)) { throw new IllegalStateException("Invalid region."); }
                ThreatRecord.load(entry);
            }
            for (String key : data.responses.keySet()) {
                requirePost(key);
                if (!(data.responses.get(key) instanceof CompoundTag entry)) { throw new IllegalStateException("Malformed response site."); }
                ResponseRecord.load(entry);
            }
            if (version < CURRENT_VERSION) { data.setDirty(); }
        } catch (IllegalArgumentException | IllegalStateException exception) { data.blocked = true; }
        return data;
    }
    private CompoundTag save() {
        if (blocked) { return retained.copy(); }
        var tag = retained.copy(); tag.putInt("data_version", CURRENT_VERSION); tag.put("regions", regions.copy()); tag.put("responses", responses.copy()); return tag;
    }
}
