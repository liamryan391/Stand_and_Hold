import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Original code-native equipment pixel art. No imported skins, textures or image inputs. */
class GenerateEquipmentTextures {
    static final int INK = 0xff202d32, LIGHT = 0xffe0e8dd, TEAL = 0xff416e70, METAL = 0xff80959a;
    public static void main(String[] args) throws Exception {
        for (boolean elite : new boolean[]{false, true}) {
            String tier = elite ? "elite" : "army";
            int shell = elite ? 0xff3d4555 : TEAL, mark = elite ? 0xffa8c59b : 0xffcbd0b2;
            for (String type : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
                BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                switch (type) {
                    case "helmet" -> { rect(icon, 2, 3, 12, 8, INK); rect(icon, 3, 3, 10, 6, shell); rect(icon, 5, 7, 6, 4, 0); rect(icon, 4, 4, 8, 1, mark); }
                    case "chestplate" -> { rect(icon, 1, 3, 14, 5, INK); rect(icon, 3, 5, 10, 9, INK); rect(icon, 2, 4, 12, 3, shell); rect(icon, 4, 6, 8, 7, shell); rect(icon, 6, 3, 4, 3, 0); rect(icon, 5, 7, 2, 4, mark); rect(icon, 9, 7, 2, 4, mark); }
                    case "leggings" -> { rect(icon, 3, 2, 10, 12, INK); rect(icon, 4, 3, 8, 10, shell); rect(icon, 7, 7, 2, 7, 0); rect(icon, 4, 3, 8, 2, mark); }
                    case "boots" -> { rect(icon, 2, 5, 5, 9, INK); rect(icon, 9, 5, 5, 9, INK); rect(icon, 3, 6, 3, 5, shell); rect(icon, 10, 6, 3, 5, shell); rect(icon, 3, 8, 3, 1, mark); rect(icon, 10, 8, 3, 1, mark); }
                }
                if (elite) { rect(icon, 4, 12, 2, 1, LIGHT); rect(icon, 10, 12, 2, 1, LIGHT); }
                write(icon, "item/" + tier + "_" + type + ".png");
            }
            BufferedImage outer = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
            box(outer, 0, 0, 8, 8, 8, shell); rect(outer, 8, 8, 8, 8, 0); rect(outer, 8, 8, 8, 2, shell); rect(outer, 9, 8, 6, 1, mark);
            box(outer, 16, 16, 8, 12, 4, shell); rect(outer, 20, 20, 8, 12, INK); rect(outer, 21, 23, 2, 6, mark); rect(outer, 25, 23, 2, 6, mark); rect(outer, 20, 30, 8, 1, METAL);
            box(outer, 40, 16, 4, 12, 4, shell); rect(outer, 40, 22, 16, 1, mark); if (elite) { rect(outer, 40, 24, 16, 1, mark); }
            box(outer, 0, 16, 4, 12, 4, INK); rect(outer, 0, 16, 16, 10, 0); rect(outer, 0, 27, 16, 1, mark);
            write(outer, "entity/equipment/humanoid/" + tier + ".png");
            write(outer, "entity/equipment/humanoid_baby/" + tier + ".png");
            BufferedImage inner = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
            box(inner, 0, 16, 4, 12, 4, shell); rect(inner, 0, 20, 16, 1, mark); rect(inner, 4, 25, 4, 2, INK);
            box(inner, 16, 16, 8, 12, 4, shell); rect(inner, 16, 23, 24, 9, 0); rect(inner, 20, 20, 8, 2, INK); rect(inner, 23, 20, 2, 2, mark);
            write(inner, "entity/equipment/humanoid_leggings/" + tier + ".png");
        }
        BufferedImage blade = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int i = 2; i < 11; i++) { rect(blade, i, 13 - i, 3, 3, INK); rect(blade, i + 1, 13 - i, 1, 2, LIGHT); }
        rect(blade, 3, 10, 5, 2, TEAL); rect(blade, 2, 12, 2, 3, METAL); rect(blade, 2, 14, 3, 1, TEAL);
        write(blade, "item/anti_threat_blade.png");
        BufferedImage launcher = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        rect(launcher, 2, 4, 12, 7, INK); rect(launcher, 3, 5, 9, 5, METAL); rect(launcher, 12, 6, 3, 3, TEAL);
        rect(launcher, 4, 9, 3, 5, INK); rect(launcher, 4, 10, 2, 3, TEAL); rect(launcher, 3, 6, 6, 1, LIGHT);
        for (int x = 5; x < 11; x += 2) { rect(launcher, x, 8, 1, 2, 0xff66c9bc); }
        write(launcher, "item/prototype_launcher.png");
        BufferedImage bolt = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        rect(bolt, 3, 6, 10, 4, 0xff446b70); rect(bolt, 4, 7, 8, 2, 0xff80e0cc); rect(bolt, 6, 7, 4, 1, LIGHT);
        write(bolt, "item/prototype_bolt.png");
        System.out.println("Generated 11 original item icons and 6 original worn armour atlases.");
    }
    static void box(BufferedImage image, int u, int v, int w, int h, int d, int colour) {
        rect(image, u + d, v, 2 * w, d, colour); rect(image, u, v + d, 2 * (w + d), h, colour);
    }
    static void rect(BufferedImage image, int x, int y, int w, int h, int colour) {
        for (int py = y; py < y + h; py++) { for (int px = x; px < x + w; px++) { image.setRGB(px, py, colour); } }
    }
    static void write(BufferedImage image, String suffix) throws Exception {
        Path output = Path.of("src/main/resources/assets/standandhold/textures/" + suffix);
        Files.createDirectories(output.getParent()); ImageIO.write(image, "png", output.toFile());
    }
}
