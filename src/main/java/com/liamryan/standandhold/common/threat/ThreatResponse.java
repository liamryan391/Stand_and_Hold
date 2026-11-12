package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Continuation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public final class ThreatResponse {
    public static final String RAID_TAG = "standandhold_raider", RAID_POST = "standandhold_raid_post", RAID_EXPIRY = "standandhold_raid_expiry";
    private ThreatResponse() { }
    public static ThreatRegion region(ServerLevel level, BlockPos pos) { return ThreatRegion.at(level.dimension().identifier(), pos); }
    public static ThreatRecord inspect(ServerLevel level, ThreatRegion key) {
        return ThreatWorldData.get(level.getServer()).region(key, level.getGameTime(), ResponseConfig.DECAY_INTERVAL.get(), ResponseConfig.DECAY_AMOUNT.get());
    }
    public static void observe(ServerLevel level, BlockPos pos, int weight, boolean death, boolean kill, boolean loss, boolean attack) {
        var data = ThreatWorldData.get(level.getServer()); var key = region(level, pos);
        var next = inspect(level, key).observe(level.getGameTime(), weight, death, kill, loss, attack);
        data.putRegion(key, next, ResponseConfig.REGIONS.get());
    }
    public static ResponseRecord state(ServerLevel level, BuildingBlockEntity post) {
        var data = ThreatWorldData.get(level.getServer()); var key = new BuildingPosition(level.dimension().identifier(), post.getBlockPos()).key();
        var response = data.response(key);
        if (response == null) { response = ResponseRecord.initial(level.getGameTime(), ResponseConfig.GRACE.get()); data.putResponse(key, response); }
        return response;
    }
    /** Nine key lookups, not a scan through saved regions or world chunks. */
    public static ThreatRegion patrolRegion(ServerLevel level, BlockPos post) {
        var local = region(level, post); var best = local; long score = inspect(level, local).score();
        for (int dx = -1; dx <= 1; dx++) { for (int dz = -1; dz <= 1; dz++) {
            var candidate = new ThreatRegion(local.dimension(), local.x() + dx, local.z() + dz);
            long found = inspect(level, candidate).score(); if (found > score) { best = candidate; score = found; }
        } }
        return best;
    }
    public static BlockPos patrolPoint(BlockPos post, ThreatRegion region, int radius) {
        var center = region.center(post.getY()); double dx = center.getX() - post.getX(), dz = center.getZ() - post.getZ();
        double length = Math.sqrt(dx * dx + dz * dz), scale = length == 0 ? 0 : Math.min(1, Math.max(1, radius - 2) / length);
        return post.offset((int) Math.round(dx * scale), 0, (int) Math.round(dz * scale));
    }
    public static void tick(ServerLevel level, BuildingBlockEntity post) {
        requirePost(level, post);
        var response = state(level, post); long now = level.getGameTime();
        if (response.hasPending() && now >= response.pendingRaid()) { arrive(level, post); response = state(level, post); }
        if (now < response.naturalAfter()) { return; }
        if (ResponseConfig.REINFORCEMENTS.get() && now >= response.nextReinforcement()
                && inspect(level, patrolRegion(level, post.getBlockPos())).score() >= ResponseConfig.REINFORCEMENT_THRESHOLD.get()) { reinforce(level, post); }
        if (ResponseConfig.RAIDS.get() && !response.hasPending() && response.raidReady(now)
                && inspect(level, region(level, post.getBlockPos())).score() >= ResponseConfig.RAID_THRESHOLD.get()
                && level.players().stream().anyMatch(p -> !p.isSpectator() && p.blockPosition().distSqr(post.getBlockPos()) <= 4096)
                && level.getRandom().nextInt(ResponseConfig.CHANCE.get()) == 0) {
            if (ResponseConfig.WARNING_TICKS.get() > 0) { warnRaid(level, post); } else { raid(level, post); }
        }
    }
    /**
     * M34: a natural raid is announced first. The warning reserves the raid cooldown (so no second wave can be scheduled) and
     * saves the arrival tick and compass octant; the post's next loaded check at or after that tick brings the wave in from
     * that side. No offline catch-up: an unloaded post resolves its warning on the next loaded check.
     */
    public static String warnRaid(ServerLevel level, BuildingBlockEntity post) {
        requirePost(level, post);
        if (level.getDifficulty() == Difficulty.PEACEFUL) { return "No raid in Peaceful difficulty."; }
        var data = ThreatWorldData.get(level.getServer()); var response = state(level, post); long now = level.getGameTime();
        if (response.hasPending()) { return "Raid already incoming in " + Math.max(0, response.pendingRaid() - now) + " ticks."; }
        if (!response.raiders().isEmpty()) { return "Raid already reserved: " + response.raiders().size() + " raiders (unloaded UUIDs still count)."; }
        if (now < response.nextRaid()) { return "Raid cooldown: " + (response.nextRaid() - now) + " ticks."; }
        var home = new BuildingPosition(level.dimension().identifier(), post.getBlockPos());
        int octant = level.getRandom().nextInt(8), lead = Math.max(1, ResponseConfig.WARNING_TICKS.get());
        data.putResponse(home.key(), response.timers(ResponseRecord.after(now, ResponseConfig.RAID_COOLDOWN.get()), response.nextReinforcement())
                .pending(Math.max(1, ResponseRecord.after(now, lead)), octant));
        if (ResponseConfig.WARNINGS.get()) {
            level.playSound(null, post.getBlockPos(), com.liamryan.standandhold.common.sound.StandAndHoldSounds.POST_RADIO.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            for (var player : level.players()) {
                if (!player.isSpectator() && player.blockPosition().distSqr(post.getBlockPos()) <= 4096) {
                    player.sendSystemMessage(Component.translatable("message.standandhold.raid_incoming", post.getBlockPos().toShortString(),
                            Component.translatable("direction.standandhold." + octant), (lead + 19) / 20));
                }
            }
        }
        return "Raid warned at " + home.key() + ": arrives from octant " + octant + " in " + lead + " ticks.";
    }
    /** The warned wave arrives, unless nobody is there to face it or the world became Peaceful: then backoff, no spawn. */
    private static void arrive(ServerLevel level, BuildingBlockEntity post) {
        var data = ThreatWorldData.get(level.getServer()); var response = state(level, post); long now = level.getGameTime();
        var home = new BuildingPosition(level.dimension().identifier(), post.getBlockPos());
        data.putResponse(home.key(), response.clearPending());
        boolean witness = level.players().stream().anyMatch(p -> !p.isSpectator() && p.blockPosition().distSqr(post.getBlockPos()) <= 4096);
        if (level.getDifficulty() == Difficulty.PEACEFUL || !witness) {
            data.putResponse(home.key(), data.response(home.key()).timers(ResponseRecord.after(now, ResponseConfig.BACKOFF.get()), response.nextReinforcement()));
            return;
        }
        spawnWave(level, post, home, now, java.util.OptionalInt.of(response.bearing()));
    }
    private static void requirePost(ServerLevel level, BuildingBlockEntity post) {
        post.requireWritable();
        HumanWorldData.get(level.getServer()).requireSupportedVersion();
        if (post.kind() != BuildingKind.COMMAND_POST || post.isRemoved() || !level.hasChunkAt(post.getBlockPos())
                || !post.operational(HumanWorldData.get(level.getServer()))) { throw new IllegalArgumentException("Post is missing, unloaded or dormant."); }
    }
    public static String reinforce(ServerLevel level, BuildingBlockEntity post) {
        return deployReinforcement(level, post).message();
    }
    public record Deployment(boolean success, String message) { }
    public static Deployment deployReinforcement(ServerLevel level, BuildingBlockEntity post) {
        requirePost(level, post);
        var data = ThreatWorldData.get(level.getServer()); var response = state(level, post); long now = level.getGameTime();
        if (now < response.nextReinforcement()) { return new Deployment(false, "Reinforcement cooldown: " + (response.nextReinforcement() - now) + " ticks."); }
        var home = new BuildingPosition(level.dimension().identifier(), post.getBlockPos());
        data.putResponse(home.key(), response.timers(response.nextRaid(), ResponseRecord.after(now, ResponseConfig.REINFORCEMENT_COOLDOWN.get())));
        var npc = DefenderDeployment.attempt(level, post);
        if (npc == null) {
            data.putResponse(home.key(), data.response(home.key()).timers(response.nextRaid(), ResponseRecord.after(now, ResponseConfig.BACKOFF.get())));
            return new Deployment(false, "No reinforcement: deployment disabled, stage locked, post/area quota full or terrain unsafe. Backoff saved.");
        }
        var target = patrolRegion(level, post.getBlockPos());
        npc.assignPatrol(patrolPoint(post.getBlockPos(), target, NpcConfig.HOME_RADIUS.get()), ResponseRecord.after(now, ResponseConfig.PATROL_DURATION.get()));
        data.putResponse(home.key(), data.response(home.key()).timers(response.nextRaid(), ResponseRecord.after(now, ResponseConfig.REINFORCEMENT_COOLDOWN.get())).counted(false));
        warn(level, post, "message.standandhold.reinforcement", target.key());
        return new Deployment(true, "Reinforcement deployed at " + home.key() + "; bounded patrol toward " + target.key() + ". Shared defender quotas apply.");
    }
    public static String raid(ServerLevel level, BuildingBlockEntity post) {
        requirePost(level, post);
        if (level.getDifficulty() == Difficulty.PEACEFUL) { return "No raid in Peaceful difficulty."; }
        var data = ThreatWorldData.get(level.getServer()); var response = state(level, post); long now = level.getGameTime();
        if (!response.raiders().isEmpty()) { return "Raid already reserved: " + response.raiders().size() + " raiders (unloaded UUIDs still count)."; }
        if (now < response.nextRaid()) { return "Raid cooldown: " + (response.nextRaid() - now) + " ticks."; }
        if (response.hasPending()) { return "Raid already incoming in " + Math.max(0, response.pendingRaid() - now) + " ticks."; }
        var home = new BuildingPosition(level.dimension().identifier(), post.getBlockPos());
        // Reserve cooldown before side effects; exceptions/partial waves cannot become rapid retries.
        data.putResponse(home.key(), response.timers(ResponseRecord.after(now, ResponseConfig.RAID_COOLDOWN.get()), response.nextReinforcement()));
        return spawnWave(level, post, home, now, java.util.OptionalInt.empty());
    }
    /** Shared wave body: cooldown is already reserved by the caller; success restarts it, total failure saves backoff. */
    private static String spawnWave(ServerLevel level, BuildingBlockEntity post, BuildingPosition home, long now, java.util.OptionalInt octant) {
        var data = ThreatWorldData.get(level.getServer()); var response = data.response(home.key());
        int stage = ProgressionRules.stageFor(HumanWorldData.get(level.getServer()).points(), HumanProgressionConfig.thresholds()).level();
        int wanted = ResponseRecord.waveSize(stage, ResponseConfig.RAID_CAP.get()), spawned = 0;
        String blocked = "could not create a zombie";
        for (int i = 0; i < wanted; i++) {
            if (hostiles(level, home.pos()) >= ResponseConfig.RAID_AREA_CAP.get()) {
                blocked = "nearby hostile quota reached (cap " + ResponseConfig.RAID_AREA_CAP.get()
                        + " within 32 blocks), or the bounded mob inspection limit was reached; clear ambient monsters";
                break;
            }
            Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.REINFORCEMENT);
            if (zombie == null) { break; }
            if (!spawnPosition(level, zombie, home.pos(), octant)) {
                blocked = "no safe loaded spawn position found 8-12 blocks from the post; check open dry ground";
                break;
            }
            zombie.addTag(RAID_TAG); zombie.setCanPickUpLoot(false);
            zombie.getPersistentData().putString(RAID_POST, home.key());
            zombie.getPersistentData().putLong(RAID_EXPIRY, ResponseRecord.after(now, ResponseConfig.RAID_LIFETIME.get()));
            data.putResponse(home.key(), data.response(home.key()).member(zombie.getUUID(), true));
            boolean added = false;
            try { added = level.addFreshEntity(zombie); }
            finally { if (!added) { data.releaseRaider(home.key(), zombie.getUUID()); } }
            if (added) { spawned++; }
        }
        if (spawned == 0) {
            data.putResponse(home.key(), data.response(home.key()).timers(ResponseRecord.after(now, ResponseConfig.BACKOFF.get()), response.nextReinforcement()));
            return "No raid: " + blocked + ". Retry after " + ResponseConfig.BACKOFF.get() + " unpaused game ticks; backoff saved.";
        }
        data.putResponse(home.key(), data.response(home.key()).timers(ResponseRecord.after(now, ResponseConfig.RAID_COOLDOWN.get()), response.nextReinforcement()).counted(true));
        observe(level, home.pos(), ResponseConfig.ATTACK_SCORE.get(), false, false, false, true);
        warn(level, post, "message.standandhold.raid", String.valueOf(spawned));
        return "Outpost raid at " + home.key() + ": " + spawned + " zombies, saved cooldown; post/area caps apply.";
    }
    private static int hostiles(ServerLevel level, BlockPos pos) {
        int[] found = {0}, inspected = {0};
        level.getEntities().get(EntityTypeTest.forClass(net.minecraft.world.entity.Mob.class), new AABB(pos).inflate(32), mob -> {
            if (mob.isAlive() && (mob instanceof Enemy || com.liamryan.standandhold.common.equipment.EquipmentAccess.mapped(mob))) { found[0]++; }
            if (++inspected[0] >= 128) { found[0] = ResponseConfig.RAID_AREA_CAP.get(); }
            return found[0] >= ResponseConfig.RAID_AREA_CAP.get() ? Continuation.ABORT : Continuation.CONTINUE;
        });
        return found[0];
    }
    /** Up to 8 tries inside the warned octant (6-12 blocks out), then the original 8 tries anywhere in the 8-12 block ring. */
    private static boolean spawnPosition(ServerLevel level, Zombie zombie, BlockPos home, java.util.OptionalInt octant) {
        int attempts = octant.isPresent() ? 16 : 8;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int dx, dz;
            if (octant.isPresent() && attempt < 8) {
                int[] offset = octantOffset(octant.getAsInt(), level.getRandom().nextInt(61) - 30, 8 + level.getRandom().nextInt(5));
                dx = offset[0]; dz = offset[1];
            } else {
                dx = level.getRandom().nextInt(25) - 12; dz = level.getRandom().nextInt(25) - 12;
                if (Math.max(Math.abs(dx), Math.abs(dz)) < 8) { continue; }
            }
            for (int dy : new int[]{0, 1, -1, 2, -2}) {
                BlockPos pos = home.offset(dx, dy, dz);
                if (!level.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 2, 1)) || pos.getY() <= level.getMinY() || pos.getY() + 2 >= level.getMaxY()
                        || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
                        || !level.getFluidState(pos.below()).isEmpty() || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) { continue; }
                zombie.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if (level.getWorldBorder().isWithinBounds(zombie.getBoundingBox()) && level.noCollision(zombie) && level.isUnobstructed(zombie)) { return true; }
            }
        }
        return false;
    }
    /** Horizontal offset for a compass octant (0 north/-Z, 2 east/+X), a spread in degrees and a radius in blocks. */
    public static int[] octantOffset(int octant, int spreadDegrees, int radius) {
        double angle = Math.toRadians(octant * 45 + spreadDegrees);
        return new int[] {(int) Math.round(Math.sin(angle) * radius), (int) Math.round(-Math.cos(angle) * radius)};
    }
    private static void warn(ServerLevel level, BuildingBlockEntity post, String key, String detail) {
        if (!ResponseConfig.WARNINGS.get()) { return; }
        for (var player : level.players()) {
            if (!player.isSpectator() && player.blockPosition().distSqr(post.getBlockPos()) <= 4096) {
                player.sendSystemMessage(Component.translatable(key, post.getBlockPos().toShortString(), detail));
            }
        }
    }
    /** Operator lookup only: bounded saved registry, loaded tiles only, no chunk request. */
    public static BuildingBlockEntity nearest(ServerLevel level, BlockPos origin) {
        var data = HumanWorldData.get(level.getServer()); BuildingPositions.cleanLoaded(level, data);
        return data.commandPostKeys().stream().map(BuildingPosition::parse).flatMap(java.util.Optional::stream)
                .filter(p -> p.dimension().equals(level.dimension().identifier()) && p.pos().distSqr(origin) <= 4096 && level.hasChunkAt(p.pos()))
                .sorted(Comparator.comparingDouble(p -> p.pos().distSqr(origin)))
                .map(p -> level.getBlockEntity(p.pos())).filter(b -> b instanceof BuildingBlockEntity).map(b -> (BuildingBlockEntity) b)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No loaded Field Command Post within 64 blocks; move closer without force loading it."));
    }
}
