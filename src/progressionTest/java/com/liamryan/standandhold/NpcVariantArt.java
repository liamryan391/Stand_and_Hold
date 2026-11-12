package com.liamryan.standandhold;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Derives the per-soldier appearance variants from each editable, Blockbench-exported soldier atlas. Variant 0 is the
 * authored atlas unchanged; 1..COUNT-1 recolour skin, eyes and facial hair, add headgear pattern, rank marks and wear,
 * only on pixels that are already opaque. Pure JDK so it runs anywhere; ArtChecks regenerates in memory and fails if the
 * shipped PNGs drift from their source. Regenerate with {@code gradlew generateNpcVariants} after editing a soldier.
 */
final class NpcVariantArt {
    static final int COUNT = 12;
    static final List<String> TIERS = List.of("survivor_defender", "army_rifleman", "heavy_soldier", "elite_soldier",
            "super_elite", "special_division_operative");
    private static final int[][] TONES = {{236, 192, 158}, {219, 170, 132}, {196, 143, 108}, {160, 110, 78}, {124, 82, 56}, {88, 58, 42}};
    private static final int[] EYES = {0xff25292c, 0xff42302a, 0xff345276, 0xff3e5e3c, 0xff58606a};
    private static final int[] HAIR = {0xff1c1816, 0xff3a281c, 0xff60422c, 0xff804226, 0xffc4a468, 0xff969490};

    private NpcVariantArt() { }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : "src/main/resources/assets/standandhold/textures/entity");
        Files.createDirectories(root.resolve("variants"));
        for (String tier : TIERS) {
            BufferedImage base = ImageIO.read(root.resolve(tier + ".png").toFile());
            for (int i = 0; i < COUNT; i++) { ImageIO.write(variant(base, tier, i), "png", root.resolve("variants/" + tier + "_" + i + ".png").toFile()); }
        }
        System.out.println("Generated " + TIERS.size() * COUNT + " soldier variants from the exported atlases.");
    }

    static BufferedImage variant(BufferedImage base, String tier, int index) {
        BufferedImage out = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < base.getHeight(); y++) { for (int x = 0; x < base.getWidth(); x++) { out.setRGB(x, y, base.getRGB(x, y)); } }
        if (index == 0) { return out; }
        Random random = new Random(tier.hashCode() * 31L + index * 7919L);
        int skin = base.getRGB(12, 12), shade = base.getRGB(8, 12), mouth = base.getRGB(12, 14), eye = base.getRGB(10, 11);
        int[] tone = TONES[(index * 5 + tier.length()) % TONES.length];
        int newSkin = argb(tone[0], tone[1], tone[2]), newShade = scale(newSkin, 0.86), newMouth = mix(scale(newSkin, 0.72), 0xff964646, 0.25);
        Map<Integer, Integer> face = new HashMap<>();
        face.put(skin, newSkin); face.put(shade, newShade); face.put(mouth, newMouth);
        recolour(out, 0, 0, 32, 16, face);
        recolour(out, 40, 30, 16, 2, Map.of(skin, newSkin));
        int newEye = EYES[random.nextInt(EYES.length)];
        for (int x : new int[] {10, 13}) { if (out.getRGB(x, 11) == eye) { out.setRGB(x, 11, newEye); } }
        int hair = HAIR[random.nextInt(HAIR.length)];
        // Hair under the headgear rim: sideburns on both sides, and a hairline on skin-coloured backs of the head.
        for (int[] p : new int[][] {{5, 10}, {6, 10}, {6, 11}, {17, 10}, {18, 10}, {17, 11}}) { paintOver(out, p[0], p[1], hair, newSkin, newShade); }
        for (int x = 24; x < 32; x++) { paintOver(out, x, 10, hair, newSkin, newShade); }
        switch (random.nextInt(6)) {
            case 1 -> { for (int x : new int[] {9, 10, 13, 14}) { paintOver(out, x, 10, hair, newSkin, newShade); } }
            // A small chin goatee; a full-width moustache read as an open mouth in the close-up capture.
            case 2 -> { for (int x = 11; x <= 12; x++) { paintOver(out, x, 15, hair, newSkin, newShade); } }
            case 3 -> {
                for (int x = 9; x <= 14; x++) { paintOver(out, x, 15, hair, newSkin, newShade); }
                for (int[] p : new int[][] {{9, 14}, {10, 14}, {14, 14}, {8, 13}, {8, 14}, {15, 13}, {15, 14}}) { paintOver(out, p[0], p[1], hair, newSkin, newShade); }
            }
            case 4 -> { for (int x = 9; x <= 14; x++) { if ((x & 1) == 0) { paintOver(out, x, 15, scale(newSkin, 0.78), newSkin, newShade); } } }
            case 5 -> { paintOver(out, 14, 12, scale(newSkin, 1.12), newSkin, newShade); paintOver(out, 14, 13, scale(newSkin, 1.12), newSkin, newShade); }
            default -> { }
        }
        headgear(out, random, index % 3);
        rank(out, index % 4);
        wear(out, random);
        return out;
    }

    /** Headgear box at the shared (0,32) UV: plain, blotch pattern or a darker band, on its dominant colour only. */
    private static void headgear(BufferedImage out, Random random, int style) {
        int dominant = dominant(out, 0, 32, 36, 10);
        if (style == 1) {
            for (int i = 0; i < 16; i++) {
                int x = random.nextInt(35), y = 32 + random.nextInt(10);
                int colour = scale(dominant, random.nextBoolean() ? 0.84 : 1.14);
                paintOver(out, x, y, colour, dominant, dominant); paintOver(out, x + 1, y, colour, dominant, dominant);
            }
        } else if (style == 2) {
            for (int x = 0; x < 36; x++) { paintOver(out, x, 42, scale(dominant, 0.8), dominant, dominant); paintOver(out, x, 43, scale(dominant, 0.8), dominant, dominant); }
        }
    }

    /** 0-3 short rank stripes on the outer upper sleeve, in the brightest colour already used on the uniform. */
    private static void rank(BufferedImage out, int stripes) {
        int sleeve = dominant(out, 40, 20, 4, 4), mark = brightest(out, 16, 16, 40, 16);
        for (int i = 0; i < stripes; i++) { for (int x = 41; x <= 43; x++) { paintOver(out, x, 21 + i * 2, mark, sleeve, sleeve); } }
    }

    /** A few scuffs on boots and trouser cuffs, darker versions of the colour underneath. */
    private static void wear(BufferedImage out, Random random) {
        for (int i = 0; i < 6; i++) {
            int x = random.nextInt(16), y = 26 + random.nextInt(6), here = out.getRGB(x, y);
            if (here >>> 24 == 0xff) { out.setRGB(x, y, scale(here, 0.82)); }
        }
    }

    private static void recolour(BufferedImage out, int x0, int y0, int w, int h, Map<Integer, Integer> map) {
        for (int y = y0; y < y0 + h; y++) { for (int x = x0; x < x0 + w; x++) { var to = map.get(out.getRGB(x, y)); if (to != null) { out.setRGB(x, y, to); } } }
    }
    /** Paints only over one of the expected opaque colours, so cutouts and other details are never touched. */
    private static void paintOver(BufferedImage out, int x, int y, int colour, int a, int b) {
        if (x < 0 || y < 0 || x >= out.getWidth() || y >= out.getHeight()) { return; }
        int here = out.getRGB(x, y);
        if (here >>> 24 == 0xff && (here == a || here == b)) { out.setRGB(x, y, colour); }
    }
    private static int dominant(BufferedImage image, int x0, int y0, int w, int h) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int y = y0; y < y0 + h; y++) { for (int x = x0; x < x0 + w; x++) { int c = image.getRGB(x, y); if (c >>> 24 == 0xff) { counts.merge(c, 1, Integer::sum); } } }
        return counts.entrySet().stream().max(Map.Entry.<Integer, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey())).map(Map.Entry::getKey).orElse(0xff808080);
    }
    private static int brightest(BufferedImage image, int x0, int y0, int w, int h) {
        int best = 0xffd0c8a8, luma = -1;
        for (int y = y0; y < y0 + h; y++) { for (int x = x0; x < x0 + w; x++) {
            int c = image.getRGB(x, y); if (c >>> 24 != 0xff) { continue; }
            int l = ((c >> 16) & 255) * 3 + ((c >> 8) & 255) * 6 + (c & 255);
            if (l > luma) { luma = l; best = c; }
        } }
        return best;
    }
    private static int argb(int r, int g, int b) { return 0xff000000 | clamp(r) << 16 | clamp(g) << 8 | clamp(b); }
    private static int scale(int c, double f) { return argb((int) Math.round(((c >> 16) & 255) * f), (int) Math.round(((c >> 8) & 255) * f), (int) Math.round((c & 255) * f)); }
    private static int mix(int a, int b, double t) {
        return argb((int) Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t), (int) Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t),
                (int) Math.round((a & 255) * (1 - t) + (b & 255) * t));
    }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
