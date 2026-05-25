package com.liamryan.standandhold.common.event;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.item.ModItems;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.threat.ThreatResponseManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class HumanProgressionEventHandler {
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.getEntityWorld();
        if (world.isRemote) {
            return;
        }

        ResourceLocation entityId = EntityList.getKey(entity);
        if (entity instanceof EntityHumanNpc) {
            ThreatResponseManager.recordHumanLoss(world, (EntityHumanNpc) entity);
            return;
        }

        if (StandAndHoldConfig.isConfiguredParasiteEntity(entityId)) {
            ThreatResponseManager.recordParasiteKill(world, entity.getPosition());
        }

        tryDropParasiteSample(entity, entityId);

        int reward = StandAndHoldConfig.getParasiteKillReward(entityId);
        if (reward <= 0) {
            return;
        }

        HumanPointManager.addPoints(world, reward, "configured entity death: " + entityId);

        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Awarded {} human points for configured entity death: {}.", reward, entityId);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        World world = victim.getEntityWorld();
        if (world.isRemote || !(victim instanceof EntityHumanNpc)) {
            return;
        }

        Entity sourceEntity = event.getSource().getTrueSource();
        ResourceLocation sourceId = sourceEntity == null ? null : EntityList.getKey(sourceEntity);
        if (StandAndHoldConfig.isConfiguredParasiteEntity(sourceId)) {
            ThreatResponseManager.recordOutpostAttack(world, (EntityHumanNpc) victim);
        }
    }

    private void tryDropParasiteSample(EntityLivingBase entity, ResourceLocation entityId) {
        double dropChance = StandAndHoldConfig.getParasiteSampleDropChance(entityId);
        if (dropChance <= 0.0D || entity.getEntityWorld().rand.nextDouble() >= dropChance) {
            return;
        }

        entity.entityDropItem(new ItemStack(ModItems.PARASITE_TISSUE_SAMPLE), 0.0F);

        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Dropped Parasite Tissue Sample from configured entity death: {}.", entityId);
        }
    }

    public static void logOptionalCompatibilityState() {
        if (StandAndHoldConfig.isScapeAndRunParasitesLoaded()) {
            StandAndHold.LOGGER.info("Scape and Run: Parasites detected. Stand and Hold will use configured SRP entity reward entries when present.");
        } else {
            StandAndHold.LOGGER.info("Scape and Run: Parasites not detected. Stand and Hold parasite kill rewards will use configured fallback/test entries only.");
        }
    }
}
