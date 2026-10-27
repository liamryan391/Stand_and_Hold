package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;

/** Parser checks without fake server/player state; live permissions/transactions stay server QA. */
public final class FoundationCommandChecks {
    private static int assertions;
    private FoundationCommandChecks() { }
    public static void run() throws Exception {
        var captured = new AtomicReference<String>();
        var dispatcher = new CommandDispatcher<Object>();
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("definition")
                .then(RequiredArgumentBuilder.<Object, Identifier>argument("id", IdentifierArgument.id())
                        .executes(context -> { captured.set(CommandFoundation.definitionId(context)); return 1; })
                        .then(RequiredArgumentBuilder.<Object, Long>argument("amount", LongArgumentType.longArg(1))
                                .executes(context -> { captured.set(CommandFoundation.definitionId(context)); return (int) LongArgumentType.getLong(context, "amount"); }))));
        for (String id : new String[]{"parasite_samples", "standandhold:stockpile_supplies", "test_pack:chapter/goal", "minecraft:analysis"}) {
            check(dispatcher.execute("definition " + id, new Object()) == 1 && captured.get().equals(id), "short/namespaced/path IDs preserve their exact parsed input");
            check(dispatcher.execute("definition " + id + " 2", new Object()) == 2 && captured.get().equals(id), "progress amount is not swallowed into the ID");
        }
        check(FoundationCatalog.commandId("parasite_samples").equals("standandhold:parasite_samples"), "short-ID default remains Stand and Hold, not vanilla minecraft");
        for (String input : new String[]{"definition", "definition bad::id", "definition Bad:id", "definition bad:id trailing"}) {
            captured.set(null);
            try { dispatcher.execute(input, new Object()); throw new AssertionError("Malformed input accepted: " + input); }
            catch (CommandSyntaxException expected) { check(captured.get() == null, "malformed command cannot execute its action"); }
        }
        System.out.println("Foundation command checks passed: " + assertions + " assertions.");
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
