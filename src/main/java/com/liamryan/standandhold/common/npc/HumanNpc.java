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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class HumanNpc extends PathfinderMob implements RangedAttackMob {
    private String commandPost = "";
    private long nextPrototypeShot;
    private boolean issuedBlade;
    private BlockPos patrolTarget;
    private long patrolUntil;

    public HumanNpc(EntityType<? extends HumanNpc> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        if (!level.isClientSide()) { applyStats(true); }
    }
    public HumanTier tier() { return getType() == NpcRegistry.ARMY.get() ? HumanTier.ARMY_RIFLEMAN : HumanTier.SURVIVOR_DEFENDER; }
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
        goalSelector.addGoal(1, new PrototypeGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.9));
        goalSelector.addGoal(3, new PatrolGoal());
        goalSelector.addGoal(4, new RandomStrollGoal(this, 0.7, 120));
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
    public boolean hasIssuedBlade() { return issuedBlade && getMainHandItem().is(EquipmentRegistry.BLADE.get()); }
    public void assignPatrol(BlockPos target, long until) {
        if (!commandPost.isEmpty() && isWithinHome(target)) { patrolTarget = target.immutable(); patrolUntil = until; }
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("standandhold_command_post", commandPost);
        output.putLong("standandhold_next_prototype_shot", nextPrototypeShot);
        output.putBoolean("standandhold_issued_blade", issuedBlade);
        if (patrolTarget != null) { output.putLong("standandhold_patrol_target", patrolTarget.asLong()); output.putLong("standandhold_patrol_until", patrolUntil); }
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        commandPost = BuildingPosition.parse(input.getStringOr("standandhold_command_post", "")).map(BuildingPosition::key).orElse("");
        if (!level().isClientSide()) { applyStats(false); }
        nextPrototypeShot = Math.clamp(input.getLongOr("standandhold_next_prototype_shot", 0), 0, level().getGameTime() + 200);
        issuedBlade = input.getBooleanOr("standandhold_issued_blade", false);
        BuildingPosition.parse(commandPost).ifPresent(position -> setHomeTo(position.pos(), NpcConfig.HOME_RADIUS.get()));
        patrolUntil = Math.clamp(input.getLongOr("standandhold_patrol_until", 0), 0, level().getGameTime() + 72000);
        patrolTarget = input.getLong("standandhold_patrol_target").map(BlockPos::of).filter(this::isWithinHome).orElse(null);
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (tickCount % 20 != 0) { return; }
        if (!getMainHandItem().is(EquipmentRegistry.BLADE.get())) { issuedBlade = false; }
        boolean bladeAllowed = tier() == HumanTier.SURVIVOR_DEFENDER && EquipmentConfig.value(EquipmentConfig.NPC_BLADE) && EquipmentAccess.allowed(level, EquipmentKind.BLADE);
        if (bladeAllowed && getMainHandItem().isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, EquipmentRegistry.BLADE.get().getDefaultInstance());
            setDropChance(EquipmentSlot.MAINHAND, 0); issuedBlade = true;
        }
        if (tier() == HumanTier.ARMY_RIFLEMAN && EquipmentConfig.value(EquipmentConfig.NPC_ENABLED) && getMainHandItem().isEmpty()
                && EquipmentAccess.allowed(level, EquipmentKind.PROTOTYPE)) {
            setItemSlot(EquipmentSlot.MAINHAND, EquipmentRegistry.PROTOTYPE.get().getDefaultInstance());
            setDropChance(EquipmentSlot.MAINHAND, 0);
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
    private boolean rangedReady() {
        return level() instanceof ServerLevel server && tier() == HumanTier.ARMY_RIFLEMAN && EquipmentConfig.value(EquipmentConfig.NPC_ENABLED)
                && getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()) && EquipmentAccess.allowed(server, EquipmentKind.PROTOTYPE)
                && getTarget() != null && canAttack(getTarget()) && distanceToSqr(getTarget()) > 9;
    }
    @Override public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel server) || !rangedReady() || !canAttack(target) || distanceToSqr(target) > 144
                || !getSensing().hasLineOfSight(target) || server.getGameTime() < nextPrototypeShot) { return; }
        var bolt = new PrototypeBolt(server, this);
        bolt.shoot(target.getX() - bolt.getX(), target.getEyeY() - bolt.getY(), target.getZ() - bolt.getZ(), EquipmentConfig.value(EquipmentConfig.SPEED).floatValue(), 0);
        if (server.addFreshEntity(bolt)) {
            nextPrototypeShot = server.getGameTime() + EquipmentConfig.value(EquipmentConfig.NPC_INTERVAL);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.4F, 1.1F);
        }
    }
    private static final class PrototypeGoal extends RangedAttackGoal {
        private final HumanNpc npc;
        PrototypeGoal(HumanNpc npc) { super(npc, 1.0, 10, 12); this.npc = npc; }
        @Override public boolean canUse() { return npc.rangedReady() && super.canUse(); }
        @Override public boolean canContinueToUse() { return canUse(); }
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
                    && level().getWorldBorder().isWithinBounds(patrolTarget) && patrolTarget.distToCenterSqr(position()) > 4;
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            if (level().getGameTime() < nextPath) { return; }
            nextPath = level().getGameTime() + 40;
            getNavigation().moveTo(patrolTarget.getX() + 0.5, patrolTarget.getY(), patrolTarget.getZ() + 0.5, 0.8);
        }
        @Override public void stop() { getNavigation().stop(); }
    }
}
