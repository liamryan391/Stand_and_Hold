package com.liamryan.standandhold.common.event;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.compat.SRPCompat;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.equipment.EquipmentUnlockManager;
import com.liamryan.standandhold.common.item.ModItems;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.threat.ThreatResponseManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().getEntityWorld().isRemote) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack equippedStack = event.getTo();
        if (equippedStack.isEmpty() || !EquipmentUnlockManager.isEquipmentSlot(event.getSlot()) || EquipmentUnlockManager.canUse(player.world, equippedStack)) {
            return;
        }

        ItemStack blockedStack = equippedStack.copy();
        player.setItemStackToSlot(event.getSlot(), ItemStack.EMPTY);
        if (!player.inventory.addItemStackToInventory(blockedStack)) {
            player.dropItem(blockedStack, false);
        }
        EquipmentUnlockManager.sendLockedMessage(player, blockedStack);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world.isRemote) {
            return;
        }

        ItemStack heldStack = player.getHeldItemMainhand();
        if (EquipmentUnlockManager.isLockedForPlayer(player, heldStack)) {
            event.setCanceled(true);
            EquipmentUnlockManager.sendLockedMessage(player, heldStack);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityItemPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (event.isCanceled() || player == null || player.world.isRemote) {
            return;
        }

        ItemStack stack = event.getItem().getItem();
        if (stack.isEmpty() || stack.getItem() != ModItems.PARASITE_TISSUE_SAMPLE) {
            return;
        }

        MissionManager.recordParasiteSampleRecovery(player.world, player, stack.getCount());
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
        if (SRPCompat.isLoaded()) {
            StandAndHold.LOGGER.info("Scape and Run: Parasites detected. Stand and Hold has {} configured SRP parasite mappings.", SRPCompat.getConfiguredMappingCount());
        } else if (SRPCompat.hasConfiguredMappings()) {
            StandAndHold.LOGGER.info("Scape and Run: Parasites not detected. Configured SRP mappings are dormant until SRP is installed.");
        } else {
            StandAndHold.LOGGER.info("Scape and Run: Parasites not detected. Stand and Hold parasite kill rewards will use configured fallback/test entries only.");
        }
    }
}
