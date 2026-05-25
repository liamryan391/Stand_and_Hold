package com.liamryan.standandhold.common.util;

import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeManager;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeRequirement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public final class CommandPostMessageHelper {
    private CommandPostMessageHelper() {
    }

    public static void sendUpgradeResult(EntityPlayer player, FieldCommandPostUpgradeManager.UpgradeResult result) {
        if (player == null || result == null) {
            return;
        }

        TextComponentTranslation message;
        TextFormatting color = TextFormatting.RED;
        FieldCommandPostUpgradeRequirement requirement = result.getRequirement();
        FieldCommandPostLevel level = result.getLevel();

        switch (result.getStatus()) {
            case COMPLETED:
                color = TextFormatting.YELLOW;
                message = new TextComponentTranslation(
                        "message.standandhold.field_command_post.upgrade.success",
                        level.getLevel(),
                        level.getDisplayName(),
                        requirement.getCompletionPointReward()
                );
                break;
            case ALREADY_MAX_LEVEL:
                message = new TextComponentTranslation(
                        "message.standandhold.field_command_post.upgrade.max",
                        level.getLevel(),
                        level.getDisplayName()
                );
                break;
            case MISSING_CONFIGURATION:
                message = new TextComponentTranslation(
                        "message.standandhold.field_command_post.upgrade.config",
                        level.getLevel(),
                        level.getDisplayName()
                );
                break;
            case MISSING_POINTS:
                message = new TextComponentTranslation(
                        "message.standandhold.field_command_post.upgrade.points",
                        level.getLevel(),
                        requirement.getRequiredHumanPoints(),
                        result.getCurrentHumanPoints()
                );
                break;
            case MISSING_RESEARCH:
                message = new TextComponentTranslation(
                        "message.standandhold.field_command_post.upgrade.research",
                        level.getLevel(),
                        joinStrings(result.getMissingResearchIds())
                );
                break;
            case MISSING_SAMPLES:
                message = new TextComponentTranslation(
                        "message.standandhold.field_command_post.upgrade.samples",
                        level.getLevel(),
                        requirement.getParasiteSampleCost(),
                        result.getAvailableSamples()
                );
                break;
            case MISSING_SUPPLIES:
                message = new TextComponentTranslation(
                        "message.standandhold.field_command_post.upgrade.supplies",
                        level.getLevel(),
                        requirement.getSupplyCost(),
                        result.getAvailableSupplies()
                );
                break;
            default:
                message = new TextComponentTranslation("message.standandhold.field_command_post.upgrade.config", 0, "unknown");
                break;
        }

        message.getStyle().setColor(color);
        player.sendMessage(message);
    }

    private static String joinStrings(Iterable<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
