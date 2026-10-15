package com.liamryan.standandhold.common.foundation;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.resource.ListenerKey;

/** Owned by the server's reloadable resources, never by a static world cache. */
public final class FoundationResources extends SimplePreparableReloadListener<FoundationResources.Snapshot> {
    private static final ListenerKey<FoundationResources> KEY = ListenerKey.create(Identifier.fromNamespaceAndPath("standandhold", "foundation"));
    private Snapshot snapshot = new Snapshot(null, "Foundation definitions not loaded");
    public static void register(AddServerReloadListenersEvent event) { event.addRetainedListener(KEY, new FoundationResources()); }
    public static FoundationCatalog get(MinecraftServer server) {
        Snapshot current = server.getServerResources().managers().getListener(KEY).snapshot;
        if (current.catalog() == null) { throw new IllegalStateException(current.error()); }
        return current.catalog();
    }
    @Override protected Snapshot prepare(ResourceManager resources, ProfilerFiller profiler) {
        try { return new Snapshot(FoundationCatalog.load(resources), ""); }
        catch (IllegalArgumentException | IllegalStateException exception) { return new Snapshot(null, exception.getMessage()); }
    }
    @Override protected void apply(Snapshot value, ResourceManager resources, ProfilerFiller profiler) { snapshot = value; }
    public record Snapshot(FoundationCatalog catalog, String error) { }
}
