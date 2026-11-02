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

/** Shared bounded server application; no on-load replay, global scan, chunk ticket or currency transaction. */
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
        post.requireGrowthWritable();
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
        post.requireGrowthWritable();
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
        var site = OutpostPreview.requirePost(player, post); var operation = latest(post);
        if (operation == null) { return "No expansion operation; applied revision " + site.appliedRevision() + ". Preview never builds."; }
        var observed = operation.observe(pos -> read(player.level(), site, pos));
        boolean protectedOk = operation.protectedDigest().equals(protectedDigest(player.level(), post, site, operation));
        return "Operation " + operation.id() + " / " + operation.phase() + " / " + observed.state()
                + " / protected " + (protectedOk ? "matching" : "changed") + "; recorded index " + operation.index()
                + (observed.firstConflict() >= 0 ? "; conflict " + operation.changes().get(observed.firstConflict()).pos().toShortString() : "")
                + ". " + operation.detail() + " No automatic replay/repair.";
    }
    public static String recover(ServerPlayer player, BuildingBlockEntity post) {
        post.requireGrowthWritable();
        var site = OutpostPreview.requirePost(player, post); var operation = latest(post);
        if (operation == null) { throw new IllegalArgumentException("No operation to inspect/seal."); }
        if (operation.phase() == ExpansionOperation.Phase.APPLIED || site.appliedRevision() != operation.sourceRevision()) { throw new IllegalArgumentException("Already sealed/unsupported; no repair or refill is available."); }
        if (operation.observe(pos -> read(player.level(), site, pos)).state() != ExpansionOperation.Observed.ALL_AFTER
                || !operation.protectedDigest().equals(protectedDigest(player.level(), post, site, operation))) {
            throw new IllegalArgumentException("Recovery refused: delta/protected state is not fully matching all-after. Review/restore backup; no blocks changed.");
        }
        var sealed = operation.withPhase(ExpansionOperation.Phase.APPLIED, operation.changes().size(), "Operator verified all-after; receipt sealed without block writes.");
        if (operation.targetRevision() == 1) { post.storeSiteOperation(sealed); } else { post.storeYardOperation(sealed); }
        return "Sealed operation " + operation.id() + "; metadata only, no blocks/cost/reward/repair.";
    }
    public static ExpansionApply.Access access(ServerPlayer player, BuildingBlockEntity post, ExpansionOperation operation) {
        var site = OutpostPreview.requirePost(player, post); operation.validateSite(site);
        return access(player.level(), post, operation, () -> !player.isRemoved() && player.isAlive() && !player.isSpectator()
                && player.level() == post.getLevel() && player.distanceToSqr(site.anchor().getX() + 0.5, site.anchor().getY() + 0.5, site.anchor().getZ() + 0.5) <= 36);
    }
    public static ExpansionApply.Result applyNow(ServerPlayer player, BuildingBlockEntity post) {
        OutpostPreview.requirePost(player, post);
        return applyNow(player.level(), post, () -> !player.isRemoved() && player.isAlive() && !player.isSpectator()
                && player.level() == post.getLevel() && player.distanceToSqr(post.getBlockPos().getX() + 0.5, post.getBlockPos().getY() + 0.5, post.getBlockPos().getZ() + 0.5) <= 36);
    }
    public static ExpansionApply.Result applyNow(ServerLevel level, BuildingBlockEntity post) { return applyNow(level, post, () -> true); }
    public static ExpansionOperation latest(BuildingBlockEntity post) { return post.yardOperation().orElseGet(() -> post.siteOperation().orElse(null)); }
    public static ExpansionApply.Access access(ServerLevel level, BuildingBlockEntity post, ExpansionOperation operation) {
        return access(level, post, operation, () -> true);
    }
    private static ExpansionApply.Result applyNow(ServerLevel level, BuildingBlockEntity post, java.util.function.BooleanSupplier actor) {
        requireEnabled();
        OutpostPreview.requirePost(level, post); post.requireGrowthWritable();
        if (post.siteOperation().isPresent()) { throw new IllegalArgumentException("Existing operation needs review; no automatic replay or repair."); }
        var inspection = OutpostPreview.inspectDetails(level, post);
        var operation = new ExpansionOperation(UUID.randomUUID(), inspection.plan().site().id(), inspection.changes(),
                protectedDigest(level, post, inspection.plan().site()), ExpansionOperation.Phase.PREPARED, 0, "");
        return ExpansionApply.apply(operation, access(level, post, operation, actor));
    }
    private static ExpansionApply.Access access(ServerLevel level, BuildingBlockEntity post, ExpansionOperation operation, java.util.function.BooleanSupplier actor) {
        var site = OutpostPreview.requirePost(level, post); operation.validateSite(site);
        var state = post.state(); String policy = policy(); int stage = stage(level);
        long[] epoch = {post.siteEpoch()};
        return new ExpansionApply.Access() {
            public boolean valid() {
                return StructureConfig.EXPANSION.get() && level.getServer().isSameThread() && actor.getAsBoolean()
                        && level.hasChunkAt(site.anchor()) && !post.isRemoved() && level.getBlockEntity(site.anchor()) == post
                        && !post.isMainBase() && post.growthSupported() && post.siteEpoch() == epoch[0] && post.state().equals(state)
                        && policy.equals(policy()) && stage == stage(level);
            }
            public CompoundTag read(BlockPos pos) { return OutpostExpansion.read(level, site, pos); }
            public boolean write(ExpansionOperation.Change change) {
                guard(level, site, change.pos());
                var block = switch (change.material()) {
                    case FLOOR -> Blocks.STONE_BRICKS; case WALL -> Blocks.COBBLESTONE; case LIGHT -> Blocks.TORCH;
                    case AIR -> Blocks.AIR; case STEP -> Blocks.STONE_BRICK_STAIRS;
                };
                return level.setBlock(change.pos(), block.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
            public String protectedDigest() { return OutpostExpansion.protectedDigest(level, post, site, operation); }
            public void store(ExpansionOperation current) {
                if (!level.hasChunkAt(site.anchor()) || post.isRemoved() || level.getBlockEntity(site.anchor()) != post) { throw new IllegalStateException("Original writable anchor missing"); }
                if (current.targetRevision() == 1) { post.storeSiteOperation(current); } else { post.storeYardOperation(current); }
                epoch[0] = post.siteEpoch();
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
        return protectedDigest(level, post, site, 4, Set.of());
    }
    public static String protectedDigest(ServerLevel level, BuildingBlockEntity post, SiteProvenance site, ExpansionOperation operation) {
        return operation.targetRevision() == 1 ? protectedDigest(level, post, site)
                : yardDigest(level, post, site, operation.changes());
    }
    public static String yardDigest(ServerLevel level, BuildingBlockEntity post, SiteProvenance site, List<ExpansionOperation.Change> changes) {
        return protectedDigest(level, post, site, 6, changes.stream().map(ExpansionOperation.Change::pos).collect(java.util.stream.Collectors.toSet()));
    }
    private static String protectedDigest(ServerLevel level, BuildingBlockEntity post, SiteProvenance site, int radius, Set<BlockPos> edits) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            try (var out = new DataOutputStream(new DigestOutputStream(OutputStream.nullOutputStream(), digest))) {
                for (var pos : BlockPos.betweenClosed(site.center().offset(-radius, 0, -radius), site.center().offset(radius, 5, radius))) {
                    guard(level, site, pos); if (edits.contains(pos)) { continue; } out.writeLong(pos.asLong());
                    var state = level.getBlockState(pos); NbtIo.write((CompoundTag) canonical(NbtUtils.writeBlockState(state)), out);
                    if (state.hasBlockEntity() && !pos.equals(site.anchor()) && !pos.equals(site.anchor().offset(-2, 0, -1)) && !pos.equals(site.anchor().offset(3, 0, -2))) {
                        throw new IllegalArgumentException("Extra protected block entity inside original footprint; no expansion attempted.");
                    }
                    var tile = state.hasBlockEntity() ? level.getBlockEntity(pos) : null; out.writeBoolean(tile != null);
                    if (tile != null) {
                        var saved = tile.saveWithFullMetadata(level.registryAccess());
                        if (saved.sizeInBytes() > 262144) { throw new IllegalArgumentException("Protected building metadata exceeds 256 KiB safety budget."); }
                        var tag = saved.copy();
                        if (tile == post) {
                            var provenance = tag.getCompoundOrEmpty("site");
                            if (radius == 4) { provenance.remove("operation"); }
                            else { provenance.remove("yard_operation"); tag.remove("growth"); }
                            provenance.putInt("applied_revision", 0);
                        }
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
    private static int stage(ServerPlayer player) { return stage(player.level()); }
    private static int stage(ServerLevel level) {
        var data = HumanWorldData.get(level.getServer()); data.requireSupportedVersion();
        return ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level();
    }
    private static String policy() { return "apron-v1|" + StructureConfig.EXPANSION.get() + "|" + StructureConfig.DIMENSIONS.get() + "|" + StructureConfig.CHECKPOINT_RADIUS.get() + "|" + StructureConfig.SLOPE.get() + "|" + Arrays.toString(HumanProgressionConfig.thresholds()); }
    private static void requireEnabled() { if (!StructureConfig.EXPANSION.get()) { throw new IllegalArgumentException("Expansion is disabled by default; an operator must enable expansionEnabled in server structure config. Preview stays read-only."); } }
}
