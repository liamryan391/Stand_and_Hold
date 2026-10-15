import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Original deterministic 16px utility art. Run with Java 25 from the repository root. */
class GenerateBuildingTextures {
    public static void main(String[] args) throws Exception {
        Path output = Path.of("src/main/resources/assets/standandhold/textures/block");
        Files.createDirectories(output);
        String[] names = {"field_command_post", "research_lab", "supply_crate"};
        int[] body = {0x536952, 0xb6c4c0, 0x856846};
        for (int kind = 0; kind < names.length; kind++) {
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int color = body[kind];
                    if (x == 0 || y == 0 || x == 15 || y == 15) { color = 0x303c36; }
                    else if ((x == 2 || x == 13) && (y == 2 || y == 13)) { color = 0xd4d7c9; }
                    else if (kind == 0 && x >= 4 && x <= 11 && y >= 4 && y <= 9) {
                        color = x == 7 || y == 6 ? 0x84d391 : 0x1b3535;
                    } else if (kind == 1 && x >= 4 && x <= 11 && y >= 4 && y <= 11) {
                        color = x >= 6 && x <= 9 || y >= 6 && y <= 9 ? 0x409eaa : 0x354a51;
                    } else if (kind == 2 && (x == 4 || x == 11)) { color = 0xadb1a0; }
                    else if (kind == 2 && y == 8 && x > 5 && x < 10) { color = 0xe0bb63; }
                    else if ((x + y * 3) % 11 == 0) { color = shade(color); }
                    image.setRGB(x, y, 0xff000000 | color);
                }
            }
            ImageIO.write(image, "png", output.resolve(names[kind] + ".png").toFile());
            System.out.println("Generated original " + names[kind] + " texture");
        }
    }
    private static int shade(int color) { return ((color & 0xfefefe) >> 1) + ((color & 0xfcfcfc) >> 2); }
}
