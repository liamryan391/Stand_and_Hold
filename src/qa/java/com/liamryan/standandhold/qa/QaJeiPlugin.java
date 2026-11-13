package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.StandAndHoldConstants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * M35 (D11) QA hook, loaded only by JEI in a {@code -PwithJei} QA run: keeps JEI's runtime so the scripted client can
 * open the recipe view for one of our items and capture the real info page and recipe. {@link QaClient} calls
 * {@link #show} only after checking that JEI is loaded, so this class never loads without JEI.
 */
@JeiPlugin
public final class QaJeiPlugin implements IModPlugin {
    private static volatile IJeiRuntime runtime;
    @Override public Identifier getPluginUid() { return Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "qa_jei"); }
    @Override public void onRuntimeAvailable(IJeiRuntime jeiRuntime) { runtime = jeiRuntime; }
    @Override public void onRuntimeUnavailable() { runtime = null; }
    /** Opens JEI's recipe view on the item, as pressing R over it does. False until JEI's runtime is ready. */
    static boolean show(Item item) {
        var current = runtime;
        if (current == null) { return false; }
        current.getRecipesGui().show(current.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, item.getDefaultInstance()));
        return true;
    }
}
