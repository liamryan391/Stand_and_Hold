package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.threat.ThreatRules;
import com.liamryan.standandhold.common.compat.OptionalThreatCompat;
import com.liamryan.standandhold.common.compat.ThreatPresets;
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
                    "minecraft:zombie is a TEST mapping. Manual entries precede presets; empty mappings AND presets disable rewards.",
                    "These selectors also identify human NPC targets, even with zero rewards. Players and other humans are never NPC targets.",
                    "Exact IDs override tags; otherwise first matching tag wins. No stacking. Duplicate selectors: first wins.",
                    "First 256 entries only; invalid/empty entries are skipped with a load/reload warning, never resolved to a fallback entity.",
                    "Use verified lower-case namespace:path IDs. Missing mods/unknown IDs and empty/missing tags simply do not match.",
                    "Points 0..1000000, chance 0..1, samples 0..64. Samples respect mob_drops and death-drop cancellation; no looting multiplier.")
            .define("mappings", ThreatRules.DEFAULTS, value -> value instanceof List<?>);
    public static final ModConfigSpec.ConfigValue<List<?>> PRESETS = B
            .comment("Opt-in mapping presets: vanilla_undead or contagion_2_1_1. Empty by default; never installs a mod.",
                    "WARNING: Contagion 2.1.1 fails bootstrap on our pinned loader (missing ModConfig.Type.SERVER); its candidate preset is for isolated reproduction only, NOT supported play.",
                    "contagion_2_1_1 requires loaded Contagion 2.1.1-NeoForge-mc26.3; absent/other versions skip safely.",
                    "Contagion preset uses the verified vanilla zombie test tag, not invented parasite IDs or infected-player targeting.",
                    "First 16 selections only; unknown/duplicate entries are skipped. Manual entries retain priority; combined limit 256.",
                    "Custom datapacks may extend tags, but added members are not verified by this preset. Restart after mod-list changes.")
            .define("presets", List.of(), value -> value instanceof List<?>);
    public static final ModConfigSpec SPEC = B.build();
    private static volatile ThreatPresets.Resolved resolved = ThreatPresets.resolve(ThreatRules.DEFAULTS, List.of(), id -> null);
    private ThreatConfig() { }
    public static ThreatRules rules() { return resolved.rules(); }
    public static ThreatPresets.Resolved resolved() { return resolved; }
    public static void onConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC || event instanceof ModConfigEvent.Unloading) { return; }
        var next = ThreatPresets.resolve(MAPPINGS.get(), PRESETS.get(), OptionalThreatCompat::installedVersion);
        resolved = next;
        LogUtils.getLogger().info("Stand and Hold threat config: presets {}, {} resolved rules.", next.active(), next.rules().rules().size());
        for (String issue : next.rules().issues()) { LogUtils.getLogger().warn("Stand and Hold threat config: {}", issue); }
    }
}
