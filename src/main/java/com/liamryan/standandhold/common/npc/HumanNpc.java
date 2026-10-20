package com.liamryan.standandhold.common.npc;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.config.ThreatConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class HumanNpc extends PathfinderMob {
    private String commandPost = "";

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
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 0.9));
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
        return ThreatConfig.rules().match(id, tag -> target.getType().builtInRegistryHolder()
                .is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(tag)))).isPresent() && super.canAttack(target);
    }
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) {
        return target instanceof LivingEntity living && canAttack(living) && super.doHurtTarget(level, target);
    }
    public void assignPost(BuildingPosition position) {
        commandPost = position.key();
        setHomeTo(position.pos(), NpcConfig.HOME_RADIUS.get());
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("standandhold_command_post", commandPost);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        commandPost = BuildingPosition.parse(input.getStringOr("standandhold_command_post", "")).map(BuildingPosition::key).orElse("");
        if (!level().isClientSide()) { applyStats(false); }
        BuildingPosition.parse(commandPost).ifPresent(position -> setHomeTo(position.pos(), NpcConfig.HOME_RADIUS.get()));
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (tickCount % 20 != 0 || commandPost.isEmpty()) { return; }
        var home = BuildingPosition.parse(commandPost).orElseThrow();
        if (!home.dimension().equals(level.dimension().identifier()) || level.hasChunkAt(home.pos())
                && (!(level.getBlockEntity(home.pos()) instanceof BuildingBlockEntity post) || post.kind() != BuildingKind.COMMAND_POST)) {
            releasePost(level);
            clearHome();
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
    private static final class ThreatTarget extends NearestAttackableTargetGoal<LivingEntity> {
        private long nextSearch;
        ThreatTarget(HumanNpc npc) { super(npc, LivingEntity.class, 1, true, true, (target, level) -> npc.canAttack(target)); }
        @Override protected double getFollowDistance() { return Math.min(16, super.getFollowDistance()); }
        @Override public boolean canUse() {
            long now = mob.level().getGameTime();
            if (now < nextSearch) { return false; }
            nextSearch = now + NpcConfig.TARGET_INTERVAL.get();
            return !ThreatConfig.rules().rules().isEmpty() && super.canUse() && canAttack(target, targetConditions);
        }
    }
}
