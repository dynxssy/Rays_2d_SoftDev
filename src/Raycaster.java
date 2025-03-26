import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Raycaster {
    private int[][] map;
    private int mapWidth;
    private int mapHeight;
    private double playerX;
    private double playerY;
    private double playerAngle;
    private int screenWidth;
    private int screenHeight;
    private BufferedImage wallTexture;
    private int fov;
    private int rayResolution;

    public Raycaster(int[][] map, int mapWidth, int mapHeight, double playerX, double playerY, double playerAngle, int screenWidth, int screenHeight, int fov, int rayResolution) {
        this.map = map;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerAngle = playerAngle;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.fov = fov;
        this.rayResolution = rayResolution;
        try {
            wallTexture = ImageIO.read(new File("assets/wall.png")); // Add your texture path
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage castRays() {
        BufferedImage image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // Render ceiling gradient
        for (int y = 0; y < screenHeight / 2; y++) {
            int shade = (int) (255 - (255.0 * y / (screenHeight / 2))); // Lighter at the top, darker toward the middle
            g.setColor(new Color(shade, shade, shade));
            g.drawLine(0, y, screenWidth, y);
        }

        // Render floor gradient
        for (int y = screenHeight / 2; y < screenHeight; y++) {
            int shade = (int) (1 + (205.0 * (y - screenHeight / 2) / (screenHeight / 2))); // Darker at the middle, lighter toward the bottom
            g.setColor(new Color(shade, shade, shade));
            g.drawLine(0, y, screenWidth, y);
        }

        // Fix raycasting loop to cover the entire screen width
        for (int x = 0; x < screenWidth; x++) { // Iterate over the screen width
            double rayAngle = playerAngle - Math.toRadians(fov / 2) + Math.toRadians(fov) * x / screenWidth; // Adjust ray angle for full screen
            double rayX = Math.cos(rayAngle);
            double rayY = Math.sin(rayAngle);
            double distance = 0;

            while (true) {
                distance += 0.001; // Increment distance for each step
                int testX = (int) (playerX + rayX * distance);
                int testY = (int) (playerY + rayY * distance);

                if (testX < 0 || testX >= mapWidth || testY < 0 || testY >= mapHeight) {
                    break;
                }
                if (map[testY][testX] > 0) {
                    // Apply cosine correction to the distance
                    double correctedDistance = distance * Math.cos(rayAngle - playerAngle);

                    int wallHeight = (int) (screenHeight / correctedDistance);
                    int drawStart = (screenHeight / 2) - (wallHeight / 2);
                    int drawEnd = drawStart + wallHeight;

                    if (wallTexture != null) {
                        int textureX = (int) ((playerX + rayX * distance) % wallTexture.getWidth());
                        for (int drawY = drawStart; drawY < drawEnd; drawY++) {
                            int textureY = (int) ((drawY - drawStart) * wallTexture.getHeight() / wallHeight);
                            int color = wallTexture.getRGB(textureX, textureY);
                            image.setRGB(x, drawY, color); // Use `x` directly for full screen rendering
                        }
                    } else {
                        // Calculate color based on distance (closer = lighter, farther = darker)
                        int shade = (int) Math.max(16, 255 - correctedDistance * 40); // Adjust multiplier for effect
                        g.setColor(new Color(shade, shade, shade));
                        g.fillRect(x, drawStart, 1, wallHeight); // Use `x` directly for full screen rendering
                    }
                    break;
                }
            }
        }

        g.dispose();
        return image;
    }
}