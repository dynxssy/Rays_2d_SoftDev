import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextureLoader {
    public static BufferedImage wallTexture;

    public static void loadTextures() {
        try {
            wallTexture = ImageIO.read(new File("Rays_2d_SoftDev-main/src/textures/brick.png"));
        } catch (IOException e) {
            System.err.println("Failed to load wall texture. Using default texture.");
            wallTexture = createDefaultTexture(); // Use default texture
        }
    }

    private static BufferedImage createDefaultTexture() {
        BufferedImage defaultTexture = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int color = (x / 8 % 2 == y / 8 % 2) ? 0xFFFFFF : 0xAAAAAA; // Checkerboard pattern
                defaultTexture.setRGB(x, y, color);
            }
        }
        return defaultTexture;
    }
}
