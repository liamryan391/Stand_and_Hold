package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.EquipmentConfig;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.ThreatConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;

public final class EquipmentAccess {
    private EquipmentAccess() { }
    public static boolean allowed(ServerLevel level, EquipmentKind kind) {
        try {
            var data = HumanWorldData.get(level.getServer());
            data.requireSupportedVersion();
            var catalog = FoundationResources.get(level.getServer());
            var gate = EquipmentConfig.gate(kind);
            return EquipmentRules.unlocked(ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level(),
                    EquipmentConfig.value(gate.stage), EquipmentConfig.value(gate.research), catalog.research()::containsKey, data::isResearchComplete);
        } catch (IllegalArgumentException | IllegalStateException exception) { return false; }
    }
    public static boolean mapped(LivingEntity target) {
        var id = BuiltInRegistries.ENTITY_TYPE.getResourceKey(target.getType()).map(key -> key.identifier().toString()).orElse(null);
        return ThreatConfig.rules().match(id, tag -> target.typeHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(tag)))).isPresent();
    }
    public static boolean allowedFor(LivingEntity actor, EquipmentKind kind) {
        return actor.level() instanceof ServerLevel server && EquipmentRules.actorUnlocked(actor instanceof Player,
                EquipmentConfig.value(EquipmentConfig.PLAYER_GATES), kind == EquipmentKind.BLADE && actor instanceof HumanNpc npc && npc.hasIssuedBlade(), () -> allowed(server, kind));
    }
    public static boolean validTarget(LivingEntity owner, LivingEntity target) {
        return owner.isAlive() && !owner.isRemoved() && !owner.isSpectator() && owner != target && target.isAlive() && !target.isRemoved() && target.level() == owner.level()
                && EquipmentRules.validTarget(true, target instanceof Player, target instanceof HumanNpc, owner.isAlliedTo(target), mapped(target), target instanceof Enemy, owner instanceof Player);
    }
    public static void notice(Player player, EquipmentKind kind) {
        var gate = EquipmentConfig.gate(kind);
        player.sendOverlayMessage(Component.translatable("message.standandhold.equipment_locked", EquipmentConfig.value(gate.stage),
                EquipmentConfig.value(gate.research).isEmpty() ? "-" : EquipmentConfig.value(gate.research)));
    }
}
