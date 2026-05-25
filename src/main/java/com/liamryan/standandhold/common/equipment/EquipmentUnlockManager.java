package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.common.item.ModItems;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public final class EquipmentUnlockManager {
    private EquipmentUnlockManager() {
    }

    public static boolean canUse(World world, ItemStack stack) {
        if (world == null || stack.isEmpty()) {
            return true;
        }

        EquipmentRequirement requirement = getRequirement(stack.getItem());
        if (requirement == null) {
            return true;
        }

        if (HumanPointManager.getData(world).getStage().getId() < requirement.requiredStage) {
            return false;
        }

        String researchId = ResearchEntry.normalizeId(requirement.requiredResearchId);
        return researchId.isEmpty() || ResearchManager.isResearchComplete(world, researchId);
    }

    public static void sendLockedMessage(EntityPlayer player, ItemStack stack) {
        if (player == null || player.world.isRemote || stack.isEmpty()) {
            return;
        }

        EquipmentRequirement requirement = getRequirement(stack.getItem());
        if (requirement == null) {
            return;
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "message.standandhold.equipment.locked",
                stack.getDisplayName(),
                requirement.requiredStage,
                formatResearchRequirement(requirement.requiredResearchId)
        );
        message.getStyle().setColor(TextFormatting.RED);
        player.sendMessage(message);
    }

    public static boolean isLockedForPlayer(EntityPlayer player, ItemStack stack) {
        return player != null && !canUse(player.world, stack);
    }

    public static boolean isEquipmentSlot(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD
                || slot == EntityEquipmentSlot.CHEST
                || slot == EntityEquipmentSlot.LEGS
                || slot == EntityEquipmentSlot.FEET;
    }

    private static EquipmentRequirement getRequirement(Item item) {
        if (item == ModItems.ARMY_HELMET || item == ModItems.ARMY_CHESTPLATE || item == ModItems.ARMY_LEGGINGS || item == ModItems.ARMY_BOOTS) {
            return new EquipmentRequirement(StandAndHoldConfig.equipment.armyEquipmentRequiredStage, StandAndHoldConfig.equipment.armyEquipmentRequiredResearch);
        }

        if (item == ModItems.ELITE_HELMET || item == ModItems.ELITE_CHESTPLATE || item == ModItems.ELITE_LEGGINGS || item == ModItems.ELITE_BOOTS) {
            return new EquipmentRequirement(StandAndHoldConfig.equipment.eliteEquipmentRequiredStage, StandAndHoldConfig.equipment.eliteEquipmentRequiredResearch);
        }

        if (item == ModItems.SPECIAL_DIVISION_HELMET || item == ModItems.SPECIAL_DIVISION_CHESTPLATE || item == ModItems.SPECIAL_DIVISION_LEGGINGS || item == ModItems.SPECIAL_DIVISION_BOOTS) {
            return new EquipmentRequirement(StandAndHoldConfig.equipment.specialDivisionEquipmentRequiredStage, StandAndHoldConfig.equipment.specialDivisionEquipmentRequiredResearch);
        }

        if (item == ModItems.ANTI_PARASITE_BLADE) {
            return new EquipmentRequirement(StandAndHoldConfig.equipment.antiParasiteBladeRequiredStage, StandAndHoldConfig.equipment.antiParasiteBladeRequiredResearch);
        }

        if (item == ModItems.PROTOTYPE_RANGED_WEAPON) {
            return new EquipmentRequirement(StandAndHoldConfig.equipment.prototypeRangedWeaponRequiredStage, StandAndHoldConfig.equipment.prototypeRangedWeaponRequiredResearch);
        }

        return null;
    }

    private static String formatResearchRequirement(String researchId) {
        String normalizedResearchId = ResearchEntry.normalizeId(researchId);
        return normalizedResearchId.isEmpty() ? "none" : normalizedResearchId;
    }

    private static final class EquipmentRequirement {
        private final int requiredStage;
        private final String requiredResearchId;

        private EquipmentRequirement(int requiredStage, String requiredResearchId) {
            this.requiredStage = Math.max(0, Math.min(6, requiredStage));
            this.requiredResearchId = requiredResearchId == null ? "" : requiredResearchId;
        }
    }
}
