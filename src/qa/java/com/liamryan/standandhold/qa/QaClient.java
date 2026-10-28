package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.client.BuildingScreen;
import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import com.liamryan.standandhold.common.npc.DefenderCapacity;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.threat.ThreatResponse;
import com.liamryan.standandhold.common.structure.StructureKind;
import com.liamryan.standandhold.common.structure.StructurePlacement;
import com.liamryan.standandhold.common.structure.OutpostPreview;
import com.liamryan.standandhold.common.structure.OutpostExpansion;
import com.liamryan.standandhold.common.structure.ExpansionOperation;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

/** Actual client/server menu transport and rendering. Never included in the release jar. */
@Mod(value = QaMod.ID, dist = Dist.CLIENT)
public final class QaClient {
    private static final BlockPos POST = new BlockPos(8, 64, 8), LAB = new BlockPos(8, 64, 12), CRATE = new BlockPos(12, 64, 12);
    private record View(String name, BuildingKind kind, String state, int scale) { }
    private final List<View> views = List.of(
            new View("command-post", BuildingKind.COMMAND_POST, "", 2),
            new View("command-post-upgraded-large", BuildingKind.COMMAND_POST, "upgraded", 3),
            new View("command-post-full-large", BuildingKind.COMMAND_POST, "full", 3),
            new View("command-post-disabled", BuildingKind.COMMAND_POST, "disabled", 2),
            new View("command-post-dormant-large", BuildingKind.COMMAND_POST, "dormant", 3),
            new View("supply-crate", BuildingKind.CRATE, "", 2),
            new View("lab-ready", BuildingKind.LAB, "ready", 2),
            new View("lab-running", BuildingKind.LAB, "running", 2),
            new View("lab-paused-large", BuildingKind.LAB, "paused", 3),
            new View("lab-blocked-large", BuildingKind.LAB, "blocked", 3),
            new View("lab-completed-large", BuildingKind.LAB, "completed", 3));
    private final String id = System.getProperty("standandhold.qa.id", UUID.randomUUID().toString());
    private final long deadline = System.nanoTime() + 300_000_000_000L;
    private CompletableFuture<Void> serverAction;
    private int phase, view, settle, previousPhase = -1, phaseTicks;
    private boolean capturing, finished;
    private final List<String> captures = new ArrayList<>();
    private static final List<UUID> fixtureReservations = new ArrayList<>();
    private BlockPos checkpointLab;
    private boolean previewReply, previewBlockedReply;
    private boolean expansionDisabledReply, expansionAppliedReply, expansionStatusReply;
    private String consentCommand, expansionProtected;
    private net.minecraft.nbt.CompoundTag previewPostBefore, previewGlobalBefore;
    public QaClient() {
        if (Boolean.getBoolean("standandhold.qa.client")) { NeoForge.EVENT_BUS.addListener(this::tick); NeoForge.EVENT_BUS.addListener(this::chat); }
    }
    private void chat(ClientChatReceivedEvent event) {
        String text = event.getMessage().getString();
        if (text.contains("Checkpoint v1 / L2 / READ ONLY")) { previewReply = true; }
        if (text.contains("Protected block entity/container")) { previewBlockedReply = true; }
        if (text.contains("Expansion is disabled by default")) { expansionDisabledReply = true; }
        if (text.contains("Apron applied once")) { expansionAppliedReply = true; }
        if (text.contains("APPLIED / ALL_AFTER")) { expansionStatusReply = true; }
        if (event.getMessage().getStyle().getClickEvent() instanceof net.minecraft.network.chat.ClickEvent.SuggestCommand suggest
                && suggest.command().startsWith("/standandhold outpost expand ")) { consentCommand = suggest.command(); }
    }
    private void tick(ClientTickEvent.Post event) {
        if (finished) { return; }
        var mc = Minecraft.getInstance();
        try {
            if (System.nanoTime() > deadline) { throw new IllegalStateException("QA client timed out in phase " + phase); }
            mc.options.pauseOnLostFocus = false;
            mc.options.onboardAccessibility = false;
            if (previousPhase != phase) { previousPhase = phase; phaseTicks = 0; }
            if (++phaseTicks > 200 && phase == 4) { throw new IllegalStateException("Expected real menu did not open: " + views.get(view).name()); }
            if (serverAction != null) {
                if (!serverAction.isDone()) { return; }
                serverAction.join(); serverAction = null; settle = 0;
            }
            if (capturing) { return; }
            switch (phase) {
                case 0 -> {
                    if (!(mc.gui.screen() instanceof TitleScreen)) {
                        if (mc.gui.screen() instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) { mc.gui.setScreen(new TitleScreen()); }
                        return;
                    }
                    phase = 1;
                    mc.createWorldOpenFlows().createFreshLevel("qa-" + id,
                            new LevelSettings("Automated QA " + id, GameType.CREATIVE, LevelSettings.DifficultySettings.DEFAULT, true, WorldDataConfiguration.DEFAULT),
                            new WorldOptions(170310L, false, false),
                            registries -> registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                            new TitleScreen());
                }
                case 1 -> {
                    if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) { return; }
                    phase = 2;
                    onServer(mc, () -> fixture(mc.getSingleplayerServer().overworld(), player(mc)));
                }
                case 2 -> {
                    if (++settle < 30) { return; }
                    mc.options.guiScale().set(2); mc.resizeGui();
                    mc.gui.setScreen(null);
                    capture(mc, "scene", 3);
                }
                case 3 -> {
                    if (view >= views.size()) { phase = 6; settle = 0; return; }
                    var next = views.get(view);
                    mc.options.guiScale().set(next.scale()); mc.resizeGui();
                    phase = 4;
                    onServer(mc, () -> open(mc, next));
                }
                case 4 -> {
                    if (!(mc.gui.screen() instanceof BuildingScreen) || !(mc.player.containerMenu instanceof BuildingMenu menu) || menu.kind() != views.get(view).kind()) { return; }
                    if (++settle < 20) { return; }
                    var next = views.get(view);
                    if (next.kind() == BuildingKind.COMMAND_POST) {
                        int cap = switch (next.state()) { case "upgraded", "full" -> 5; case "disabled" -> 0; default -> 3; };
                        int reason = switch (next.state()) { case "full" -> 4; case "disabled" -> 1; case "dormant" -> 2; default -> 0; };
                        if (menu.number(BuildingMenu.DEFENDER_CAP) != cap || menu.number(BuildingMenu.DEPLOYMENT_STATUS) != reason
                                || menu.number(BuildingMenu.DEFENDERS) != (next.state().equals("full") ? 5 : 0)) {
                            throw new IllegalStateException("Post capacity/status did not sync: " + next.state());
                        }
                    }
                    if (next.kind() == BuildingKind.LAB) {
                        long expected = next.state().equals("blocked") ? 2 : next.state().equals("completed") ? 1 : 0;
                        if (next.state().equals("blocked")) {
                            if (menu.number(BuildingMenu.STATUS) == 0 || menu.number(BuildingMenu.STATUS) == 1) { throw new IllegalStateException("Blocked research not synced"); }
                        } else if (menu.number(BuildingMenu.STATUS) != expected) { throw new IllegalStateException("Research state did not sync: " + next.state()); }
                    }
                    capture(mc, next.name(), 5);
                }
                case 5 -> {
                    boolean reset = views.get(view).kind() == BuildingKind.COMMAND_POST;
                    onServer(mc, () -> {
                        player(mc).closeContainer();
                        if (reset) { resetPost(mc.getSingleplayerServer().overworld()); }
                    });
                    phase = 17; settle = 0;
                }
                case 17 -> {
                    if (++settle > 200) { throw new IllegalStateException("Server menu-close acknowledgement did not arrive"); }
                    // Server-initiated close has no delayed C2S echo to close the next menu.
                    if (mc.player.containerMenu != mc.player.inventoryMenu || mc.gui.screen() instanceof BuildingScreen) { return; }
                    view++; phase = 3; settle = 0;
                }
                case 6 -> {
                    mc.options.guiScale().set(3); mc.resizeGui();
                    mc.player.connection.sendCommand("standandhold guide");
                    mc.gui.setScreen(new ChatScreen("", false)); phase = 7; settle = 0;
                }
                case 7 -> { if (++settle >= 30) { capture(mc, "guide-large", 9); } }
                case 9 -> {
                    phase = 10;
                    onServer(mc, () -> {
                        var server = mc.getSingleplayerServer(); var data = HumanWorldData.get(server);
                        HumanPointManager.setStage(server, HumanStage.LOCAL_ARMY_RESPONSE);
                        ResearchManager.complete(data, FoundationResources.get(server), HumanProgressionConfig.thresholds(), "standandhold:field_communications");
                        data.setSupplies(30);
                        player(mc).connection.teleport(8.5, 64, 11.5, 180, 21);
                    });
                }
                case 10 -> {
                    if (++settle < 20) { return; }
                    mc.gui.hud.getChat().clearMessages(true);
                    mc.player.connection.sendCommand("standandhold support reinforce");
                    phase = 11; settle = 0;
                }
                case 11 -> {
                    if (++settle < 20) { return; }
                    phase = 12;
                    onServer(mc, () -> {
                        var level = mc.getSingleplayerServer().overworld(); var post = tile(level, POST);
                        var data = HumanWorldData.get(level.getServer());
                        if (data.supplies() != 20 || data.defenderCount(new BuildingPosition(level.dimension().identifier(), POST).key()) != 1
                                || ThreatResponse.state(level, post).reinforcements() != 1) { throw new IllegalStateException("Real client support command did not deploy/charge once"); }
                    });
                }
                case 12 -> capture(mc, "support-call-in", 13);
                case 13 -> {
                    mc.gui.setScreen(null); mc.options.guiScale().set(2); mc.resizeGui(); phase = 14;
                    onServer(mc, () -> {
                        var level = mc.getSingleplayerServer().overworld();
                        var placed = StructurePlacement.place(level, StructureKind.CHECKPOINT, 1, 1);
                        if (!placed.success()) { throw new IllegalStateException("QA checkpoint placement: " + placed.message()); }
                        int radius = com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS.get();
                        checkpointLab = placed.commandPost().offset(-radius + 2, 0, -radius + 3);
                        if (tile(level, checkpointLab).kind() != BuildingKind.LAB || !tile(level, checkpointLab).state().equals(BuildingState.EMPTY)) {
                            throw new IllegalStateException("Checkpoint lab is missing or not empty/idle");
                        }
                        var camera = player(mc);
                        camera.getAbilities().flying = true; camera.onUpdateAbilities();
                        camera.connection.teleport(24.5, 69, 34.5, 180, 30);
                    });
                }
                case 14 -> {
                    if (++settle > 300) { throw new IllegalStateException("Generated checkpoint did not become visible for screenshot"); }
                    if (settle < 60 || mc.level == null || !mc.level.getBlockState(checkpointLab).is(BuildingRegistry.LAB.get())
                            || !mc.levelRenderer.isSectionCompiledAndVisible(checkpointLab, 0)) { return; }
                    if (mc.player.position().distanceToSqr(24.5, 69, 34.5) > 1) { throw new IllegalStateException("QA checkpoint camera left its flight position"); }
                    mc.gui.hud.getChat().clearMessages(true);
                    capture(mc, "checkpoint-with-lab", 15);
                }
                case 15 -> {
                    phase = 16;
                    onServer(mc, () -> {
                        var level = mc.getSingleplayerServer().overworld(); var lab = tile(level, checkpointLab);
                        var pos = checkpointLab;
                        player(mc).connection.teleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 2.5, 180, 12);
                        player(mc).openMenu(lab, lab::writeMenuData);
                    });
                }
                case 16 -> {
                    if (++settle > 200) { throw new IllegalStateException("Generated lab menu did not open"); }
                    if (!(mc.gui.screen() instanceof BuildingScreen) || !(mc.player.containerMenu instanceof BuildingMenu menu)
                            || menu.kind() != BuildingKind.LAB || settle < 20) { return; }
                    if (menu.number(BuildingMenu.LOCAL) != 0 || menu.number(BuildingMenu.SAMPLES) != 0
                            || menu.number(BuildingMenu.WORK) != 0 || menu.number(BuildingMenu.STATUS) != 6) {
                        throw new IllegalStateException("Generated lab empty state did not sync");
                    }
                    capture(mc, "checkpoint-lab-menu", 18);
                }
                case 18 -> {
                    phase = 19;
                    onServer(mc, () -> {
                        var level = mc.getSingleplayerServer().overworld(); player(mc).closeContainer();
                        var post = tile(level, checkpointLab.offset(2, 0, 1));
                        post.change(new BuildingState(2, post.state().supplies(), post.state().samples(), "", 0));
                        var pos = post.getBlockPos();
                        player(mc).connection.teleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16);
                        previewPostBefore = post.saveWithFullMetadata(level.registryAccess());
                        previewGlobalBefore = (net.minecraft.nbt.CompoundTag) HumanWorldData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, HumanWorldData.get(level.getServer())).getOrThrow();
                    });
                }
                case 19 -> {
                    if (++settle < 20 || mc.player.containerMenu != mc.player.inventoryMenu || mc.gui.screen() instanceof BuildingScreen) { return; }
                    mc.options.guiScale().set(2); mc.resizeGui(); mc.gui.hud.getChat().clearMessages(true);
                    mc.player.connection.sendCommand("standandhold outpost preview");
                    mc.gui.setScreen(new ChatScreen("", false)); phase = 20; settle = 0;
                }
                case 20 -> {
                    if (++settle > 200) { throw new IllegalStateException("Real operator preview reply missing"); }
                    if (!previewReply || settle < 20) { return; }
                    phase = 21;
                    onServer(mc, () -> checkPreviewUnchanged(mc));
                }
                case 21 -> capture(mc, "outpost-preview", 22);
                case 22 -> { mc.options.guiScale().set(3); mc.resizeGui(); phase = 23; settle = 0; }
                case 23 -> { if (++settle >= 10) { capture(mc, "outpost-preview-large", 24); } }
                case 24 -> {
                    phase = 25;
                    onServer(mc, () -> {
                        var level = mc.getSingleplayerServer().overworld(); var post = tile(level, checkpointLab.offset(2, 0, 1));
                        var pos = post.site().center().offset(-6, 1, -6);
                        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
                        ((net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(pos)).setItem(0, net.minecraft.world.item.Items.DIAMOND.getDefaultInstance());
                    });
                }
                case 25 -> {
                    mc.gui.hud.getChat().clearMessages(true); mc.player.connection.sendCommand("standandhold outpost preview");
                    phase = 26; settle = 0;
                }
                case 26 -> {
                    if (++settle > 200) { throw new IllegalStateException("Blocked preview reply missing"); }
                    if (!previewBlockedReply || settle < 20) { return; }
                    phase = 27;
                    onServer(mc, () -> checkPreviewUnchanged(mc));
                }
                case 27 -> { if (++settle >= 10) { capture(mc, "outpost-preview-blocked-large", 28); } }
                case 28 -> {
                    phase = 29;
                    onServer(mc, () -> {
                        var level = mc.getSingleplayerServer().overworld(); var post = tile(level, checkpointLab.offset(2, 0, 1));
                        var pos = post.site().center().offset(-6, 1, -6);
                        ((net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(pos)).clearContent();
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        // Keep the extended visual fixture deterministic; mechanics/loaded intervals are native-tested.
                        com.liamryan.standandhold.config.BuildingConfig.POINTS.set(0); com.liamryan.standandhold.config.BuildingConfig.SUPPLIES.set(0);
                        NpcConfig.DEPLOY.set(false); com.liamryan.standandhold.config.ResponseConfig.RAIDS.set(false); com.liamryan.standandhold.config.ResponseConfig.REINFORCEMENTS.set(false);
                        com.liamryan.standandhold.config.StructureConfig.EXPANSION.set(false);
                    });
                }
                case 29 -> { mc.gui.hud.getChat().clearMessages(true); mc.player.connection.sendCommand("standandhold outpost prepare"); phase = 30; settle = 0; }
                case 30 -> {
                    if (++settle > 200) { throw new IllegalStateException("Disabled expansion reply missing"); }
                    if (expansionDisabledReply && settle >= 15) { capture(mc, "outpost-expansion-disabled-large", 31); }
                }
                case 31 -> {
                    phase = 32;
                    onServer(mc, () -> {
                        com.liamryan.standandhold.config.StructureConfig.EXPANSION.set(true);
                        var level = mc.getSingleplayerServer().overworld(); var post = tile(level, checkpointLab.offset(2, 0, 1));
                        expansionProtected = OutpostExpansion.protectedDigest(level, post, post.site());
                        previewGlobalBefore = (net.minecraft.nbt.CompoundTag) HumanWorldData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, HumanWorldData.get(level.getServer())).getOrThrow();
                    });
                }
                case 32 -> {
                    mc.options.guiScale().set(2); mc.resizeGui(); mc.gui.hud.getChat().clearMessages(true);
                    mc.player.connection.sendCommand("standandhold outpost prepare"); phase = 33; settle = 0;
                }
                case 33 -> {
                    if (++settle > 100) { throw new IllegalStateException("Actual safe consent suggestion missing"); }
                    if (consentCommand != null && settle >= 10) { capture(mc, "outpost-expansion-consent", 34); }
                }
                case 34 -> { mc.options.guiScale().set(3); mc.resizeGui(); phase = 35; settle = 0; }
                case 35 -> { if (++settle >= 8) { capture(mc, "outpost-expansion-consent-large", 36); } }
                case 36 -> {
                    mc.gui.hud.getChat().clearMessages(true); mc.player.connection.sendCommand(consentCommand.substring(1)); phase = 37; settle = 0;
                }
                case 37 -> {
                    if (++settle > 200) { throw new IllegalStateException("Actual expansion confirmation failed"); }
                    if (!expansionAppliedReply || settle < 15) { return; }
                    phase = 38;
                    onServer(mc, () -> {
                        var level = mc.getSingleplayerServer().overworld(); var post = tile(level, checkpointLab.offset(2, 0, 1));
                        var operation = post.siteOperation().orElseThrow();
                        if (post.site().appliedRevision() != 1 || operation.phase() != ExpansionOperation.Phase.APPLIED
                                || operation.observe(pos -> net.minecraft.nbt.NbtUtils.writeBlockState(level.getBlockState(pos))).state() != ExpansionOperation.Observed.ALL_AFTER
                                || !expansionProtected.equals(OutpostExpansion.protectedDigest(level, post, post.site()))
                                || !previewGlobalBefore.equals(HumanWorldData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, HumanWorldData.get(level.getServer())).getOrThrow())) {
                            throw new IllegalStateException("Real confirmation changed protected state/resources or failed exact application");
                        }
                    });
                }
                case 38 -> capture(mc, "outpost-expansion-applied-large", 39);
                case 39 -> { mc.gui.hud.getChat().clearMessages(true); mc.player.connection.sendCommand("standandhold outpost status"); phase = 40; settle = 0; }
                case 40 -> {
                    if (++settle > 200) { throw new IllegalStateException("Operation status reply missing"); }
                    if (expansionStatusReply && settle >= 15) { capture(mc, "outpost-expansion-status-large", 41); }
                }
                case 41 -> {
                    mc.gui.setScreen(null); mc.gui.hud.getChat().clearMessages(true); phase = 42;
                    onServer(mc, () -> {
                        var post = tile(mc.getSingleplayerServer().overworld(), checkpointLab.offset(2, 0, 1)); var center = post.site().center();
                        var player = player(mc); player.getAbilities().flying = true; player.onUpdateAbilities();
                        player.connection.teleport(center.getX() + 0.5, center.getY() + 10, center.getZ() + 12.5, 180, 40);
                        com.liamryan.standandhold.config.StructureConfig.EXPANSION.set(false);
                    });
                }
                case 42 -> {
                    if (++settle < 25 || !mc.levelRenderer.isSectionCompiledAndVisible(checkpointLab, 0)) { return; }
                    capture(mc, "outpost-expanded-apron", 8);
                }
                case 8 -> {
                    mc.gui.setScreen(null);
                    mc.disconnectWithSavingScreen();
                    result(mc, true, "Real menus/screens captured; world saved and client stopped");
                }
                default -> throw new IllegalStateException("Unknown QA phase");
            }
        } catch (Throwable failure) {
            com.mojang.logging.LogUtils.getLogger().error("QA client failed", failure);
            if (mc.level != null) { mc.disconnectWithSavingScreen(); }
            result(mc, false, failure.toString());
        }
    }
    private static ServerPlayer player(Minecraft mc) { return Objects.requireNonNull(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())); }
    private void checkPreviewUnchanged(Minecraft mc) {
        var level = mc.getSingleplayerServer().overworld(); var post = tile(level, checkpointLab.offset(2, 0, 1));
        if (!post.saveWithFullMetadata(level.registryAccess()).equals(previewPostBefore)
                || !HumanWorldData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, HumanWorldData.get(level.getServer())).getOrThrow().equals(previewGlobalBefore)) {
            throw new IllegalStateException("Real preview command mutated post or global data");
        }
    }
    private void onServer(Minecraft mc, Runnable work) { serverAction = CompletableFuture.runAsync(work, mc.getSingleplayerServer()); }
    private static void fixture(ServerLevel level, ServerPlayer player) {
        NpcConfig.POST_CAP.set(16); NpcConfig.POST_LEVELS.set(DefenderCapacity.DEFAULT_LEVELS);
        com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS.set(4);
        level.getGameRules().set(GameRules.SPAWN_MOBS, false, level.getServer());
        level.getGameRules().set(GameRules.ADVANCE_TIME, false, level.getServer());
        for (BlockPos pos : BlockPos.betweenClosed(0, 62, 0, 31, 67, 31)) {
            level.setBlockAndUpdate(pos, (pos.getY() == 62 ? Blocks.DIRT : pos.getY() == 63 ? Blocks.GRASS_BLOCK : Blocks.AIR).defaultBlockState());
        }
        level.setBlockAndUpdate(POST, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
        level.setBlockAndUpdate(LAB, BuildingRegistry.LAB.get().defaultBlockState());
        level.setBlockAndUpdate(CRATE, BuildingRegistry.CRATE.get().defaultBlockState());
        tile(level, POST).change(BuildingState.EMPTY.supplies(20));
        tile(level, CRATE).change(BuildingState.EMPTY.supplies(27).insertSamples(3));
        HumanWorldData.get(level.getServer()).setSupplies(123);
        player.connection.teleport(9.5, 64, 17.5, 180, 12);
        for (var type : List.of(NpcRegistry.SURVIVOR.get(), NpcRegistry.ARMY.get())) {
            var npc = type.create(level, EntitySpawnReason.COMMAND);
            if (npc == null) { throw new IllegalStateException("NPC factory failed"); }
            npc.snapTo(type == NpcRegistry.SURVIVOR.get() ? 7.5 : 10.5, 64, 10.5, 0, 0); npc.setNoAi(true); level.addFreshEntity(npc);
        }
    }
    private static BuildingBlockEntity tile(ServerLevel level, BlockPos pos) { return (BuildingBlockEntity) level.getBlockEntity(pos); }
    private static void open(Minecraft mc, View view) {
        var level = mc.getSingleplayerServer().overworld(); var player = player(mc);
        player.closeContainer();
        var pos = switch (view.kind()) { case COMMAND_POST -> POST; case LAB -> LAB; case CRATE -> CRATE; };
        var building = tile(level, pos);
        if (view.kind() == BuildingKind.COMMAND_POST) {
            var data = HumanWorldData.get(level.getServer());
            var home = new BuildingPosition(level.dimension().identifier(), POST);
            resetPost(level);
            building = tile(level, POST); data.registerCommandPost(home);
            building.change(new BuildingState(view.state().equals("upgraded") || view.state().equals("full") ? 2 : 1, 20, 0, "", 0));
            NpcConfig.POST_CAP.set(view.state().equals("disabled") ? 0 : 16);
            if (view.state().equals("full")) {
                for (int i = 0; i < 5; i++) {
                    var id = UUID.randomUUID(); data.reserveDefender(home.key(), id, 5); fixtureReservations.add(id);
                }
            }
            if (view.state().equals("dormant")) { building.initializeMainBase(); data.registerMainBase(home); }
        }
        if (view.kind() == BuildingKind.LAB) {
            var state = new BuildingState(1, 10, 0, "standandhold:parasite_samples", 0, false);
            switch (view.state()) {
                case "running" -> state = state.work(6).running(true);
                case "paused" -> state = state.work(6);
                case "blocked" -> state = state.target("standandhold:special_division_training");
                case "completed" -> {
                    var data = HumanWorldData.get(level.getServer()); data.setSupplies(123);
                    ResearchManager.complete(data, FoundationResources.get(level.getServer()), HumanProgressionConfig.thresholds(), "standandhold:parasite_samples");
                    state = state.supplies(0).work(12);
                }
            }
            building.change(state);
        }
        player.connection.teleport(pos.getX() + 1.5, pos.getY(), pos.getZ() + 2.5, 180, 12);
        player.openMenu(building, building::writeMenuData);
    }
    private static void resetPost(ServerLevel level) {
        var data = HumanWorldData.get(level.getServer());
        String key = new BuildingPosition(level.dimension().identifier(), POST).key();
        for (var id : fixtureReservations) { data.releaseDefender(key, id); }
        fixtureReservations.clear();
        level.setBlockAndUpdate(POST, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(POST, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
        tile(level, POST).change(BuildingState.EMPTY.supplies(20));
        NpcConfig.POST_CAP.set(16);
    }
    private void capture(Minecraft mc, String name, int nextPhase) {
        capturing = true;
        String file = id + "-" + name + ".png";
        Screenshot.grab(mc.gameDirectory, file, mc.gameRenderer.mainRenderTarget(), 1, message -> mc.execute(() -> {
            Path path = mc.gameDirectory.toPath().resolve("screenshots").resolve(file);
            if (!Files.isRegularFile(path)) { result(mc, false, "Screenshot failed: " + message.getString()); return; }
            captures.add(file); capturing = false; phase = nextPhase; settle = 0;
        }));
    }
    private void result(Minecraft mc, boolean success, String detail) {
        finished = true;
        try {
            var values = new Properties(); values.setProperty("success", Boolean.toString(success)); values.setProperty("detail", detail);
            values.setProperty("screenshots", String.join(",", captures));
            try (var out = Files.newOutputStream(mc.gameDirectory.toPath().resolve(id + "-result.properties"), StandardOpenOption.CREATE_NEW)) { values.store(out, "Automated client QA"); }
        } catch (Exception failure) { com.mojang.logging.LogUtils.getLogger().error("Cannot write QA client result", failure); }
        mc.stop();
    }
}
