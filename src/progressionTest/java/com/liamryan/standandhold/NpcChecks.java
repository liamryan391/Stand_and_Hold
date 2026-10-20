package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.npc.HumanTier;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import javax.imageio.ImageIO;

final class NpcChecks {
    private static int assertions;
    static void run() throws Exception {
        check(HumanTier.unlocked(0, 0, 1) == HumanTier.SURVIVOR_DEFENDER, "stage zero survivor");
        check(HumanTier.unlocked(1, 0, 1) == HumanTier.ARMY_RIFLEMAN, "stage one army");
        check(HumanTier.unlocked(6, 0, 1) == HumanTier.ARMY_RIFLEMAN, "higher stages reuse implemented tier");
        check(HumanTier.unlocked(0, 2, 3) == null, "custom stage gates can block all deployment");
        check(HumanTier.unlocked(2, 2, 3) == HumanTier.SURVIVOR_DEFENDER, "custom survivor gate");
        expect(() -> HumanTier.unlocked(7, 0, 1));
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
        check(HumanTier.ARMY_RIFLEMAN.health > HumanTier.SURVIVOR_DEFENDER.health
                && HumanTier.ARMY_RIFLEMAN.damage > HumanTier.SURVIVOR_DEFENDER.damage, "distinct default tier stats");
        for (HumanTier tier : HumanTier.values()) {
            try (var stream = NpcChecks.class.getResourceAsStream("/assets/standandhold/textures/entity/" + tier.id + ".png")) {
                var image = ImageIO.read(stream);
                check(image.getWidth() == 64 && image.getHeight() == 32, "texture dimensions match the registered classic humanoid atlas");
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
    private static CompoundTag encode(HumanWorldData data) { return (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(); }
    private static HumanWorldData decode(CompoundTag tag) { return HumanWorldData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(); }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
    private static void expect(Runnable action) {
        assertions++; try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("Expected safely rejected NPC operation");
    }
}
