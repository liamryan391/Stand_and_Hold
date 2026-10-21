import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Original code-native 16px specimen pouch icon, using no external art. */
class GenerateSampleTexture {
    public static void main(String[] args) throws Exception {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 2; y <= 14; y++) {
            for (int x = 3; x <= 12; x++) {
                int color = y <= 3 ? 0xffb8c0c2 : x == 3 || x == 12 || y == 14 ? 0xff385459 : 0xff8dc3bd;
                if (y >= 7 && y <= 11 && x >= 5 && x <= 10) { color = (x + y) % 3 == 0 ? 0xffcb6879 : 0xff803f61; }
                if (y == 5 && x >= 5 && x <= 10) { color = 0xffe9ede0; }
                if (x == 4 && y >= 6 && y <= 12) { color = 0xffc3e3df; }
                if (y == 2 && x >= 4 && x <= 11) { color = 0xffe7eeeb; }
                image.setRGB(x, y, color);
            }
        }
        Path output = Path.of("src/main/resources/assets/standandhold/textures/item/parasite_tissue_sample.png");
        Files.createDirectories(output.getParent());
        ImageIO.write(image, "png", output.toFile());
        System.out.println("Generated original Parasite Tissue Sample icon");
    }
}
