import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Original seed uniforms: classic body UVs plus an explicit accessory region, not player-skin UVs. */
class GenerateNpcTextures {
    public static void main(String[] args) throws Exception {
        make("survivor_defender", 0xff77494b, 0xff414c46, 0xffad794c, 0xffd7a481);
        if (args.length == 0 || !args[0].equals("survivor-only")) { make("army_rifleman", 0xff397b7d, 0xff354953, 0xffb9c5a3, 0xffbd896e); }
    }
    private static void make(String id, int uniform, int trousers, int trim, int skin) throws Exception {
        BufferedImage body = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        box(body, 0, 0, 8, 8, 8, skin);
        box(body, 16, 16, 8, 12, 4, uniform);
        box(body, 40, 16, 4, 12, 4, uniform);
        box(body, 0, 16, 4, 12, 4, trousers);
        // Cap sides/top, with a clear face aligned to the head's front UV rectangle.
        rect(body, 8, 0, 16, 8, trousers);
        rect(body, 0, 8, 32, 2, trousers);
        rect(body, 24, 10, 8, 6, trousers);
        rect(body, 10, 11, 1, 1, 0xff252a2c); rect(body, 13, 11, 1, 1, 0xff252a2c);
        rect(body, 11, 14, 3, 1, 0xff895e55);
        // Torso vest, shoulder marks, sleeve cuffs and boots cover every corresponding face.
        rect(body, 21, 21, 6, 7, trousers);
        rect(body, 22, 22, 1, 4, trim); rect(body, 25, 22, 1, 4, trim);
        rect(body, 20, 29, 8, 2, trim); rect(body, 32, 29, 8, 2, trim);
        rect(body, 40, 22, 16, 1, trim); rect(body, 40, 30, 16, 2, skin);
        rect(body, 0, 29, 16, 3, 0xff282e33);
        // Original face, stitched seams and sleeve patches, with no transparent base faces.
        rect(body, 8, 10, 1, 5, 0xffa87560); rect(body, 15, 10, 1, 5, 0xffa87560);
        rect(body, 20, 20, 1, 9, trim); rect(body, 27, 20, 1, 9, trim);
        rect(body, 32, 21, 1, 8, trim); rect(body, 39, 21, 1, 8, trim);
        rect(body, 44, 23, 4, 3, trousers); rect(body, 45, 24, 2, 1, trim);
        rect(body, 4, 27, 4, 1, trim); rect(body, 12, 27, 4, 1, trim);
        if (id.equals("survivor_defender")) {
            box(body, 0, 32, 9, 2, 9, trousers); // Cap.
            box(body, 0, 44, 9, 1, 2, trousers); // Short brim.
            box(body, 0, 48, 6, 7, 3, trim); // Field pack.
            rect(body, 4, 53, 4, 3, 0xff755d43); rect(body, 5, 52, 2, 1, 0xffe0c18b);
            rect(body, 10, 41, 5, 1, uniform);
        } else {
            box(body, 0, 32, 9, 4, 9, uniform); // Helmet.
            box(body, 0, 46, 9, 9, 5, trousers); // Vest.
            rect(body, 10, 41, 5, 2, trim);
            rect(body, 45, 26, 2, 1, trim);
            rect(body, 7, 53, 2, 4, trim); rect(body, 11, 53, 2, 4, trim);
            rect(body, 6, 58, 7, 1, 0xff202b30);
        }
        write(body, "entity/" + id + ".png");

        BufferedImage egg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 1; y < 15; y++) {
            int half = y < 4 ? 2 : y < 7 ? 4 : y < 12 ? 5 : 3;
            for (int x = 8 - half; x < 8 + half; x++) {
                egg.setRGB(x, y, x == 8 - half || x == 7 + half || y == 1 || y == 14 ? trousers : (x * 3 + y * 5) % 7 < 2 ? trim : uniform);
            }
        }
        write(egg, "item/" + id + "_spawn_egg.png");
        System.out.println("Generated original " + id + " seed atlas (64x64) and test egg (16x16)");
    }
    private static void box(BufferedImage image, int u, int v, int w, int h, int d, int color) {
        rect(image, u + d, v, w * 2, d, color);
        rect(image, u, v + d, (w + d) * 2, h, color);
    }
    private static void rect(BufferedImage image, int x, int y, int w, int h, int color) {
        for (int py = y; py < y + h; py++) { for (int px = x; px < x + w; px++) { image.setRGB(px, py, color); } }
    }
    private static void write(BufferedImage image, String name) throws Exception {
        Path output = Path.of("src/main/resources/assets/standandhold/textures/" + name);
        Files.createDirectories(output.getParent());
        ImageIO.write(image, "png", output.toFile());
    }
}
