package com.liamryan.standandhold.qa;

import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(QaMod.ID)
public final class QaMod {
    public static final String ID = "standandhold_qa";
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, ID);
    static { WorldTests.cases().forEach((name, test) -> FUNCTIONS.register(name, () -> test)); }
    public QaMod(IEventBus bus) {
        FUNCTIONS.register(bus); bus.addListener(QaMod::tests);
        if (System.getProperty("standandhold.qa.phase") != null) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(RestartFixture::tick);
        }
    }
    private static void tests(RegisterGameTestsEvent event) {
        for (String name : WorldTests.cases().keySet()) {
            // A distinct environment per case serialises mutations to shared world SavedData.
            var environment = event.registerEnvironment(id(name), new TestEnvironmentDefinition.AllOf(
                    new TestEnvironmentDefinition.SetGameRules(new GameRuleMap.Builder().set(GameRules.SPAWN_MOBS, false)
                            .set(GameRules.ADVANCE_TIME, false).set(GameRules.MOB_GRIEFING, false).build()),
                    new TestEnvironmentDefinition.SetDifficulty(Difficulty.NORMAL)));
            event.registerTest(id(name), new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                    new TestData<>(environment, Level.OVERWORLD, id("arena"), name.equals("lab_finishes_once") ? 1700 : 500,
                            0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }
    static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID, path); }
}
