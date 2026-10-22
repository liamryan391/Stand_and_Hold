package com.liamryan.standandhold;

import com.liamryan.standandhold.common.threat.*;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import com.liamryan.standandhold.common.foundation.*;
import com.liamryan.standandhold.common.mission.*;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.List;

final class ResponseChecks {
    private static int assertions;
    static void run() {
        var dimension = Identifier.parse("minecraft:overworld");
        for (int x : new int[]{-129, -65, -64, -1, 0, 63, 64, 128}) {
            var key = ThreatRegion.at(dimension, new BlockPos(x, 64, x));
            check(key.x() == Math.floorDiv(x, 64), "negative coordinates floor correctly");
            check(ThreatRegion.parse(key.key()).orElseThrow().equals(key), "canonical region round trip");
        }
        for (String bad : new String[]{"", "zombie", "minecraft:overworld@0", "minecraft:overworld@00,0", "minecraft:overworld@999999,0", "bad@@0,0"}) {
            check(ThreatRegion.parse(bad).isEmpty(), "invalid region refused");
        }
        var record = ThreatRecord.empty(0).observe(0, 20, true, true, false, false);
        check(record.score() == 20 && record.kills() == 1 && record.deaths() == 1, "death/kill counters");
        check(record.decay(1199, 1200, 2).equals(record), "decay boundary");
        var decayed = record.decay(3600, 1200, 2);
        check(decayed.score() == 14 && decayed.kills() == 1 && decayed.lastDecay() == 3600, "lazy elapsed decay keeps counters");
        check(decayed.decay(3601, 1200, 2).equals(decayed), "no repeated decay");
        check(decayed.decay(Long.MAX_VALUE, 1200, 1000).score() == 0, "large elapsed decay saturates safely");
        check(decayed.decay(1, 1200, 2).lastDecay() == 1, "rewound clock rebased safely");
        check(record.decay(7200, 1200, 0).score() == 20, "disabled decay");
        var attack = record.observe(20, 4, false, false, false, true);
        check(attack.observe(39, 4, false, false, false, true).equals(attack), "damage windows debounced");
        check(attack.observe(40, 4, false, false, false, true).attacks() == 2, "next damage window");
        check(ThreatRecord.credit(Long.MAX_VALUE - 1, 8) == Long.MAX_VALUE, "counter overflow bounded");
        var key = ThreatRegion.at(dimension, BlockPos.ZERO);
        var data = new ThreatWorldData();
        check(data.putRegion(key, record, 1), "new record"); data.setDirty(false);
        check(data.putRegion(key, record, 1) && !data.isDirty(), "same record does not dirty");
        check(!data.putRegion(new ThreatRegion(dimension, 1, 0), record, 1), "full records skip new without evicting history");
        String post = "minecraft:overworld@0,64,0";
        var id = UUID.randomUUID();
        var initial = ResponseRecord.initial(100, 1200);
        check(initial.raidReady(100) && initial.nextReinforcement() == 100 && initial.naturalAfter() == 1300, "admin first use immediate while natural grace stays saved");
        var response = initial.timers(1300, 1400).member(id, true).counted(true);
        check(!response.raidReady(1300) && !response.member(id, false).raidReady(1299) && response.member(id, false).raidReady(1300), "cooldown boundary and unloaded reservations both enforced");
        for (int stage = 0; stage <= 6; stage++) { for (int cap = 1; cap <= 8; cap++) {
            check(ResponseRecord.waveSize(stage, cap) == Math.min(cap, 1 + stage / 2), "stage wave never exceeds cap");
        } }
        for (int x = -2; x <= 2; x++) { for (int z = -2; z <= 2; z++) { for (int radius : new int[]{8, 16, 24}) {
            var origin = new BlockPos(31, 64, 31); var patrol = ThreatResponse.patrolPoint(origin, new ThreatRegion(dimension, x, z), radius);
            check(patrol.distSqr(origin) <= radius * radius && patrol.getY() == 64, "region patrol remains inside home radius");
        } } }
        expect(() -> ResponseRecord.waveSize(7, 4)); expect(() -> ResponseRecord.waveSize(1, 0));
        data.putResponse(post, response); data.setDirty(false); data.putResponse(post, response);
        check(!data.isDirty(), "unchanged cooldown/roster does not dirty");
        var saved = encode(data); saved.putString("unknown", "keep");
        saved.getCompoundOrEmpty("regions").getCompoundOrEmpty(key.key()).putString("future_detail", "keep");
        var loaded = decode(saved);
        check(loaded.response(post).equals(response), "cooldown and unloaded raider survives reload");
        check(loaded.region(key, 0, 1200, 2).equals(record), "scores/counters survive reload");
        check(loaded.releaseRaider(post, id), "confirmed removal releases reserved raider"); loaded.setDirty(false);
        check(!loaded.releaseRaider(post, id) && !loaded.isDirty(), "repeat death/removal cannot duplicate progress");
        loaded.putRegion(key, decayed, 1);
        check(encode(loaded).getStringOr("unknown", "").equals("keep"), "unknown root retained");
        check(encode(loaded).getCompoundOrEmpty("regions").getCompoundOrEmpty(key.key()).getStringOr("future_detail", "").equals("keep"), "unknown region retained");
        loaded.resetRegion(key);
        check(loaded.response(post).nextRaid() == 1300, "reset threat does not reset cooldown or quota");
        var future = saved.copy(); future.putInt("data_version", 999); assertBlockedRetained(future, post);
        for (String field : new String[]{"regions", "responses"}) {
            var bad = saved.copy(); bad.remove(field); assertBlockedRetained(bad, post);
            bad = saved.copy(); bad.putString(field, "bad"); assertBlockedRetained(bad, post);
        }
        var bad = saved.copy(); bad.getCompoundOrEmpty("regions").getCompoundOrEmpty(key.key()).putDouble("score", Double.NaN); assertBlockedRetained(bad, post);
        bad = saved.copy(); bad.getCompoundOrEmpty("responses").putString(post, "bad"); assertBlockedRetained(bad, post);
        bad = saved.copy(); bad.getCompoundOrEmpty("responses").getCompoundOrEmpty(post).getCompoundOrEmpty("raiders").putBoolean("not-uuid", true); assertBlockedRetained(bad, post);
        var old = new CompoundTag(); old.putString("unknown", "keep"); var migrated = decode(old);
        check(migrated.isDirty() && encode(migrated).getStringOr("unknown", "").equals("keep"), "empty version zero migrates without deleting unknown fields");
        expect(() -> new ResponseRecord(0, 0, 0, 0, Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()), 0));
        var oldResponse = response.save(); oldResponse.remove("natural_after");
        check(ResponseRecord.load(oldResponse).nextRaid() == 1300 && ResponseRecord.load(oldResponse).raiders().contains(id), "older M10 timers/quotas retained without erasing cooldowns");
        oldResponse.putDouble("natural_after", 1.5); expect(() -> ResponseRecord.load(oldResponse));
        expect(() -> data.putResponse("bad", response));
        var mission = new Mission("standandhold:defend_test", "Defence", Mission.Objective.OUTPOST_DEFENCE, 2, "", List.of(), new FoundationReward(5, 3));
        var catalog = new FoundationCatalog(List.of(), List.of(mission)); var progression = new HumanWorldData();
        var thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        check(MissionManager.observeDefence(progression, catalog, thresholds).isEmpty(), "inactive defence mission not advanced");
        MissionManager.start(progression, catalog, thresholds, mission.id());
        check(MissionManager.observeDefence(progression, catalog, thresholds).size() == 1 && progression.mission(mission.id()).orElseThrow().progress() == 1, "real defence hook advances active objective");
        MissionManager.observeDefence(progression, catalog, thresholds);
        check(progression.points() == 5 && progression.supplies() == 3, "defence reward once");
        check(MissionManager.observeDefence(progression, catalog, thresholds).isEmpty() && progression.points() == 5, "repeat defence cannot regrant reward");
        System.out.println("Threat response checks passed: " + assertions + " assertions.");
    }
    private static void assertBlockedRetained(CompoundTag tag, String post) {
        var data = decode(tag); expect(() -> data.response(post)); data.setDirty(); check(encode(data).equals(tag), "malformed/future payload retained exactly");
    }
    private static CompoundTag encode(ThreatWorldData data) { return (CompoundTag) ThreatWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(); }
    private static ThreatWorldData decode(CompoundTag tag) { return ThreatWorldData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(); }
    private static void check(boolean result, String message) { assertions++; if (!result) { throw new AssertionError(message); } }
    private static void expect(Runnable action) { assertions++; try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; } throw new AssertionError("Expected safe rejection"); }
}
