package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.equipment.EquipmentAccess;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class ResponseEvents {
    private static final String DEATH = "standandhold_response_death";
    private ResponseEvents() { }
    public static void death(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (event.isCanceled() || !(entity.level() instanceof ServerLevel level) || entity instanceof ServerPlayer
                || entity.getPersistentData().getBooleanOr(DEATH, false)) { return; }
        boolean mapped = !(entity instanceof HumanNpc) && EquipmentAccess.mapped(entity);
        boolean human = entity instanceof HumanNpc;
        if (!mapped && !human && !entity.entityTags().contains(ThreatResponse.RAID_TAG)) { return; }
        try {
            var killer = event.getSource().getEntity(); boolean credited = killer instanceof ServerPlayer || killer instanceof HumanNpc;
            ThreatWorldData.get(level.getServer()).requireWritable(); entity.getPersistentData().putBoolean(DEATH, true);
            if (mapped || human) { ThreatResponse.observe(level, entity.blockPosition(), human ? ResponseConfig.LOSS_SCORE.get() : credited ? ResponseConfig.KILL_SCORE.get() : 0, mapped, mapped && credited, human, false); }
            if (entity.entityTags().contains(ThreatResponse.RAID_TAG)) {
                String post = entity.getPersistentData().getStringOr(ThreatResponse.RAID_POST, "");
                if (BuildingPosition.parse(post).isPresent() && ThreatWorldData.get(level.getServer()).releaseRaider(post, entity.getUUID()) && credited) {
                    MissionManager.observeDefence(HumanWorldData.get(level.getServer()), FoundationResources.get(level.getServer()), HumanProgressionConfig.thresholds())
                            .forEach(message -> level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false));
                }
            }
        } catch (IllegalArgumentException | IllegalStateException exception) { LogUtils.getLogger().debug("Threat death paused: {}", exception.getMessage()); }
    }
    public static void damage(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0 || !(event.getEntity() instanceof HumanNpc human) || !(human.level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker instanceof HumanNpc || attacker instanceof ServerPlayer
                || (!(attacker instanceof Enemy) && !EquipmentAccess.mapped(attacker))) { return; }
        human.commandPost().filter(p -> p.dimension().equals(level.dimension().identifier()) && p.pos().distSqr(human.blockPosition()) <= 1024)
                .ifPresent(post -> {
                    try { ThreatResponse.observe(level, post.pos(), ResponseConfig.ATTACK_SCORE.get(), false, false, false, true); }
                    catch (IllegalArgumentException | IllegalStateException exception) { LogUtils.getLogger().debug("Outpost attack paused: {}", exception.getMessage()); }
                });
    }
    public static void join(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Zombie zombie) || !zombie.entityTags().contains(ThreatResponse.RAID_TAG)) { return; }
        BuildingPosition.parse(zombie.getPersistentData().getStringOr(ThreatResponse.RAID_POST, "")).ifPresent(home -> {
            zombie.setHomeTo(home.pos(), 32);
            zombie.targetSelector.removeAllGoals(goal -> goal instanceof RaidTarget);
            zombie.goalSelector.removeAllGoals(goal -> goal instanceof RaidHome);
            zombie.targetSelector.addGoal(1, new RaidTarget(zombie));
            zombie.goalSelector.addGoal(5, new RaidHome(zombie));
        });
    }
    public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Zombie zombie) || zombie.tickCount % 20 != 0 || !zombie.entityTags().contains(ThreatResponse.RAID_TAG)
                || !(zombie.level() instanceof ServerLevel level)) { return; }
        try {
            var home = BuildingPosition.parse(zombie.getPersistentData().getStringOr(ThreatResponse.RAID_POST, ""));
            long expiry = zombie.getPersistentData().getLongOr(ThreatResponse.RAID_EXPIRY, 0);
            var record = home.isPresent() ? ThreatWorldData.get(level.getServer()).response(home.get().key()) : null;
            if (home.isEmpty() || !home.get().dimension().equals(level.dimension().identifier()) || expiry <= level.getGameTime()
                    || expiry - level.getGameTime() > 72000 || record == null || !record.raiders().contains(zombie.getUUID())) { zombie.discard(); }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            // A damaged ledger must not silently free reservations or keep a tagged attacker alive indefinitely.
            zombie.discard();
        }
    }
    public static void leave(EntityLeaveLevelEvent event) {
        var entity = event.getEntity(); var reason = entity.getRemovalReason();
        if (!(event.getLevel() instanceof ServerLevel level) || !entity.entityTags().contains(ThreatResponse.RAID_TAG) || reason == null
                || !(reason.shouldDestroy() || reason == Entity.RemovalReason.CHANGED_DIMENSION)) { return; }
        String post = entity.getPersistentData().getStringOr(ThreatResponse.RAID_POST, "");
        if (BuildingPosition.parse(post).isEmpty()) { return; }
        try { ThreatWorldData.get(level.getServer()).releaseRaider(post, entity.getUUID()); }
        catch (IllegalArgumentException | IllegalStateException exception) { LogUtils.getLogger().debug("Raider reservation retained: {}", exception.getMessage()); }
    }
    private static final class RaidHome extends MoveTowardsRestrictionGoal { RaidHome(Zombie mob) { super(mob, 0.9); } }
    private static final class RaidTarget extends NearestAttackableTargetGoal<HumanNpc> {
        private long nextSearch;
        RaidTarget(Zombie zombie) { super(zombie, HumanNpc.class, 1, true, true, (target, level) -> target.isAlive() && !zombie.isAlliedTo(target) && zombie.isWithinHome(target.blockPosition())); }
        @Override protected double getFollowDistance() { return Math.min(24, super.getFollowDistance()); }
        @Override public boolean canUse() {
            if (mob.level().getGameTime() < nextSearch) { return false; }
            nextSearch = mob.level().getGameTime() + 20; return super.canUse();
        }
    }
}
