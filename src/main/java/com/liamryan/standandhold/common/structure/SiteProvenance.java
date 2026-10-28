package com.liamryan.standandhold.common.structure;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.Identifier;

/** Immutable generation receipt; possession is not a claim over surrounding player terrain. */
public record SiteProvenance(UUID id, String dimension, BlockPos anchor, int radius, String origin, int appliedRevision) {
    public static final String LAYOUT = "standandhold:checkpoint_lab_v1";
    public SiteProvenance {
        if (id == null || dimension == null || dimension.length() > 128 || Identifier.tryParse(dimension) == null
                || !Identifier.tryParse(dimension).toString().equals(dimension)
                || anchor == null || (anchor.getX() & 15) != 8 || (anchor.getZ() & 15) != 7
                || Math.abs((long) anchor.getX()) > 29999984 || Math.abs((long) anchor.getZ()) > 29999984
                || anchor.getY() < -2047 || anchor.getY() > 2047 || radius < 3 || radius > 5
                || !("debug".equals(origin) || "worldgen".equals(origin)) || appliedRevision < 0) {
            throw new IllegalArgumentException("Malformed checkpoint site provenance.");
        }
        anchor = anchor.immutable();
    }
    public BlockPos center() { return anchor.south().below(); }
    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putInt("site_version", 1); tag.putString("uuid", id.toString()); tag.putString("dimension", dimension);
        tag.putString("kind", "checkpoint"); tag.putString("layout", LAYOUT); tag.putInt("layout_revision", 1);
        tag.putInt("anchor_x", anchor.getX()); tag.putInt("anchor_y", anchor.getY()); tag.putInt("anchor_z", anchor.getZ());
        tag.putInt("floor_y", center().getY()); tag.putInt("radius", radius); tag.putString("orientation", "south");
        tag.putString("origin", origin); tag.putInt("applied_revision", appliedRevision);
        return tag;
    }
    public static SiteProvenance read(CompoundTag tag) {
        if (integer(tag, "site_version") != 1 || integer(tag, "layout_revision") != 1
                || !tag.getStringOr("kind", "").equals("checkpoint") || !tag.getStringOr("layout", "").equals(LAYOUT)
                || !tag.getStringOr("orientation", "").equals("south")) {
            throw new IllegalArgumentException("Unsupported checkpoint site/layout version; no adoption or repair attempted.");
        }
        String uuid = tag.getStringOr("uuid", "");
        if (uuid.length() != 36 || !UUID.fromString(uuid).toString().equals(uuid)) { throw new IllegalArgumentException("Malformed site UUID."); }
        var anchor = new BlockPos(integer(tag, "anchor_x"), integer(tag, "anchor_y"), integer(tag, "anchor_z"));
        var site = new SiteProvenance(UUID.fromString(uuid), tag.getStringOr("dimension", ""), anchor,
                integer(tag, "radius"), tag.getStringOr("origin", ""), integer(tag, "applied_revision"));
        if (integer(tag, "floor_y") != site.center().getY()) { throw new IllegalArgumentException("Malformed site floor height."); }
        return site;
    }
    private static int integer(CompoundTag tag, String key) {
        if (!(tag.get(key) instanceof IntTag)) { throw new IllegalArgumentException("Malformed site field: " + key); }
        return tag.getIntOr(key, 0);
    }
}
