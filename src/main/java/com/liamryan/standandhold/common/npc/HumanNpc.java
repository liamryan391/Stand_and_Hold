package com.liamryan.standandhold.common.npc;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.config.ThreatConfig;
import com.liamryan.standandhold.config.EquipmentConfig;
import com.liamryan.standandhold.common.equipment.EquipmentAccess;
import com.liamryan.standandhold.common.equipment.EquipmentKind;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import com.liamryan.standandhold.common.equipment.PrototypeBolt;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Enemy;
import com.liamryan.standandhold.common.sound.StandAndHoldSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class HumanNpc extends PathfinderMob implements RangedAttackMob, com.geckolib.animatable.GeoEntity {
    // M33 D5 experiment: GeckoLib animation state (client-side use only; the Army Rifleman renderer reads it).
    private final com.geckolib.animatable.instance.AnimatableInstanceCache geoCache = com.geckolib.util.GeckoLibUtil.createInstanceCache(this);
    @Override public void registerControllers(com.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar controllers) {
        RiflemanAnimation.register(controllers);
    }
    @Override public com.geckolib.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }
    private static final EntityDataAccessor<Boolean> LAUNCHER_AIM =
            SynchedEntityData.defineId(HumanNpc.class, EntityDataSerializers.BOOLEAN);
    /** Entity event for any long-gun shot (launcher or service rifle): recoil pose, and the Rifleman's fire clip. */
    private static final byte LAUNCHER_SHOT = 100;
    private static final int SHOT_POSE_TICKS = 5;
    private String commandPost = "";
    private long nextPrototypeShot, nextRifleShot;
    private boolean issuedBlade;
    private BlockPos patrolTarget;
    private long patrolUntil;
    private int localWaypoint;
    private LocalDutyGoal localDuty;
    private float previousAim, aim;
    private int shotPoseTicks;

    public HumanNpc(EntityType<? extends HumanNpc> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        if (!level.isClientSide()) {
            applyStats(true);
            // M35: Riflemen arrive armed. A saved soldier's own equipment replaces this when it loads.
            if (carriesServiceRifle()) { issue(EquipmentRegistry.SERVICE_RIFLE.get().getDefaultInstance()); }
        }
    }
    private boolean carriesServiceRifle() { return tier() == HumanTier.ARMY_RIFLEMAN && NpcConfig.SERVICE_RIFLE.get(); }
    private void issue(net.minecraft.world.item.ItemStack weapon) {
        setItemSlot(EquipmentSlot.MAINHAND, weapon);
        setDropChance(EquipmentSlot.MAINHAND, 0);
    }
    public HumanTier tier() { return NpcRegistry.tier(getType()); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LAUNCHER_AIM, false);
    }
    public boolean launcherAiming() { return isAlive() && entityData.get(LAUNCHER_AIM); }
    public float launcherAim(float partialTick) { return Mth.lerp(partialTick, previousAim, aim); }
    public float launcherKick(float partialTick) {
        return Math.clamp((shotPoseTicks - partialTick) / SHOT_POSE_TICKS, 0, 1);
    }
    @Override public void handleEntityEvent(byte event) {
        if (event == LAUNCHER_SHOT) {
            shotPoseTicks = SHOT_POSE_TICKS;
            if (level().isClientSide() && tier() == HumanTier.ARMY_RIFLEMAN) { triggerAnim(RiflemanAnimation.ACTION, RiflemanAnimation.FIRE); }
        }
        else { super.handleEntityEvent(event); }
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide() && tier().usesLauncherMotion()) {
            previousAim = aim;
            aim = Mth.approach(aim, launcherAiming() ? 1 : 0, 0.25F);
            if (shotPoseTicks > 0) { shotPoseTicks--; }
        }
    }
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData group) {
        if (tier().advanced() && (reason == EntitySpawnReason.DISPENSER || EntitySpawnReason.isSpawner(reason))) {
            setSpawnCancelled(true);
            return group;
        }
        return super.finalizeSpawn(level, difficulty, reason, group);
    }
    @Override public boolean requiresCustomPersistence() { return true; }
    public static AttributeSupplier.Builder attributes(HumanTier tier) {
        return createMobAttributes().add(Attributes.MAX_HEALTH, tier.health).add(Attributes.MOVEMENT_SPEED, tier.speed)
                .add(Attributes.ARMOR, tier.armor).add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.ATTACK_DAMAGE, tier.damage).add(Attributes.ATTACK_KNOCKBACK, 0);
    }
    private void applyStats(boolean fresh) {
        var stats = NpcConfig.stats(tier());
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.health.get());
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.damage.get());
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(stats.speed.get());
        getAttribute(Attributes.ARMOR).setBaseValue(stats.armor.get());
        setHealth(fresh ? getMaxHealth() : Math.min(getHealth(), getMaxHealth()));
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new WeaponGoal(this, 12, HumanNpc::rangedReady));
        goalSelector.addGoal(1, new WeaponGoal(this, NpcConfig.RIFLE_RANGE.get(), HumanNpc::rifleReady));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.9));
        goalSelector.addGoal(3, new PatrolGoal());
        localDuty = new LocalDutyGoal(this);
        goalSelector.addGoal(4, localDuty);
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.7, 120));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new ThreatTarget(this));
    }
    @Override public boolean canAttack(LivingEntity target) {
        if (target == this || target instanceof Player || target instanceof HumanNpc || !target.isAlive()
                || target.isRemoved() || target.level() != level() || !level().hasChunkAt(target.blockPosition())
                || !isWithinHome(target.blockPosition())) { return false; }
        var id = BuiltInRegistries.ENTITY_TYPE.getResourceKey(target.getType()).map(key -> key.identifier().toString()).orElse(null);
        boolean mapped = ThreatConfig.rules().match(id, tag -> target.getType().builtInRegistryHolder()
                .is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(tag)))).isPresent();
        return (NpcConfig.HOSTILE_TARGETS.get() && target instanceof Enemy || mapped) && !isAlliedTo(target) && super.canAttack(target);
    }
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) {
        return target instanceof LivingEntity living && canAttack(living) && super.doHurtTarget(level, target);
    }
    public void assignPost(BuildingPosition position) {
        commandPost = position.key();
        setHomeTo(position.pos(), NpcConfig.HOME_RADIUS.get());
    }
    public java.util.Optional<BuildingPosition> commandPost() { return BuildingPosition.parse(commandPost); }
    public int localWaypoint() { return localWaypoint; }
    public void localWaypoint(int index) { localWaypoint = Math.clamp(index, 0, 1); }
    public LocalDutyGoal localDuty() { return localDuty; }
    public boolean hasResponseOrder() { return patrolTarget != null && !commandPost.isEmpty() && level().getGameTime() < patrolUntil && isWithinHome(patrolTarget); }
    public boolean hasIssuedBlade() { return issuedBlade && getMainHandItem().is(EquipmentRegistry.BLADE.get()); }
    public void assignPatrol(BlockPos target, long until) {
        if (!commandPost.isEmpty() && isWithinHome(target)) { patrolTarget = target.immutable(); patrolUntil = until; }
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("standandhold_command_post", commandPost);
        output.putLong("standandhold_next_prototype_shot", nextPrototypeShot);
        output.putBoolean("standandhold_issued_blade", issuedBlade);
        output.putInt("standandhold_local_waypoint", localWaypoint);
        if (patrolTarget != null) { output.putLong("standandhold_patrol_target", patrolTarget.asLong()); output.putLong("standandhold_patrol_until", patrolUntil); }
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        commandPost = BuildingPosition.parse(input.getStringOr("standandhold_command_post", "")).map(BuildingPosition::key).orElse("");
        if (!level().isClientSide()) { applyStats(false); }
        nextPrototypeShot = Math.clamp(input.getLongOr("standandhold_next_prototype_shot", 0), 0, level().getGameTime() + 200);
        issuedBlade = input.getBooleanOr("standandhold_issued_blade", false);
        localWaypoint = Math.clamp(input.getIntOr("standandhold_local_waypoint", 0), 0, 1);
        BuildingPosition.parse(commandPost).ifPresent(position -> setHomeTo(position.pos(), NpcConfig.HOME_RADIUS.get()));
        patrolUntil = Math.clamp(input.getLongOr("standandhold_patrol_until", 0), 0, level().getGameTime() + 72000);
        patrolTarget = input.getLong("standandhold_patrol_target").map(BlockPos::of).filter(this::isWithinHome).orElse(null);
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (tickCount % 20 != 0) { return; }
        if (!getMainHandItem().is(EquipmentRegistry.BLADE.get())) { issuedBlade = false; }
        boolean bladeAllowed = tier().usesBlade() && EquipmentConfig.value(EquipmentConfig.NPC_BLADE) && EquipmentAccess.allowed(level, EquipmentKind.BLADE);
        if (bladeAllowed && getMainHandItem().isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, EquipmentRegistry.BLADE.get().getDefaultInstance());
            setDropChance(EquipmentSlot.MAINHAND, 0); issuedBlade = true;
        }
        var held = getMainHandItem();
        boolean issuedRifle = held.is(EquipmentRegistry.SERVICE_RIFLE.get());
        if (tier().usesPrototype() && EquipmentConfig.value(EquipmentConfig.NPC_ENABLED) && (held.isEmpty() || issuedRifle)
                && EquipmentAccess.allowed(level, EquipmentKind.PROTOTYPE)) {
            issue(EquipmentRegistry.PROTOTYPE.get().getDefaultInstance()); // the launcher replaces only the issued rifle
        } else if (held.isEmpty() && carriesServiceRifle()) {
            issue(EquipmentRegistry.SERVICE_RIFLE.get().getDefaultInstance());
        } else if (issuedRifle && !carriesServiceRifle()) {
            setItemSlot(EquipmentSlot.MAINHAND, net.minecraft.world.item.ItemStack.EMPTY);
        }
        if (commandPost.isEmpty()) { return; }
        var home = BuildingPosition.parse(commandPost).orElseThrow();
        if (!home.dimension().equals(level.dimension().identifier()) || level.hasChunkAt(home.pos())
                && (!(level.getBlockEntity(home.pos()) instanceof BuildingBlockEntity post) || post.kind() != BuildingKind.COMMAND_POST)) {
            releasePost(level);
            clearHome();
            patrolTarget = null;
        }
    }
    private void releasePost(ServerLevel level) {
        if (commandPost.isEmpty()) { return; }
        try { HumanWorldData.get(level.getServer()).releaseDefender(commandPost, getUUID()); }
        catch (IllegalArgumentException | IllegalStateException exception) { LogUtils.getLogger().debug("Defender reservation retained: {}", exception.getMessage()); }
        commandPost = "";
    }
    @Override public void onRemoval(RemovalReason reason) {
        if (level() instanceof ServerLevel server && (reason.shouldDestroy() || reason == RemovalReason.CHANGED_DIMENSION)) { releasePost(server); }
        super.onRemoval(reason);
    }
    /** The block's own footstep, plus a soft kit rustle on about one step in three (M35: low cloth and webbing, no metal clinks). */
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        super.playStepSound(pos, block);
        if (!isSilent() && getRandom().nextInt(3) == 0) { playSound(StandAndHoldSounds.NPC_GEAR.get(), 0.3F, 0.9F + getRandom().nextFloat() * 0.2F); }
    }
    private boolean rangedReady() {
        return level() instanceof ServerLevel server && tier().usesPrototype() && EquipmentConfig.value(EquipmentConfig.NPC_ENABLED)
                && getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()) && EquipmentAccess.allowed(server, EquipmentKind.PROTOTYPE)
                && getTarget() != null && canAttack(getTarget()) && distanceToSqr(getTarget()) > 9;
    }
    /** M35: the service rifle fires at a hostile in sight between 3 blocks and its configured range. */
    private boolean rifleReady() {
        var target = getTarget();
        int range = NpcConfig.RIFLE_RANGE.get();
        return level() instanceof ServerLevel && carriesServiceRifle() && getMainHandItem().is(EquipmentRegistry.SERVICE_RIFLE.get())
                && target != null && canAttack(target) && distanceToSqr(target) > 9 && distanceToSqr(target) <= range * range;
    }
    /**
     * Hitscan: holds fire if a player or another soldier is in the line of fire, then hits with a chance that falls from
     * 90% up close to 60% at full range. Bounded: one block ray and one entity box along the shot.
     */
    private void fireRifle(ServerLevel server, LivingEntity target) {
        if (!rifleReady() || target != getTarget() || !getSensing().hasLineOfSight(target) || server.getGameTime() < nextRifleShot) { return; }
        var from = getEyePosition();
        var to = target.getBoundingBox().getCenter();
        var path = to.subtract(from);
        boolean friendlyInLine = !server.getEntities(this, getBoundingBox().expandTowards(path).inflate(0.5),
                entity -> entity != target && !entity.isSpectator() && (entity instanceof Player || entity instanceof HumanNpc)
                        && entity.getBoundingBox().inflate(0.3).clip(from, to).isPresent()).isEmpty();
        if (friendlyInLine) { return; }
        nextRifleShot = server.getGameTime() + NpcConfig.RIFLE_INTERVAL.get();
        server.broadcastEntityEvent(this, LAUNCHER_SHOT);
        server.playSound(null, getX(), getEyeY(), getZ(), StandAndHoldSounds.RIFLE_FIRE.get(), SoundSource.NEUTRAL, 1.6F, 0.95F + getRandom().nextFloat() * 0.1F);
        var muzzle = from.add(path.normalize().scale(1.1)).add(0, -0.2, 0);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, 0.04, 0.04, 0.04, 0.01);
        double chance = 0.9 - 0.3 * Math.min(1, Math.sqrt(distanceToSqr(target)) / NpcConfig.RIFLE_RANGE.get());
        if (getRandom().nextDouble() < chance) {
            target.hurtServer(server, damageSources().mobAttack(this), NpcConfig.RIFLE_DAMAGE.get().floatValue());
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, to.x, to.y, to.z, 4, 0.1, 0.1, 0.1, 0.05);
        }
    }
    @Override public void performRangedAttack(LivingEntity target, float power) {
        if (level() instanceof ServerLevel rifleServer && getMainHandItem().is(EquipmentRegistry.SERVICE_RIFLE.get())) { fireRifle(rifleServer, target); return; }
        if (!(level() instanceof ServerLevel server) || !rangedReady() || !canAttack(target) || distanceToSqr(target) > 144
                || !getSensing().hasLineOfSight(target) || server.getGameTime() < nextPrototypeShot) { return; }
        var bolt = new PrototypeBolt(server, this);
        bolt.shoot(target.getX() - bolt.getX(), target.getEyeY() - bolt.getY(), target.getZ() - bolt.getZ(), EquipmentConfig.value(EquipmentConfig.SPEED).floatValue(), 0);
        if (server.addFreshEntity(bolt)) {
            nextPrototypeShot = server.getGameTime() + EquipmentConfig.value(EquipmentConfig.NPC_INTERVAL);
            if (tier().usesLauncherMotion()) { server.broadcastEntityEvent(this, LAUNCHER_SHOT); }
            server.playSound(null, getX(), getY(), getZ(), StandAndHoldSounds.LAUNCHER_FIRE.get(), SoundSource.NEUTRAL, 0.6F, 0.95F + getRandom().nextFloat() * 0.1F);
        }
    }
    /** The vanilla ranged goal for one long gun: closes to range, aims while the target is in sight and fires on its timer. */
    private static final class WeaponGoal extends RangedAttackGoal {
        private final HumanNpc npc;
        private final java.util.function.Predicate<HumanNpc> ready;
        private final double reachSqr;
        WeaponGoal(HumanNpc npc, int reach, java.util.function.Predicate<HumanNpc> ready) {
            super(npc, 1.0, 10, reach); this.npc = npc; this.ready = ready; this.reachSqr = reach * reach;
        }
        @Override public boolean canUse() { return ready.test(npc) && super.canUse(); }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            super.tick();
            if (npc.tier().usesLauncherMotion()) {
                var target = npc.getTarget();
                // Reuse the running goal's cached sight check. Synched data sends only changes.
                double distance = target == null ? 0 : npc.distanceToSqr(target);
                npc.entityData.set(LAUNCHER_AIM, target != null && distance > 9 && distance <= reachSqr
                        && npc.getSensing().hasLineOfSight(target));
            }
        }
        @Override public void stop() {
            super.stop();
            if (npc.tier().usesLauncherMotion()) { npc.entityData.set(LAUNCHER_AIM, false); }
        }
    }
    private static final class ThreatTarget extends NearestAttackableTargetGoal<LivingEntity> {
        private long nextSearch;
        ThreatTarget(HumanNpc npc) { super(npc, LivingEntity.class, 1, true, true, (target, level) -> npc.canAttack(target)); }
        @Override protected double getFollowDistance() { return Math.min(16, super.getFollowDistance()); }
        @Override public boolean canUse() {
            long now = mob.level().getGameTime();
            if (now < nextSearch) { return false; }
            nextSearch = now + NpcConfig.TARGET_INTERVAL.get();
            return (NpcConfig.HOSTILE_TARGETS.get() || !ThreatConfig.rules().rules().isEmpty()) && super.canUse() && canAttack(target, targetConditions);
        }
    }
    private final class PatrolGoal extends Goal {
        private long nextPath;
        PatrolGoal() { setFlags(java.util.EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            return getTarget() == null && patrolTarget != null && !commandPost.isEmpty() && level().getGameTime() < patrolUntil
                    && isWithinHome(patrolTarget) && level().hasChunksAt(patrolTarget.offset(-1, -1, -1), patrolTarget.offset(1, 2, 1))
                    && level().getWorldBorder().isWithinBounds(patrolTarget);
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            if (patrolTarget.distToCenterSqr(position()) <= 4) { getNavigation().stop(); return; }
            if (level().getGameTime() < nextPath) { return; }
            nextPath = level().getGameTime() + 40;
            getNavigation().moveTo(patrolTarget.getX() + 0.5, patrolTarget.getY(), patrolTarget.getZ() + 0.5, 0.8);
        }
        @Override public void stop() { getNavigation().stop(); }
    }
}
