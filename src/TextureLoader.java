import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextureLoader {
    public static BufferedImage wallTexture;

    public static void loadTextures() {
        try {
            wallTexture = ImageIO.read(new File("src/textures/brick.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
