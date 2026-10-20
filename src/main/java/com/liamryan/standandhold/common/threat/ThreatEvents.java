package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.sample.SampleItems;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.ThreatConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class ThreatEvents {
    private static final String PROCESSED = "standandhold_threat_processed";
    private ThreatEvents() { }
    public static void onDrops(LivingDropsEvent event) {
        var victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || victim instanceof ServerPlayer || !level.getServer().isSameThread()) { return; }
        if (ThreatConfig.PLAYER_KILLS.get() && !(event.getSource().getEntity() instanceof ServerPlayer)) { return; }
        // getKey on a defaulted registry can return pig for an unregistered type; resource keys cannot.
        var id = BuiltInRegistries.ENTITY_TYPE.getResourceKey(victim.getType()).map(key -> key.identifier().toString()).orElse(null);
        var selected = ThreatConfig.rules().match(id, tag -> victim.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(tag))));
        if (selected.isEmpty() || victim.getPersistentData().getBooleanOr(PROCESSED, false)) { return; }
        victim.getPersistentData().putBoolean(PROCESSED, true);
        var rule = selected.get();
        try {
            HumanWorldData data = HumanWorldData.get(level.getServer());
            data.requireSupportedVersion();
            if (rule.points() > 0) { HumanPointManager.addPoints(level.getServer(), rule.points()); }
            if (level.getGameRules().get(GameRules.MOB_DROPS)) {
                int count = rule.dropCount(level.getRandom().nextDouble());
                if (count > 0) { event.getDrops().add(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), new ItemStack(SampleItems.TISSUE.get(), count))); }
            }
            MissionManager.refresh(data, FoundationResources.get(level.getServer()), HumanProgressionConfig.thresholds());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            LogUtils.getLogger().debug("Threat reward skipped/paused for {}: {}", id, exception.getMessage());
        }
    }
}
