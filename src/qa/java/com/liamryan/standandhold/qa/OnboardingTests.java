package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.npc.DefenderDeployment;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import com.liamryan.standandhold.common.sample.SampleEvents;
import com.liamryan.standandhold.common.sample.SampleItems;
import com.liamryan.standandhold.common.structure.StructureKind;
import com.liamryan.standandhold.common.structure.StructurePlacement;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.BuildingConfig;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.config.ThreatConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** M32 earned onboarding: real recipes, item placement, hooks and server menus. Synthetic players prove authority, not network play. */
final class OnboardingTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    private static final String RECOVER = "standandhold:recover_parasite_samples";
    private OnboardingTests() { }

    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("onboarding_recipes_craft", OnboardingTests::recipes);
        tests.put("onboarding_crafted_post_registers_once", OnboardingTests::craftedPost);
        tests.put("onboarding_post_spacing", OnboardingTests::spacing);
        tests.put("onboarding_precarried_samples_once", OnboardingTests::preCarried);
        tests.put("onboarding_shared_order_two_players", OnboardingTests::shared);
        tests.put("onboarding_ordinary_undead_credit", OnboardingTests::undead);
        tests.put("onboarding_orders_menu_states", OnboardingTests::menu);
        tests.put("onboarding_spacing_same_tick", OnboardingTests::sameTick);
        tests.put("onboarding_stale_post_records", OnboardingTests::staleRecords);
        tests.put("onboarding_cancelled_placement_no_reward", OnboardingTests::cancelledPlacement);
        return tests;
    }

    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static FoundationCatalog catalog(GameTestHelper h) { return FoundationResources.get(h.getLevel().getServer()); }
    private static BuildingBlockEntity post(GameTestHelper h, BlockPos relative) {
        h.setBlock(relative, BuildingRegistry.COMMAND_POST.get());
        return h.getBlockEntity(relative, BuildingBlockEntity.class);
    }
    private static QaPlayer player(GameTestHelper h, BlockPos relative) {
        var player = QaPlayer.join(h.getLevel());
        BlockPos pos = h.absolutePos(relative);
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        return player;
    }
    private static void carry(QaPlayer player, int samples) { player.getInventory().setItem(9, new ItemStack(SampleItems.TISSUE.get(), samples)); }
    /** The real item path: BlockItem.useOn -> place -> getStateForPlacement, clicking the top of the arena ground. */
    private static boolean placePost(GameTestHelper h, QaPlayer player, BlockPos relative) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BuildingRegistry.COMMAND_POST.get().asItem(), 4));
        BlockPos ground = h.absolutePos(relative.below());
        var hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        return h.getLevel().getBlockState(h.absolutePos(relative)).is(BuildingRegistry.COMMAND_POST.get());
    }

    private static void recipes(GameTestHelper h) {
        QaWorld.begin(h);
        var level = h.getLevel(); var recipes = level.getServer().getRecipeManager();
        ItemStack p = new ItemStack(Items.OAK_PLANKS), i = new ItemStack(Items.IRON_INGOT), c = new ItemStack(Items.COPPER_INGOT);
        ItemStack g = new ItemStack(Items.GLASS), s = new ItemStack(Items.COBBLESTONE), e = ItemStack.EMPTY;
        var postInput = CraftingInput.of(3, 3, List.of(p, i, p, i, c, i, p, i, p));
        var post = recipes.getRecipeFor(RecipeType.CRAFTING, postInput, level);
        h.assertTrue(post.isPresent() && post.get().id().identifier().equals(Identifier.fromNamespaceAndPath("standandhold", "field_command_post"))
                && post.get().value().assemble(postInput).is(BuildingRegistry.COMMAND_POST.get().asItem()), "4 iron, 1 copper, 4 planks craft a Field Command Post");
        var labInput = CraftingInput.of(3, 3, List.of(g, g, g, i, e, i, s, s, s));
        var lab = recipes.getRecipeFor(RecipeType.CRAFTING, labInput, level);
        h.assertTrue(lab.isPresent() && lab.get().value().assemble(labInput).is(BuildingRegistry.LAB.get().asItem()), "3 glass, 2 iron, 3 cobblestone craft a Research Lab");
        var noCopper = CraftingInput.of(3, 3, List.of(p, i, p, i, i, i, p, i, p));
        h.assertTrue(recipes.getRecipeFor(RecipeType.CRAFTING, noCopper, level).filter(r -> r.value().assemble(noCopper).is(BuildingRegistry.COMMAND_POST.get().asItem())).isEmpty(),
                "the post needs its copper");
        var crate = CraftingInput.of(3, 3, List.of(p, p, p, p, i, p, p, p, p));
        h.assertTrue(recipes.getRecipeFor(RecipeType.CRAFTING, crate, level).filter(r -> r.value().assemble(crate).is(BuildingRegistry.CRATE.get().asItem())).isEmpty(),
                "the Supply Crate stays recipe-free");
        var unlock = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("standandhold", "recipes/buildings"));
        h.assertTrue(unlock != null, "iron unlocks both building recipes in the recipe book");
        h.succeed();
    }

    private static void craftedPost(GameTestHelper h) {
        QaWorld.begin(h);
        var player = player(h, HOME.offset(3, 0, 0));
        h.assertTrue(placePost(h, player, HOME), "Survival item placement creates a post");
        h.assertTrue(player.getMainHandItem().getCount() == 3, "exactly one post item consumed");
        // Block entities placed during a tick load (and register) on the following tick.
        h.runAfterDelay(2, () -> {
            var post = h.getBlockEntity(HOME, BuildingBlockEntity.class);
            var key = new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()).key();
            h.assertTrue(data(h).commandPostKeys().equals(List.of(key)), "crafted post registers exactly once");
            try { post.site(); h.fail("a crafted post must not receive checkpoint provenance or growth rights"); } catch (IllegalArgumentException expected) { }
            h.assertTrue(post.state().equals(BuildingState.EMPTY), "no crate stock, refill or starter supplies");
            post.onLoad();
            h.assertTrue(data(h).commandPostKeys().size() == 1, "reload does not register it twice");
            var npc = DefenderDeployment.attempt(h.getLevel(), post);
            h.assertTrue(npc != null && npc.getType() == NpcRegistry.SURVIVOR.get(), "a crafted post deploys a Survivor at Stage 0");
            h.succeed();
        });
    }

    private static void spacing(GameTestHelper h) {
        QaWorld.begin(h);
        int saved = BuildingConfig.POST_SPACING.get();
        post(h, HOME);
        var player = player(h, HOME.offset(-20, 0, 24));
        BlockPos target = HOME.offset(-20, 0, 21);
        h.runAfterDelay(2, () -> {
            try {
                BuildingConfig.POST_SPACING.set(96);
                h.assertTrue(data(h).commandPostKeys().size() == 1, "the first post is registered before the check");
                h.assertFalse(placePost(h, player, target), "a post 29 blocks from another is refused");
                h.assertTrue(player.getMainHandItem().getCount() == 4 && player.messages("message.standandhold.post_too_close") == 1,
                        "refusal keeps the item and explains the distance once");
                int chunkX = h.absolutePos(HOME.offset(-16, 0, 0)).getX() >> 4, chunkZ = h.absolutePos(HOME).getZ() >> 4;
                var generated = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, chunkX, chunkZ);
                h.assertTrue(generated.success() && data(h).commandPostKeys().size() == 2, "generated checkpoints are not limited by player spacing: " + generated.message());
                BuildingConfig.POST_SPACING.set(8);
                h.assertTrue(placePost(h, player, target), "a configured lower spacing allows the post");
                BuildingConfig.POST_SPACING.set(0);
                h.assertTrue(placePost(h, player, HOME.offset(4, 0, 4)), "spacing 0 disables the rule");
            } finally { BuildingConfig.POST_SPACING.set(saved); }
            h.runAfterDelay(2, () -> {
                h.assertTrue(data(h).commandPostKeys().size() == 4, "allowed posts register normally");
                h.succeed();
            });
        });
    }

    /** Two item placements in one server tick: the first registers at placement, so the second is refused. */
    private static void sameTick(GameTestHelper h) {
        QaWorld.begin(h);
        int saved = BuildingConfig.POST_SPACING.get();
        try {
            BuildingConfig.POST_SPACING.set(96);
            var player = player(h, HOME.offset(-20, 0, 24));
            h.assertTrue(placePost(h, player, HOME.offset(-20, 0, 21)), "the first post is placed");
            h.assertTrue(data(h).commandPostKeys().size() == 1, "it registers in the same call, before its block entity loads");
            h.assertFalse(placePost(h, player, HOME.offset(0, 0, 21)), "a second post 20 blocks away in the same tick is refused");
            h.assertTrue(data(h).commandPostKeys().size() == 1 && player.messages("message.standandhold.post_too_close") == 1, "only one post registered");
        } finally { BuildingConfig.POST_SPACING.set(saved); }
        h.runAfterDelay(2, () -> {
            h.assertTrue(data(h).commandPostKeys().size() == 1, "loading the placed post does not register it twice");
            h.succeed();
        });
    }

    /** Loaded records with no post neither block placement nor grant first-time post credit; unloaded records are kept. */
    private static void staleRecords(GameTestHelper h) {
        QaWorld.begin(h);
        var dimension = h.getLevel().dimension().identifier();
        var stale = new BuildingPosition(dimension, h.absolutePos(HOME.offset(-10, 0, 21)));
        var far = new BuildingPosition(dimension, h.absolutePos(HOME).offset(100_000, 0, 0));
        var player = player(h, HOME.offset(-20, 0, 24));
        h.runAfterDelay(2, () -> {
            data(h).registerCommandPost(stale); data(h).registerCommandPost(far);
            carry(player, 3); SampleEvents.observe(player);
            h.assertTrue(data(h).mission(RECOVER).map(s -> s.completed()).orElse(false)
                    && data(h).mission("standandhold:establish_field_command").map(s -> !s.completed()).orElse(false),
                    "stale or unloaded post records grant no Establish credit");
            h.assertTrue(!data(h).commandPostKeys().contains(stale.key()) && data(h).commandPostKeys().contains(far.key()),
                    "the loaded stale record is cleaned; the unloaded record is kept without loading its chunk");
            data(h).registerCommandPost(stale);
            int saved = BuildingConfig.POST_SPACING.get();
            try {
                BuildingConfig.POST_SPACING.set(96);
                h.assertTrue(placePost(h, player, HOME.offset(-20, 0, 21)), "a stale record 10 blocks away does not block a real post");
            } finally { BuildingConfig.POST_SPACING.set(saved); }
            h.assertTrue(!data(h).commandPostKeys().contains(stale.key()) && data(h).commandPostKeys().contains(far.key()),
                    "placement cleans the stale record it met and keeps the unloaded one");
            h.assertTrue(data(h).mission("standandhold:establish_field_command").map(s -> s.completed()).orElse(false)
                    && player.count("Order complete: Establish Field Command") == 1, "the real placed post earns Establish once");
            h.succeed();
        });
    }

    /** Test-only stand-in for a claim/protection mod: refuses Command Post placement while set. Never set outside a test. */
    static volatile boolean cancelPostPlacement;
    static void cancelGuard(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (cancelPostPlacement && event.getPlacedBlock().is(BuildingRegistry.COMMAND_POST.get())) { event.setCanceled(true); }
    }

    /** A placement refused by the built-in NeoForge place event must register, credit and announce nothing. */
    private static void cancelledPlacement(GameTestHelper h) {
        QaWorld.begin(h);
        var player = player(h, HOME.offset(3, 0, 0));
        h.runAfterDelay(2, () -> {
            carry(player, 3); SampleEvents.observe(player);
            h.assertTrue(data(h).mission(RECOVER).map(s -> s.completed()).orElse(false) && data(h).points() == 25 && data(h).supplies() == 10,
                    "sample recovery complete; Establish is eligible");
            try {
                cancelPostPlacement = true;
                h.assertFalse(placePost(h, player, HOME), "a cancelled placement leaves no post");
            } finally { cancelPostPlacement = false; }
            h.assertTrue(player.getMainHandItem().getCount() == 4, "the item is retained");
            h.assertTrue(data(h).commandPostKeys().isEmpty(), "no registration");
            h.assertTrue(data(h).mission("standandhold:establish_field_command").map(s -> !s.completed()).orElse(false)
                    && data(h).points() == 25 && data(h).supplies() == 10, "no completion receipt and no reward");
            h.assertTrue(player.count("Order complete: Establish Field Command") == 0, "no completion notice");
            h.assertTrue(placePost(h, player, HOME), "the same placement is accepted when nothing cancels it");
            h.assertTrue(data(h).commandPostKeys().size() == 1 && player.count("Order complete: Establish Field Command") == 1
                    && data(h).points() == 75 && data(h).supplies() == 20, "it registers and rewards once (Establish, then Stockpile)");
            h.runAfterDelay(2, () -> {
                h.assertTrue(data(h).commandPostKeys().size() == 1 && data(h).points() == 75, "loading the post adds nothing");
                h.succeed();
            });
        });
    }

    private static void preCarried(GameTestHelper h) {
        QaWorld.begin(h);
        var player = player(h, HOME.offset(2, 0, 0));
        post(h, HOME);
        h.runAfterDelay(2, () -> {
            h.assertTrue(data(h).mission(RECOVER).map(state -> state.progress() == 0).orElse(false), "the first order started on its own");
            carry(player, 3);
            h.useBlock(HOME, player);
            h.assertTrue(data(h).points() == 75 && data(h).supplies() == 20, "samples carried before the order count once; post and stockpile resolve");
            h.assertTrue(player.count("Order complete: Recover Parasite Samples") == 1 && player.count("New order: Complete First Research") == 1,
                    "completion and the next order are announced once");
            h.useBlock(HOME, player); SampleEvents.observe(player);
            carry(player, 0); carry(player, 3); SampleEvents.observe(player);
            h.assertTrue(data(h).points() == 75 && data(h).supplies() == 20 && player.count("Order complete: Recover Parasite Samples") == 1,
                    "re-opening, dropping and re-picking add nothing");
            h.succeed();
        });
    }

    private static void shared(GameTestHelper h) {
        QaWorld.begin(h);
        var a = player(h, HOME.offset(2, 0, 0)); var b = player(h, HOME.offset(-2, 0, 0));
        post(h, HOME);
        h.runAfterDelay(2, () -> {
            carry(a, 3); carry(b, 3);
            SampleEvents.observe(a); SampleEvents.observe(b); SampleEvents.observe(a);
            h.assertTrue(data(h).points() == 75 && data(h).supplies() == 20, "two players finishing the same shared order earn one reward");
            for (var player : List.of(a, b)) {
                h.assertTrue(player.count("Order complete: Recover Parasite Samples") == 1 && player.count("Order complete: Stockpile Supplies") == 1,
                        "every player sees each world-shared completion exactly once");
            }
            h.succeed();
        });
    }

    private static void undead(GameTestHelper h) {
        QaWorld.begin(h);
        List<?> saved = ThreatConfig.PRESETS.get();
        try {
            ThreatConfig.PRESETS.set(ThreatConfig.DEFAULT_PRESETS); ThreatConfig.refresh();
            var player = player(h, HOME);
            long points = 0;
            for (EntityType<? extends Mob> type : List.<EntityType<? extends Mob>>of(EntityTypes.HUSK, EntityTypes.DROWNED, EntityTypes.ZOMBIE_VILLAGER, EntityTypes.ZOMBIE)) {
                Mob mob = h.spawn(type, HOME.offset(2, 0, 0));
                mob.hurtServer(h.getLevel(), h.getLevel().damageSources().playerAttack(player), 1000);
                points += 5;
                h.assertTrue(data(h).points() == points, "default preset credits " + type + " exactly once");
            }
            h.assertTrue(data(h).mission(RECOVER).isPresent(), "the first credited kill starts the first order");
        } finally { ThreatConfig.PRESETS.set(saved); ThreatConfig.refresh(); }
        h.succeed();
    }

    private static void menu(GameTestHelper h) {
        QaWorld.begin(h);
        var player = player(h, HOME.offset(2, 0, 0));
        var post = post(h, HOME);
        h.runAfterDelay(2, () -> {
            var catalog = catalog(h);
            var menu = new BuildingMenu(1, player.getInventory(), post, catalog); player.containerMenu = menu;
            h.assertTrue(menu.number(BuildingMenu.ORDER_STATE) == BuildingMenu.ORDER_ACTIVE && menu.order().id().equals(RECOVER)
                    && menu.number(BuildingMenu.ORDER_NEED) == 3, "Orders shows the active sample order and its target");
            h.assertTrue(menu.number(BuildingMenu.NEXT_DEFENDER_CAP) == NpcConfig.capacity(2), "next-level slots synced for the upgrade hint");
            carry(player, 3); SampleEvents.observe(player); menu.broadcastChanges();
            h.assertTrue(menu.order().id().equals("standandhold:complete_first_research") && menu.number(BuildingMenu.ORDER_NEED)
                    == catalog.research("parasite_samples").supplyCost(), "Orders moves to research and shows its local cost");
            var stale = new BuildingMenu(2, player.getInventory(), post, new FoundationCatalog(List.of(), List.of()));
            long global = data(h).supplies();
            h.assertTrue(stale.number(BuildingMenu.ORDER_STATE) == BuildingMenu.ORDER_UNAVAILABLE, "reloaded definitions show Unavailable");
            player.containerMenu = stale;
            h.assertFalse(stale.clickMenuButton(player, BuildingMenu.IMPORT), "a stale menu cannot act");
            h.assertTrue(data(h).supplies() == global && post.state().supplies() == 0, "nothing moves through a stale menu");
            var thresholds = HumanProgressionConfig.thresholds();
            for (var mission : catalog.missions().values()) {
                if (!mission.autoStart() || data(h).mission(mission.id()).map(s -> s.completed()).orElse(false)) { continue; }
                MissionManager.start(data(h), catalog, thresholds, mission.id());
                MissionManager.advance(data(h), catalog, thresholds, mission.id(), mission.target());
            }
            player.containerMenu = menu; menu.broadcastChanges();
            h.assertTrue(menu.number(BuildingMenu.ORDER_STATE) == BuildingMenu.ORDER_WAITING && menu.order().id().equals("standandhold:defend_outpost"),
                    "the operator-started defence order shows Waiting, not Active");
            h.succeed();
        });
    }
}
