package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.npc.HumanTier;
import com.liamryan.standandhold.common.npc.DefenderCapacity;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import javax.imageio.ImageIO;

final class NpcChecks {
    private static int assertions;
    static void run() throws Exception {
        var duty = com.liamryan.standandhold.common.npc.DefenderDuty.DEFAULT;
        var patrol = duty.mode(com.liamryan.standandhold.common.npc.DefenderDuty.Mode.PATROL).watch(UUID.randomUUID());
        check(com.liamryan.standandhold.common.npc.DefenderDuty.read(patrol.save()).equals(patrol), "duty policy and unique watch UUID round trip");
        check(patrol.mode(com.liamryan.standandhold.common.npc.DefenderDuty.Mode.GUARD).equals(duty), "guard releases watch policy without changing recruitment");
        var futureDuty = patrol.save(); futureDuty.putInt("version", 99);
        expect(() -> com.liamryan.standandhold.common.npc.DefenderDuty.read(futureDuty));
        var malformedDuty = patrol.save(); malformedDuty.putString("watch", "bad");
        expect(() -> com.liamryan.standandhold.common.npc.DefenderDuty.read(malformedDuty));
        var malformedMode = patrol.save(); malformedMode.putString("mode", "1");
        expect(() -> com.liamryan.standandhold.common.npc.DefenderDuty.read(malformedMode));
        check(DefenderCapacity.validProfile(DefenderCapacity.DEFAULT_LEVELS), "default capacity profile");
        check(DefenderCapacity.validProfile(List.of(0L, 0L, 8L, 16L, 16L)), "integer TOML profile and disabled early levels");
        for (Object invalid : List.of("bad", List.of(3, 5), List.of(3, 2, 6, 7, 8), List.of(-1, 5, 6, 7, 8),
                List.of(3, 5, 6, 7, 17), List.of(3, 5, 6, 7, Long.MAX_VALUE), List.of(3, 5, 6, 7, 8.0),
                java.util.Arrays.asList(3, 5, 6, 7, null))) {
            check(!DefenderCapacity.validProfile(invalid), "malformed profile rejected");
            check(DefenderCapacity.effective(5, invalid, 3, 8) == 3, "fallback never bypasses existing ceiling");
        }
        check(!DefenderCapacity.validProfile(null), "missing profile invalid");
        for (int level = 1; level <= 5; level++) {
            for (int ceiling = 0; ceiling <= 16; ceiling++) {
                for (int area = 1; area <= 64; area++) {
                    int effective = DefenderCapacity.effective(level, DefenderCapacity.DEFAULT_LEVELS, ceiling, area);
                    check(effective == Math.min(DefenderCapacity.DEFAULT_LEVELS.get(level - 1), Math.min(ceiling, area)), "all capacity boundaries");
                }
            }
            check(DefenderCapacity.effective(level, List.of(0, 0, 0, 0, 0), 16, 8) == 0, "zero profile disables");
        }
        expect(() -> DefenderCapacity.effective(0, null, 16, 8));
        expect(() -> DefenderCapacity.effective(6, null, 16, 8));
        expect(() -> DefenderCapacity.effective(1, null, 17, 8));
        expect(() -> DefenderCapacity.effective(1, null, 16, 0));
        check(HumanTier.unlocked(0, 0, 1) == HumanTier.SURVIVOR_DEFENDER, "stage zero survivor");
        check(HumanTier.unlocked(1, 0, 1) == HumanTier.ARMY_RIFLEMAN, "stage one army");
        check(HumanTier.unlocked(6, 0, 1) == HumanTier.ARMY_RIFLEMAN, "higher stages reuse implemented tier");
        check(HumanTier.unlocked(0, 2, 3) == null, "custom stage gates can block all deployment");
        check(HumanTier.unlocked(2, 2, 3) == HumanTier.SURVIVOR_DEFENDER, "custom survivor gate");
        expect(() -> HumanTier.unlocked(7, 0, 1));
        for (int stage = 0; stage <= 6; stage++) {
            final int current = stage;
            for (int mask = 0; mask < (1 << HumanTier.values().length); mask++) {
                final int gates = mask;
                HumanTier expected = null;
                for (HumanTier tier : HumanTier.values()) {
                    if (stage >= tier.stage && (mask & 1 << tier.ordinal()) != 0) { expected = tier; }
                }
                check(HumanTier.unlocked(current, tier -> tier.stage, tier -> (gates & 1 << tier.ordinal()) != 0) == expected,
                        "every stage/research combination selects highest eligible role");
            }
        }
        check(HumanTier.unlocked(2, tier -> tier == HumanTier.ELITE_SOLDIER ? 2 : 6, tier -> true) == HumanTier.ELITE_SOLDIER,
                "configured stages may reorder gates without selecting a locked lower role");
        check(HumanTier.HEAVY_SOLDIER.usesBlade() && !HumanTier.HEAVY_SOLDIER.usesPrototype()
                && HumanTier.ELITE_SOLDIER.usesPrototype() && !HumanTier.ELITE_SOLDIER.usesBlade(), "roles reuse separate kit paths");
        check(HumanTier.HEAVY_SOLDIER.health > HumanTier.ELITE_SOLDIER.health
                && HumanTier.HEAVY_SOLDIER.armor > HumanTier.ELITE_SOLDIER.armor
                && HumanTier.HEAVY_SOLDIER.speed < HumanTier.ELITE_SOLDIER.speed, "heavy tank versus mobile elite contract");
        check(HumanTier.SUPER_ELITE.usesBlade() && HumanTier.SUPER_ELITE.armor > HumanTier.HEAVY_SOLDIER.armor
                && HumanTier.SPECIAL_DIVISION.usesPrototype() && HumanTier.SPECIAL_DIVISION.speed > HumanTier.SUPER_ELITE.speed,
                "specialists have distinct existing-kit roles");
        check(HumanTier.hasRoom(2, 3, 7, 8), "last available quota");
        check(!HumanTier.hasRoom(3, 3, 0, 8), "post cap enforced");
        check(!HumanTier.hasRoom(0, 3, 8, 8), "overlapping post/egg area cap enforced");
        check(!HumanTier.hasRoom(0, 0, 0, 8), "zero cap disables deployment");
        check(!HumanTier.hasRoom(4, 2, 9, 8), "lowering caps cannot add more units");
        String key = "minecraft:overworld@0,64,0";
        var post = BuildingPosition.parse(key).orElseThrow();
        var data = new HumanWorldData();
        var first = UUID.randomUUID(); var second = UUID.randomUUID();
        expect(() -> data.reserveDefender(key, first, 3));
        data.registerCommandPost(post);
        check(data.reserveDefender(key, first, 2), "first reservation"); data.setDirty(false);
        check(!data.reserveDefender(key, first, 2) && !data.isDirty(), "duplicate does not dirty or consume quota");
        check(data.reserveDefender(key, second, 2), "second reservation"); data.setDirty(false);
        check(!data.reserveDefender(key, UUID.randomUUID(), 2) && !data.isDirty(), "full quota rejects without dirtying");
        var restored = decode(encode(data));
        check(restored.defenderCount(key) == 2, "reservations survive reload without loaded entities");
        restored.removeCommandPost(key);
        check(restored.defenderCount(key) == 2, "breaking/cleaning a post does not forget surviving units");
        restored.releaseDefender(key, first);
        check(restored.defenderCount(key) == 1, "confirmed removal releases even with home removed/unloaded");
        restored.releaseDefender(key, second); restored.setDirty(false);
        restored.releaseDefender(key, second);
        check(restored.defenderCount(key) == 0 && !restored.isDirty(), "repeat removal no-op");
        var old = encode(data); old.putInt("data_version", 3); old.remove("defenders"); old.putString("future_note", "keep");
        var migrated = decode(old);
        check(migrated.dataVersion() == HumanWorldData.CURRENT_VERSION && migrated.defenderCount(key) == 0 && migrated.commandPostKeys().contains(key), "schema three gains empty quota without losing posts");
        check(encode(migrated).getStringOr("future_note", "").equals("keep"), "migration keeps unknown fields");
        var malformed = encode(data); malformed.putString("defenders", "do not delete");
        var blocked = decode(malformed);
        expect(() -> blocked.defenderCount(key));
        check(encode(blocked).getStringOr("defenders", "").equals("do not delete"), "malformed root retained, not reset into free quota");
        malformed = encode(data); malformed.getCompoundOrEmpty("defenders").getCompoundOrEmpty(key).putBoolean("bad uuid", true);
        var badUuid = decode(malformed);
        expect(() -> badUuid.defenderCount(key));
        expect(() -> data.reserveDefender(key, first, 17));
        expect(() -> data.defenderCount("bad position"));
        var missing = encode(data); missing.remove("defenders"); var missingLedger = decode(missing);
        expect(() -> missingLedger.defenderCount(key));
        divisionRoster();
        check(HumanTier.ARMY_RIFLEMAN.health > HumanTier.SURVIVOR_DEFENDER.health
                && HumanTier.ARMY_RIFLEMAN.damage > HumanTier.SURVIVOR_DEFENDER.damage, "distinct default tier stats");
        for (HumanTier tier : HumanTier.values()) {
            try (var stream = NpcChecks.class.getResourceAsStream("/assets/standandhold/textures/entity/" + tier.id + ".png")) {
                var image = ImageIO.read(stream);
                check(image.getWidth() == 64 && image.getHeight() == 64, "texture dimensions match the explicit humanoid and accessory atlas");
                for (int[] rect : new int[][] {{8, 0, 16, 8}, {0, 8, 32, 8}, {20, 16, 16, 4}, {16, 20, 24, 12},
                        {44, 16, 8, 4}, {40, 20, 16, 12}, {4, 16, 8, 4}, {0, 20, 16, 12}}) {
                    boolean opaque = true;
                    for (int y = rect[1]; y < rect[1] + rect[3]; y++) {
                        for (int x = rect[0]; x < rect[0] + rect[2]; x++) { opaque &= image.getRGB(x, y) >>> 24 == 255; }
                    }
                    check(opaque, "every base body face is opaque; limbs cannot disappear from mismatched UVs");
                }
                check(image.getRGB(32, 0) >>> 24 == 0, "unused hat layer remains transparent");
            }
            try (var stream = NpcChecks.class.getResourceAsStream("/assets/standandhold/textures/item/" + tier.id + "_spawn_egg.png")) {
                var image = ImageIO.read(stream);
                check(image.getWidth() == 16 && image.getHeight() == 16, "original test egg asset exists");
            }
        }
        System.out.println("NPC checks passed: " + assertions + " assertions.");
    }
    private static void divisionRoster() {
        var data = new HumanWorldData();
        var base = BuildingPosition.parse("minecraft:overworld@8,64,8").orElseThrow();
        var key = base.key();
        var first = UUID.randomUUID(); var second = UUID.randomUUID(); var regular = UUID.randomUUID();
        data.registerCommandPost(base);
        check(!data.reserveDefender(key, first, 3, true), "ordinary posts cannot reserve a Division slot");
        data.registerMainBase(base);
        check(data.reserveDefender(key, regular, 2) && data.reserveDefender(key, first, 2, true), "Division uses a normal slot");
        var encoded = encode(data);
        check(encoded.getCompoundOrEmpty("defenders").getCompoundOrEmpty(key).getBooleanOr(regular.toString(), false), "existing ordinary reservation encoding preserved");
        var restored = decode(encoded);
        check(restored.defenderCount(key) == 2 && restored.hasDivisionDefender(key), "unloaded Division reservation survives codec");
        restored.setDirty(false);
        check(!restored.reserveDefender(key, second, 3, true) && !restored.isDirty(), "no second specialist even with room");
        var malformed = encoded.copy();
        malformed.getCompoundOrEmpty("defenders").getCompoundOrEmpty(key).putByte(second.toString(), (byte) 2);
        var blocked = decode(malformed);
        expect(() -> blocked.hasDivisionDefender(key));
        check(encode(blocked).get("defenders").equals(malformed.get("defenders")), "ambiguous multiple specialists retained, never reset");
        restored.releaseDefender(key, first);
        check(!restored.hasDivisionDefender(key) && restored.reserveDefender(key, second, 2, true), "confirmed removal frees exactly one specialist slot");
    }
    private static CompoundTag encode(HumanWorldData data) { return (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(); }
    private static HumanWorldData decode(CompoundTag tag) { return HumanWorldData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(); }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
    private static void expect(Runnable action) {
        assertions++; try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("Expected safely rejected NPC operation");
    }
}
