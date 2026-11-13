package com.liamryan.standandhold.common.compat.jei;

import com.liamryan.standandhold.StandAndHoldConstants;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * M35 (D11): optional Just Enough Items support. JEI finds this class by its annotation and is the only thing that ever
 * loads it, so nothing here runs, or needs JEI, when JEI is not installed. Our crafting recipes are ordinary vanilla
 * recipes, which JEI lists on its own; this plugin adds an info page to each item a new player needs explained.
 * Page text lives in the language files under {@code jei.standandhold.info.<item id>}.
 */
@JeiPlugin
public final class StandAndHoldJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "jei");
    private static final List<String> INFO = List.of("field_command_post", "research_lab", "supply_crate", "parasite_tissue_sample",
            "service_rifle", "prototype_launcher", "anti_threat_blade");

    @Override public Identifier getPluginUid() { return UID; }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        for (String id : INFO) {
            var item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, id));
            registration.addIngredientInfo(item, Component.translatable("jei.standandhold.info." + id));
        }
    }
}
