package com.liamryan.standandhold.common.structure;

import java.util.*;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;

/** Bounded intent/diagnostic receipt, not a durable write-ahead journal. */
public record ExpansionOperation(UUID id, UUID siteId, List<Change> changes, String protectedDigest,
                                 Phase phase, int index, String detail, int sourceRevision, int targetRevision) {
    public ExpansionOperation(UUID id, UUID siteId, List<Change> changes, String protectedDigest, Phase phase, int index, String detail) {
        this(id, siteId, changes, protectedDigest, phase, index, detail, 0, 1);
    }
    public enum Phase { PREPARED, REVIEW, APPLIED }
    public enum Observed { ALL_BEFORE, ALL_AFTER, MIXED, CONFLICT }
    public record Observation(Observed state, int firstConflict) { }
    public record Change(BlockPos pos, CompoundTag before, ExpansionPlan.Material material) {
        public Change { pos = pos.immutable(); validateState(before); before = before.copy(); Objects.requireNonNull(material); }
        @Override public CompoundTag before() { return before.copy(); }
        public CompoundTag after() { return afterState(material); }
    }
    public ExpansionOperation {
        Objects.requireNonNull(id); Objects.requireNonNull(siteId); Objects.requireNonNull(phase);
        changes = List.copyOf(changes);
        if (targetRevision != 1 && targetRevision != 2 && targetRevision != 3 || sourceRevision < 0 || sourceRevision >= targetRevision
                || targetRevision == 3 && (sourceRevision != 2 || changes.size() != 63)
                || targetRevision == 1 && (sourceRevision != 0 || changes.size() != 137)
                || changes.isEmpty() || changes.size() > ExpansionPlan.MAX_CELLS || protectedDigest == null || !protectedDigest.matches("[0-9a-f]{64}")
                || index < 0 || index > changes.size() || detail == null || detail.length() > 160
                || phase == Phase.PREPARED && index != 0 || phase == Phase.APPLIED && index != changes.size()) {
            throw new IllegalArgumentException("Malformed expansion operation receipt.");
        }
        for (var cell : changes) { if (cell.before().equals(cell.after())) { throw new IllegalArgumentException("Expansion before/after states must differ."); } }
    }
    public void validateSite(SiteProvenance site) {
        var layout = targetRevision == 1 ? ExpansionPlan.layout(site) : targetRevision == 2 ? LevelTwoPlan.layout(site, sourceRevision) : LevelThreePlan.layout(site);
        if (!siteId.equals(site.id()) || !changes.stream().map(c -> new ExpansionPlan.Cell(c.pos(), c.material())).toList().equals(layout)) {
            throw new IllegalArgumentException("Operation does not match the exact authored site/layout revision.");
        }
        if (targetRevision == 3) {
            var source = LevelThreePlan.sourceStates(site);
            for (var change : changes) {
                if (!change.before().equals(source.getOrDefault(change.pos(), afterState(ExpansionPlan.Material.AIR)))) {
                    throw new IllegalArgumentException("Deck receipt does not match the exact authored source state.");
                }
            }
        }
    }
    public ExpansionOperation withPhase(Phase next, int at, String message) {
        return new ExpansionOperation(id, siteId, changes, protectedDigest, next, at, message.substring(0, Math.min(160, message.length())), sourceRevision, targetRevision);
    }
    public Observation observe(Function<BlockPos, CompoundTag> read) {
        int before = 0, after = 0;
        for (int i = 0; i < changes.size(); i++) {
            var change = changes.get(i); var actual = read.apply(change.pos());
            if (change.before().equals(actual)) { before++; }
            else if (change.after().equals(actual)) { after++; }
            else { return new Observation(Observed.CONFLICT, i); }
        }
        return new Observation(before == changes.size() ? Observed.ALL_BEFORE : after == changes.size() ? Observed.ALL_AFTER : Observed.MIXED, -1);
    }
    public CompoundTag save() {
        var tag = new CompoundTag(); tag.putInt("operation_version", targetRevision);
        if (targetRevision >= 2) { tag.putInt("source_revision", sourceRevision); tag.putInt("target_revision", targetRevision); }
        tag.putString("id", id.toString()); tag.putString("site_uuid", siteId.toString());
        tag.putString("protected_digest", protectedDigest); tag.putString("phase", phase.name());
        tag.putInt("index", index); tag.putString("detail", detail);
        var list = new ListTag();
        for (var cell : changes) {
            var row = new CompoundTag(); row.putLong("pos", cell.pos().asLong()); row.putString("material", cell.material().name());
            row.put("before", cell.before()); row.put("after", cell.after()); list.add(row);
        }
        tag.put("cells", list); return tag;
    }
    public static ExpansionOperation read(Tag raw, SiteProvenance site) {
        if (!(raw instanceof CompoundTag tag)) { throw new IllegalArgumentException("Unsupported/malformed expansion operation; retained for review."); }
        int version = integer(tag, "operation_version");
        if (version < 1 || version > 3 || version >= 2 && integer(tag, "target_revision") != version) { throw new IllegalArgumentException("Unsupported expansion operation version."); }
        if (!(tag.get("cells") instanceof ListTag list) || list.isEmpty() || list.size() > ExpansionPlan.MAX_CELLS || version == 1 && list.size() != 137) { throw new IllegalArgumentException("Malformed expansion cell list."); }
        var changes = new ArrayList<Change>();
        for (Tag entry : list) {
            if (!(entry instanceof CompoundTag row) || !(row.get("pos") instanceof LongTag)
                    || !(row.get("before") instanceof CompoundTag before) || !(row.get("after") instanceof CompoundTag after)) {
                throw new IllegalArgumentException("Malformed expansion cell.");
            }
            var material = ExpansionPlan.Material.valueOf(row.getStringOr("material", ""));
            if (!afterState(material).equals(after)) { throw new IllegalArgumentException("Unsupported expansion after-state."); }
            changes.add(new Change(BlockPos.of(row.getLongOr("pos", 0)), before, material));
        }
        var operation = new ExpansionOperation(uuid(tag, "id"), uuid(tag, "site_uuid"), changes, tag.getStringOr("protected_digest", ""),
                Phase.valueOf(tag.getStringOr("phase", "")), integer(tag, "index"), tag.getStringOr("detail", ""), version == 1 ? 0 : integer(tag, "source_revision"), version);
        operation.validateSite(site); return operation;
    }
    public static CompoundTag afterState(ExpansionPlan.Material material) {
        var tag = new CompoundTag(); tag.putString("id", switch (material) {
            case FLOOR -> "minecraft:stone_bricks"; case WALL -> "minecraft:cobblestone"; case LIGHT -> "minecraft:torch";
            case AIR -> "minecraft:air"; case STEP -> "minecraft:stone_brick_stairs";
        });
        if (material == ExpansionPlan.Material.STEP) {
            var properties = new CompoundTag(); properties.putString("facing", "north"); properties.putString("half", "bottom");
            properties.putString("shape", "straight"); properties.putString("waterlogged", "false"); tag.put("properties", properties);
        }
        return tag;
    }
    private static void validateState(CompoundTag tag) {
        if (tag == null || tag.keySet().size() > 2 || !(tag.get("id") instanceof StringTag)) { throw new IllegalArgumentException("Malformed expected block state."); }
        String name = tag.getStringOr("id", "");
        var parsed = name.length() <= 128 ? Identifier.tryParse(name) : null;
        if (parsed == null || !parsed.toString().equals(name)) { throw new IllegalArgumentException("Malformed expected block name."); }
        for (String key : tag.keySet()) { if (!key.equals("id") && !key.equals("properties")) { throw new IllegalArgumentException("Unknown expected-state field."); } }
        if (tag.contains("properties")) {
            if (!(tag.get("properties") instanceof CompoundTag properties) || properties.keySet().size() > 8) { throw new IllegalArgumentException("Malformed expected-state properties."); }
            for (String key : properties.keySet()) {
                if (key.length() > 32 || !(properties.get(key) instanceof StringTag) || properties.getStringOr(key, "").length() > 32) { throw new IllegalArgumentException("Oversized expected-state property."); }
            }
        }
    }
    private static int integer(CompoundTag tag, String key) {
        if (!(tag.get(key) instanceof IntTag)) { throw new IllegalArgumentException("Malformed operation field: " + key); }
        return tag.getIntOr(key, 0);
    }
    private static UUID uuid(CompoundTag tag, String key) {
        String text = tag.getStringOr(key, "");
        if (text.length() != 36 || !UUID.fromString(text).toString().equals(text)) { throw new IllegalArgumentException("Malformed operation UUID."); }
        return UUID.fromString(text);
    }
}
