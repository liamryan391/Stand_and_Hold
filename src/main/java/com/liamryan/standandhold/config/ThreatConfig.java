package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.threat.ThreatRules;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThreatConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue PLAYER_KILLS = B
            .comment("Require the fatal damage's owner to be a server player (melee/projectiles). Environmental deaths and /kill do not count by default.",
                    "False allows all accepted deaths, including environmental/NPC kills; farms are then possible.")
            .define("requirePlayerKill", true);
    public static final ModConfigSpec.ConfigValue<List<?>> MAPPINGS = B
            .comment("OPTIONAL threat entity IDs or #entity_type tags: selector|human points|sample drop chance|sample count.",
                    "minecraft:zombie is a TEST mapping, not verified external-mod compatibility. Empty list disables rewards.",
                    "These selectors also identify human NPC targets, even with zero rewards. Players and other humans are never NPC targets.",
                    "Exact IDs override tags; otherwise first matching tag wins. No stacking. Duplicate selectors: first wins.",
                    "First 256 entries only; invalid/empty entries are skipped with a load/reload warning, never resolved to a fallback entity.",
                    "Use verified lower-case namespace:path IDs. Missing mods/unknown IDs and empty/missing tags simply do not match.",
                    "Points 0..1000000, chance 0..1, samples 0..64. Samples respect mob_drops and death-drop cancellation; no looting multiplier.")
            .define("mappings", ThreatRules.DEFAULTS, value -> value instanceof List<?>);
    public static final ModConfigSpec SPEC = B.build();
    private static volatile ThreatRules rules = ThreatRules.parse(ThreatRules.DEFAULTS);
    private ThreatConfig() { }
    public static ThreatRules rules() { return rules; }
    public static void onConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC || event instanceof ModConfigEvent.Unloading) { return; }
        ThreatRules next = ThreatRules.parse(MAPPINGS.get());
        rules = next;
        for (String issue : next.issues()) { LogUtils.getLogger().warn("Stand and Hold threat config: {}", issue); }
    }
}
