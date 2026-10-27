package com.liamryan.standandhold.qa;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

/** Generates our empty test arena with Minecraft's own NBT writer. */
public final class QaTemplates {
    private QaTemplates() { }
    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]).resolve("data/standandhold_qa/structure/arena.nbt");
        Files.createDirectories(output.getParent());
        var tag = new CompoundTag();
        var size = new ListTag();
        for (int value : new int[]{64, 8, 64}) { size.add(IntTag.valueOf(value)); }
        tag.put("size", size);
        var palette = new ListTag();
        var air = new CompoundTag(); air.putString("Name", "minecraft:air"); palette.add(air);
        tag.put("palette", palette);
        tag.put("blocks", new ListTag()); tag.put("entities", new ListTag());
        NbtIo.writeCompressed(tag, output);
        Files.write(Path.of(args[0]).resolve("standandhold-qa-tests.txt"),
                WorldTests.cases().keySet().stream().map(name -> QaMod.ID + ":" + name).sorted().toList());
    }
}
