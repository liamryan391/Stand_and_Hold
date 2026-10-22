package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.config.EquipmentConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class PrototypeBolt extends ThrowableItemProjectile {
    private int flightAge, lifetime = 1;
    private float damage;
    public PrototypeBolt(EntityType<? extends PrototypeBolt> type, Level level) { super(type, level); }
    public PrototypeBolt(ServerLevel level, LivingEntity owner) {
        this(EquipmentRegistry.PROJECTILE.get(), level);
        setOwner(owner); setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        lifetime = EquipmentConfig.value(EquipmentConfig.LIFETIME);
        damage = EquipmentRules.damage(EquipmentConfig.value(EquipmentConfig.DAMAGE));
    }
    @Override protected Item getDefaultItem() { return EquipmentRegistry.BOLT.get(); }
    @Override protected double getDefaultGravity() { return 0; }
    @Override public boolean canUsePortal(boolean ignorePassenger) { return false; }
    @Override public void tick() {
        if (level() instanceof ServerLevel server) {
            if (EquipmentRules.expired(flightAge, lifetime) || !(getOwner() instanceof LivingEntity owner) || !owner.isAlive()
                    || owner.isRemoved() || owner.level() != server || !loadedFlight(server)) { discard(); return; }
            flightAge++;
        }
        super.tick();
    }
    private boolean loadedFlight(ServerLevel server) {
        var end = position().add(getDeltaMovement());
        if (!Double.isFinite(end.x) || !Double.isFinite(end.y) || !Double.isFinite(end.z) || getDeltaMovement().lengthSqr() > 10
                || end.y < server.getMinY() || end.y >= server.getMaxY() || !server.getWorldBorder().isWithinBounds(BlockPos.containing(end))) { return false; }
        int minX = Math.floorDiv(Mth.floor(Math.min(getX(), end.x) - 0.5), 16), maxX = Math.floorDiv(Mth.floor(Math.max(getX(), end.x) + 0.5), 16);
        int minZ = Math.floorDiv(Mth.floor(Math.min(getZ(), end.z) - 0.5), 16), maxZ = Math.floorDiv(Mth.floor(Math.max(getZ(), end.z) + 0.5), 16);
        if (maxX - minX > 1 || maxZ - minZ > 1) { return false; }
        for (int x = minX; x <= maxX; x++) { for (int z = minZ; z <= maxZ; z++) { if (!server.hasChunk(x, z)) { return false; } } }
        return true;
    }
    @Override protected void onHit(HitResult hit) {
        if (!(level() instanceof ServerLevel server)) { return; }
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity victim
                && getOwner() instanceof LivingEntity owner && (owner instanceof Player || owner instanceof HumanNpc)
                && EquipmentAccess.validTarget(owner, victim) && EquipmentAccess.allowedFor(owner, EquipmentKind.PROTOTYPE)
                && (!(owner instanceof HumanNpc npc) || npc.canAttack(victim))) {
            victim.hurtServer(server, damageSources().thrown(this, owner), damage);
        }
        // Do not invoke block projectile callbacks: this test bolt cannot operate buttons or damage terrain.
        server.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 4, 0.08, 0.08, 0.08, 0.05);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.35F, 1.5F);
        discard();
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("standandhold_flight_age", flightAge); output.putInt("standandhold_lifetime", lifetime); output.putDouble("standandhold_damage", damage);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        flightAge = EquipmentRules.flightAge(input.getIntOr("standandhold_flight_age", 100));
        lifetime = input.getIntOr("standandhold_lifetime", 1);
        damage = EquipmentRules.damage(input.getDoubleOr("standandhold_damage", 0));
    }
}
