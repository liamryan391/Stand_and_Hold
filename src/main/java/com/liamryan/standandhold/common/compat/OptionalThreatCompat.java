package com.liamryan.standandhold.common.compat;

import net.neoforged.fml.ModList;

/** The only optional-mod probe: called on config load/reload and admin diagnostics, not entity ticks. */
public final class OptionalThreatCompat {
    private OptionalThreatCompat() { }
    public static String installedVersion(String id) {
        ModList mods = ModList.get();
        if (mods == null || !mods.isLoaded(id)) { return null; }
        return mods.getModContainerById(id).map(container -> container.getModInfo().getVersion().toString()).orElse(null);
    }
}
