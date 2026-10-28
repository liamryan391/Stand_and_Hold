package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.StructureConfig;
import java.io.*;
import java.security.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Explicit server actions only: no on-load application, global tick, chunk ticket or currency transaction. */
public final class OutpostExpansion {
    private record Ticket(ExpansionOperation operation, String dimension, BlockPos anchor, int level, int stage, String policy, UUID previous) { }
    private static final Map<MinecraftServer, ExpansionTokens<Ticket>> TOKENS = new WeakHashMap<>();
    private OutpostExpansion() { }
    private static ExpansionTokens<Ticket> tokens(MinecraftServer server) { return TOKENS.computeIfAbsent(server, ignored -> new ExpansionTokens<>()); }
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { var cache = TOKENS.get(player.level().getServer()); if (cache != null) { cache.revoke(player.getUUID()); } }
    }
    public static void stopped(ServerStoppedEvent event) { TOKENS.remove(event.getServer()); }
    public static UUID prepare(ServerPlayer player, BuildingBlockEntity post) {
        requireEnabled(); var inspection = OutpostPreview.inspectDetails(player, post);
        var site = inspection.plan().site(); var digest = protectedDigest(player.level(), post, site);
        var previous = post.siteOperation().orElse(null);
        if (previous != null && (previous.phase() != ExpansionOperation.Phase.PREPARED || !previous.changes().equals(inspection.changes()) || !previous.protectedDigest().equals(digest))) {
            throw new IllegalArgumentException("Prepared intent no longer matches all-before/protected state; operator review required.");
        }
        var operation = new ExpansionOperation(UUID.randomUUID(), site.id(), inspection.changes(), digest, ExpansionOperation.Phase.PREPARED, 0, "");
        return tokens(player.level().getServer()).issue(player.getUUID(), player.level().getGameTime(),
                new Ticket(operation, site.dimension(), site.anchor(), post.state().level(), stage(player), policy(), previous == null ? null : previous.id()));
    }
    public static ExpansionApply.Result confirm(ServerPlayer player, BuildingBlockEntity post, UUID token) {
        if (!player.level().getServer().isSameThread()) { throw new IllegalArgumentException("Expansion requires the server thread."); }
        var ticket = tokens(player.level().getServer()).take(player.getUUID(), token, player.level().getGameTime());
        requireEnabled(); var inspection = OutpostPreview.inspectDetails(player, post); var site = inspection.plan().site();
        var previous = post.siteOperation().orElse(null);
        if (!ticket.dimension().equals(site.dimension()) || !ticket.anchor().equals(post.getBlockPos()) || !ticket.operation().siteId().equals(site.id())
                || ticket.level() != post.state().level() || ticket.stage() != stage(player) || !ticket.policy().equals(policy())
                || !Objects.equals(ticket.previous(), previous == null ? null : previous.id())
                || !ticket.operation().changes().equals(inspection.changes())
                || !ticket.operation().protectedDigest().equals(protectedDigest(player.level(), post, site))) {
            throw new IllegalArgumentException("Consent is stale (site/level/stage/policy/terrain/building changed); prepare again.");
        }
        return ExpansionApply.apply(ticket.operation(), access(player, post, ticket.operation()));
    }
    public static String status(ServerPlayer player, BuildingBlockEntity post) {
        var site = OutpostPreview.requirePost(player, post); var operation = post.siteOperation().orElse(null);
        if (operation == null) { return "No expansion operation; applied revision " + site.appliedRevision() + ". Preview never builds."; }
        var observed = operation.observe(pos -> read(player.level(), site, pos));
        boolean protectedOk = operation.protectedDigest().equals(protectedDigest(player.level(), post, site));
        return "Operation " + operation.id() + " / " + operation.phase() + " / " + observed.state()
                + " / protected " + (protectedOk ? "matching" : "changed") + "; recorded index " + operation.index()
                + (observed.firstConflict() >= 0 ? "; conflict " + operation.changes().get(observed.firstConflict()).pos().toShortString() : "")
                + ". " + operation.detail() + " No automatic replay/repair.";
    }
    public static String recover(ServerPlayer player, BuildingBlockEntity post) {
        var site = OutpostPreview.requirePost(player, post); var operation = post.siteOperation().orElseThrow(() -> new IllegalArgumentException("No operation to inspect/seal."));
        if (operation.phase() == ExpansionOperation.Phase.APPLIED || site.appliedRevision() != 0) { throw new IllegalArgumentException("Already sealed/unsupported; no repair or refill is available."); }
        if (operation.observe(pos -> read(player.level(), site, pos)).state() != ExpansionOperation.Observed.ALL_AFTER
                || !operation.protectedDigest().equals(protectedDigest(player.level(), post, site))) {
            throw new IllegalArgumentException("Recovery refused: delta/protected state is not fully matching all-after. Review/restore backup; no blocks changed.");
        }
        post.storeSiteOperation(operation.withPhase(ExpansionOperation.Phase.APPLIED, 137, "Operator verified all-after; receipt sealed without block writes."));
        return "Sealed operation " + operation.id() + "; metadata only, no blocks/cost/reward/repair.";
    }
    public static ExpansionApply.Access access(ServerPlayer player, BuildingBlockEntity post, ExpansionOperation operation) {
        var site = OutpostPreview.requirePost(player, post); operation.validateSite(site);
        var level = player.level(); var state = post.state(); String policy = policy(); int stage = stage(player);
        long[] epoch = {post.siteEpoch()};
        return new ExpansionApply.Access() {
            public boolean valid() {
                return StructureConfig.EXPANSION.get() && level.getServer().isSameThread() && !player.isRemoved() && player.isAlive() && !player.isSpectator() && player.level() == level
                        && level.hasChunkAt(site.anchor()) && !post.isRemoved() && level.getBlockEntity(site.anchor()) == post
                        && !post.isMainBase() && post.siteEpoch() == epoch[0] && post.state().equals(state)
                        && player.distanceToSqr(site.anchor().getX() + 0.5, site.anchor().getY() + 0.5, site.anchor().getZ() + 0.5) <= 36
                        && policy.equals(policy()) && stage == stage(player);
            }
            public CompoundTag read(BlockPos pos) { return OutpostExpansion.read(level, site, pos); }
            public boolean write(ExpansionOperation.Change change) {
                guard(level, site, change.pos());
                var block = switch (change.material()) { case FLOOR -> Blocks.STONE_BRICKS; case WALL -> Blocks.COBBLESTONE; case LIGHT -> Blocks.TORCH; };
                return level.setBlock(change.pos(), block.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
            public String protectedDigest() { return OutpostExpansion.protectedDigest(level, post, site); }
            public void store(ExpansionOperation current) {
                if (!level.hasChunkAt(site.anchor()) || post.isRemoved() || level.getBlockEntity(site.anchor()) != post) { throw new IllegalStateException("Original writable anchor missing"); }
                post.storeSiteOperation(current); epoch[0] = post.siteEpoch();
            }
        };
    }
    private static CompoundTag read(ServerLevel level, SiteProvenance site, BlockPos pos) {
        guard(level, site, pos); return NbtUtils.writeBlockState(level.getBlockState(pos));
    }
    private static void guard(ServerLevel level, SiteProvenance site, BlockPos pos) {
        if (pos.getX() >> 4 != site.anchor().getX() >> 4 || pos.getZ() >> 4 != site.anchor().getZ() >> 4
                || pos.getY() < level.getMinY() || pos.getY() >= level.getMaxY() || !level.hasChunkAt(pos)) {
            throw new IllegalArgumentException("Operation chunk unavailable; no chunk requested.");
        }
        if (!level.getWorldBorder().isWithinBounds(pos)) { throw new IllegalArgumentException("Operation outside world border."); }
    }
    public static String protectedDigest(ServerLevel level, BuildingBlockEntity post, SiteProvenance site) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            try (var out = new DataOutputStream(new DigestOutputStream(OutputStream.nullOutputStream(), digest))) {
                for (var pos : BlockPos.betweenClosed(site.center().offset(-4, 0, -4), site.center().offset(4, 5, 4))) {
                    guard(level, site, pos); out.writeLong(pos.asLong());
                    var state = level.getBlockState(pos); NbtIo.write((CompoundTag) canonical(NbtUtils.writeBlockState(state)), out);
                    if (state.hasBlockEntity() && !pos.equals(site.anchor()) && !pos.equals(site.anchor().offset(-2, 0, -1)) && !pos.equals(site.anchor().offset(3, 0, -2))) {
                        throw new IllegalArgumentException("Extra protected block entity inside original footprint; no expansion attempted.");
                    }
                    var tile = state.hasBlockEntity() ? level.getBlockEntity(pos) : null; out.writeBoolean(tile != null);
                    if (tile != null) {
                        var saved = tile.saveWithFullMetadata(level.registryAccess());
                        if (saved.sizeInBytes() > 262144) { throw new IllegalArgumentException("Protected building metadata exceeds 256 KiB safety budget."); }
                        var tag = saved.copy();
                        if (tile == post) { var provenance = tag.getCompoundOrEmpty("site"); provenance.remove("operation"); provenance.putInt("applied_revision", 0); }
                        NbtIo.write((CompoundTag) canonical(tag), out);
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException failure) { throw new IllegalStateException("Unable to hash protected footprint", failure); }
    }
    private static Tag canonical(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            var result = new CompoundTag(); compound.keySet().stream().sorted().forEach(key -> result.put(key, canonical(compound.get(key)))); return result;
        }
        if (tag instanceof ListTag list) { var result = new ListTag(); for (Tag value : list) { result.add(canonical(value)); } return result; }
        return tag.copy();
    }
    private static int stage(ServerPlayer player) { return ProgressionRules.stageFor(HumanWorldData.get(player.level().getServer()).points(), HumanProgressionConfig.thresholds()).level(); }
    private static String policy() { return "apron-v1|" + StructureConfig.EXPANSION.get() + "|" + StructureConfig.DIMENSIONS.get() + "|" + StructureConfig.CHECKPOINT_RADIUS.get() + "|" + StructureConfig.SLOPE.get() + "|" + Arrays.toString(HumanProgressionConfig.thresholds()); }
    private static void requireEnabled() { if (!StructureConfig.EXPANSION.get()) { throw new IllegalArgumentException("Expansion is disabled by default; an operator must enable expansionEnabled in server structure config. Preview stays read-only."); } }
}
