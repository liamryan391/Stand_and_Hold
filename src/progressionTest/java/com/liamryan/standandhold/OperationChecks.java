package com.liamryan.standandhold;

import com.liamryan.standandhold.common.structure.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

final class OperationChecks {
    private static int assertions;
    static void run() {
        var site = new SiteProvenance(UUID.randomUUID(), "minecraft:overworld", new BlockPos(-8, 64, -9), 4, "debug", 0);
        var changes = ExpansionPlan.layout(site).stream().map(cell -> new ExpansionOperation.Change(cell.pos(),
                state(cell.material() == ExpansionPlan.Material.FLOOR ? "minecraft:grass_block" : "minecraft:air"), cell.material())).toList();
        var operation = new ExpansionOperation(UUID.randomUUID(), site.id(), changes, "a".repeat(64), ExpansionOperation.Phase.PREPARED, 0, "");
        operation.validateSite(site);
        check(ExpansionOperation.read(operation.save(), site).equals(operation), "Intent survives structured NBT round trip");
        var before = new HashMap<BlockPos, CompoundTag>(); changes.forEach(cell -> before.put(cell.pos(), cell.before()));
        check(operation.observe(before::get).state() == ExpansionOperation.Observed.ALL_BEFORE, "Prepared/all-before is not replay permission");
        for (int i = 0; i < changes.size(); i++) {
            var cell = changes.get(i); before.put(cell.pos(), cell.after());
            check(operation.observe(before::get).state() == (i == 136 ? ExpansionOperation.Observed.ALL_AFTER : ExpansionOperation.Observed.MIXED), "Every partial prefix classified conservatively");
        }
        for (int i : new int[]{0, 68, 136}) {
            var cell = changes.get(i); before.put(cell.pos(), state("minecraft:chest"));
            check(operation.observe(before::get).equals(new ExpansionOperation.Observation(ExpansionOperation.Observed.CONFLICT, i)), "First conflict retained"); before.put(cell.pos(), cell.after());
        }
        var copy = changes.getFirst().before(); copy.putString("id", "minecraft:lava");
        check(changes.getFirst().before().getStringOr("id", "").equals("minecraft:grass_block"), "Expected states are immutable defensive copies");
        for (String key : List.of("operation_version", "id", "site_uuid", "protected_digest", "phase", "index", "cells")) {
            var tag = operation.save(); tag.remove(key); expect(() -> ExpansionOperation.read(tag, site));
        }
        var future = operation.save(); future.putInt("operation_version", 2); expect(() -> ExpansionOperation.read(future, site));
        var altered = operation.save(); altered.getListOrEmpty("cells").getCompoundOrEmpty(0).putLong("pos", site.anchor().asLong()); expect(() -> ExpansionOperation.read(altered, site));
        var after = operation.save(); after.getListOrEmpty("cells").getCompoundOrEmpty(0).put("after", state("minecraft:diamond_block")); expect(() -> ExpansionOperation.read(after, site));
        var oversized = operation.save(); oversized.getListOrEmpty("cells").add(new CompoundTag()); expect(() -> ExpansionOperation.read(oversized, site));
        var material = operation.save(); material.getListOrEmpty("cells").getCompoundOrEmpty(0).putString("material", "LIGHT"); expect(() -> ExpansionOperation.read(material, site));
        var wrongSite = new SiteProvenance(UUID.randomUUID(), site.dimension(), site.anchor(), 4, "debug", 0); expect(() -> ExpansionOperation.read(operation.save(), wrongSite));
        check(operation.withPhase(ExpansionOperation.Phase.REVIEW, 68, "x".repeat(200)).detail().length() == 160, "Bounded diagnostic");
        var tokens = new ExpansionTokens<String>(); var player = UUID.randomUUID(); var other = UUID.randomUUID();
        UUID token = tokens.issue(player, 10, "intent"); expect(() -> tokens.take(other, token, 11));
        check(tokens.take(player, token, 209).equals("intent"), "Foreign player cannot consume token; tick199 valid"); expect(() -> tokens.take(player, token, 209));
        UUID expired = tokens.issue(player, 0, "expired"); expect(() -> tokens.take(player, expired, 200));
        UUID old = tokens.issue(player, 100, "old"); UUID fresh = tokens.issue(player, 101, "fresh"); expect(() -> tokens.take(player, old, 102));
        check(tokens.take(player, fresh, 102).equals("fresh"), "New preparation revokes previous consent");
        UUID backwards = tokens.issue(player, 100, "backwards"); expect(() -> tokens.take(player, backwards, 99));
        UUID first = tokens.issue(player, 1000, "evicted");
        for (int i = 0; i < 64; i++) { tokens.issue(UUID.randomUUID(), 1000, "value"); check(tokens.size() <= 64, "Bounded server token cache"); }
        expect(() -> tokens.take(player, first, 1001));
        UUID revoked = tokens.issue(player, 1001, "disconnect"); tokens.revoke(player); expect(() -> tokens.take(player, revoked, 1001));
        expect(() -> new ExpansionTokens<String>().take(player, revoked, 1001));
        System.out.println("Operation checks passed: " + assertions + " assertions.");
    }
    private static CompoundTag state(String id) { var tag = new CompoundTag(); tag.putString("id", id); return tag; }
    private static void check(boolean okay, String message) { assertions++; if (!okay) { throw new AssertionError(message); } }
    private static void expect(Runnable action) { assertions++; try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; } throw new AssertionError("Expected safe receipt/token refusal"); }
}
