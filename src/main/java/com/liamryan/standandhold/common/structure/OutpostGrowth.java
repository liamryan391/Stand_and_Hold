package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.config.StructureConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Player menus and operator helpers share this policy; terrain is inspected only on an attempt. */
public final class OutpostGrowth {
    public static final int UNSUPPORTED = 0, DISABLED = 1, LEVEL = 2, READY = 3, EXPANDED = 4,
            TERRAIN = 5, ENTITY = 6, REVIEW = 7, UNAVAILABLE = 8;
    public enum Mode { MANUAL, AUTOMATIC }
    public record Settings(Mode mode, int pause) {
        public static final Settings DEFAULT = new Settings(Mode.MANUAL, 0);
        public Settings {
            if (mode == null || pause != 0 && (pause < TERRAIN || pause > UNAVAILABLE)) { throw new IllegalArgumentException("Unsupported growth policy."); }
        }
        public CompoundTag save() {
            var tag = new CompoundTag(); tag.putInt("version", 1); tag.putInt("mode", mode.ordinal()); tag.putInt("pause", pause); return tag;
        }
        public static Settings read(CompoundTag tag) {
            for (String key : new String[]{"version", "mode", "pause"}) {
                if (!(tag.get(key) instanceof IntTag)) { throw new IllegalArgumentException("Malformed growth policy."); }
            }
            int mode = tag.getIntOr("mode", -1);
            if (tag.getIntOr("version", -1) != 1 || mode < 0 || mode >= Mode.values().length) { throw new IllegalArgumentException("Unsupported growth version/mode."); }
            return new Settings(Mode.values()[mode], tag.getIntOr("pause", -1));
        }
    }
    private OutpostGrowth() { }
    public static String label(int status) {
        return switch (status) {
            case DISABLED -> "Disabled by server"; case LEVEL -> "Requires local level 2"; case READY -> "Available";
            case EXPANDED -> "Expanded"; case TERRAIN -> "Paused: terrain/building obstruction"; case ENTITY -> "Paused: entity obstruction";
            case REVIEW -> "Paused: admin review"; case UNAVAILABLE -> "Unavailable save data"; default -> "Unsupported layout";
        };
    }
    public static int status(ServerLevel level, BuildingBlockEntity post) {
        if (!post.growthSupported()) { return UNAVAILABLE; }
        try { com.liamryan.standandhold.common.world.HumanWorldData.get(level.getServer()).requireSupportedVersion(); }
        catch (IllegalArgumentException | IllegalStateException failure) { return UNAVAILABLE; }
        try {
            post.requireWritable();
            if (post.kind() != BuildingKind.COMMAND_POST || post.isMainBase()) { return UNSUPPORTED; }
            var site = post.site();
            if (site.radius() != 4 || !site.anchor().equals(post.getBlockPos()) || !site.dimension().equals(level.dimension().identifier().toString())) { return UNSUPPORTED; }
            var operation = post.siteOperation().orElse(null);
            if (site.appliedRevision() == 1 && operation != null && operation.phase() == ExpansionOperation.Phase.APPLIED) { return EXPANDED; }
            if (site.appliedRevision() != 0 || operation != null) { return REVIEW; }
            if (!StructureConfig.EXPANSION.get()) { return DISABLED; }
            if (post.state().level() < 2) { return LEVEL; }
            return post.growthSettings().pause() == 0 ? READY : post.growthSettings().pause();
        } catch (IllegalArgumentException | IllegalStateException failure) { return UNSUPPORTED; }
    }
    public static void setMode(ServerLevel level, BuildingBlockEntity post, Mode mode) {
        if (!level.getServer().isSameThread() || post.getLevel() != level || post.isRemoved() || !level.hasChunkAt(post.getBlockPos())
                || level.getBlockEntity(post.getBlockPos()) != post || post.kind() != BuildingKind.COMMAND_POST) { throw new IllegalArgumentException("Growth requires the same loaded Command Post."); }
        if (mode == Mode.AUTOMATIC) { OutpostPreview.requirePost(level, post); }
        if (mode == Mode.AUTOMATIC && !StructureConfig.EXPANSION.get()) { throw new IllegalArgumentException("Growth disabled by server; manual mode remains available."); }
        post.changeGrowth(new Settings(mode, 0));
    }
    public static void setMode(ServerPlayer player, BuildingBlockEntity post, Mode mode) {
        if (player.isRemoved() || !player.isAlive() || player.isSpectator() || player.distanceToSqr(post.getBlockPos().getX() + 0.5,
                post.getBlockPos().getY() + 0.5, post.getBlockPos().getZ() + 0.5) > 36) { throw new IllegalArgumentException("Growth needs the loaded post within 6 blocks; no spectators."); }
        setMode(player.level(), post, mode);
    }
    public static void expand(ServerPlayer player, BuildingBlockEntity post) {
        OutpostPreview.requirePost(player, post);
        post.requireGrowthWritable();
        if (status(player.level(), post) == EXPANDED) { throw new IllegalArgumentException("Already expanded; no repeat, repair or refill."); }
        try {
            var result = OutpostExpansion.applyNow(player, post);
            if (!result.success()) { post.changeGrowth(new Settings(post.growthSettings().mode(), REVIEW)); throw new IllegalStateException(result.message()); }
            post.changeGrowth(new Settings(post.growthSettings().mode(), 0));
        } catch (IllegalArgumentException | IllegalStateException failure) {
            int state = status(player.level(), post);
            if (state == READY || state == TERRAIN || state == ENTITY) { post.changeGrowth(new Settings(post.growthSettings().mode(), blocked(failure))); }
            throw failure;
        }
    }
    public static void tick(ServerLevel level, BuildingBlockEntity post) {
        if (!level.getServer().isSameThread() || post.getLevel() != level || post.isRemoved() || !level.hasChunkAt(post.getBlockPos())
                || level.getBlockEntity(post.getBlockPos()) != post) { return; }
        if (!post.growthSupported() || post.growthSettings().mode() != Mode.AUTOMATIC || post.growthSettings().pause() != 0) { return; }
        int state = status(level, post);
        if (state == REVIEW || state == UNSUPPORTED || state == UNAVAILABLE) {
            post.changeGrowth(new Settings(Mode.AUTOMATIC, REVIEW)); return;
        }
        if (state != READY) { return; }
        try {
            var result = OutpostExpansion.applyNow(level, post);
            if (!result.success()) { post.changeGrowth(new Settings(Mode.AUTOMATIC, REVIEW)); }
        } catch (IllegalArgumentException | IllegalStateException failure) {
            post.changeGrowth(new Settings(Mode.AUTOMATIC, blocked(failure)));
        }
    }
    private static int blocked(RuntimeException failure) {
        return failure instanceof OutpostPreview.EntityObstruction ? ENTITY : TERRAIN;
    }
}
