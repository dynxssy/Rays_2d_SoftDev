import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    private BufferedImage image;

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
            wallTexture = ImageIO.read(new File("Rays_2d_SoftDev-main/textures/brick.jpg")); // 128x128 recommended
            System.out.println("✅ Texture loaded.");
        } catch (IOException e) {
            System.out.println("❌ Failed to load wall texture.");
            e.printStackTrace();
        }

        image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage castRays() {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Ceiling
        for (int y = 0; y < screenHeight / 2; y++) {
            int shade = (int) (255 - (255.0 * y / (screenHeight / 2)));
            g.setColor(new Color(shade, shade, shade));
            g.drawLine(0, y, screenWidth, y);
        }

        // Floor
        for (int y = screenHeight / 2; y < screenHeight; y++) {
            int shade = (int) (1 + (205.0 * (y - screenHeight / 2) / (screenHeight / 2)));
            g.setColor(new Color(shade, shade, shade));
            g.drawLine(0, y, screenWidth, y);
        }

        // Raycasting
        for (int x = 0; x < screenWidth; x++) {
            double rayAngle = playerAngle - Math.toRadians(fov / 2) + Math.toRadians(fov) * x / screenWidth;
            double rayX = Math.cos(rayAngle);
            double rayY = Math.sin(rayAngle);
            double distance = 0;

            // DDA Algorithm for raycasting
            double sideDistX, sideDistY;
            double deltaDistX = (rayX == 0) ? 1e30 : Math.abs(1 / rayX);
            double deltaDistY = (rayY == 0) ? 1e30 : Math.abs(1 / rayY);

            int stepX, stepY;
            boolean hit = false;
            int side = 0; // 0 = x-axis, 1 = y-axis
            int mapX = (int) playerX;
            int mapY = (int) playerY;

            if (rayX < 0) {
                stepX = -1;
                sideDistX = (playerX - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - playerX) * deltaDistX;
            }

            if (rayY < 0) {
                stepY = -1;
                sideDistY = (playerY - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - playerY) * deltaDistY;
            }

            while (!hit) {
                // Step in the x-direction or y-direction
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0; // x-side hit
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1; // y-side hit
                }

                if (mapX < 0 || mapX >= mapWidth || mapY < 0 || mapY >= mapHeight || map[mapY][mapX] > 0) {
                    hit = true;
                }
            }

            // Calculate the perpendicular wall distance
            double correctedDistance = (side == 0) ? (mapX - playerX + (1 - stepX) / 2) / rayX : (mapY - playerY + (1 - stepY) / 2) / rayY;

            if (correctedDistance < 0.0001) correctedDistance = 0.0001;

            double wallHeight = screenHeight / correctedDistance;
            int drawStart = (int) (screenHeight / 2 - wallHeight / 2);
            int drawEnd = (int) (screenHeight / 2 + wallHeight / 2);

            drawStart = Math.max(0, drawStart);
            drawEnd = Math.min(screenHeight, drawEnd);

            // Correct texture mapping based on side hit
            double wallX;
            boolean isVerticalHit = (side == 0);
            if (side == 0) {
                wallX = playerY + correctedDistance * rayY;
            } else {
                wallX = playerX + correctedDistance * rayX;
            }
            wallX -= Math.floor(wallX); // Wall position in texture coordinates

            int textureX = (int) (wallX * wallTexture.getWidth());
            if (isVerticalHit && rayX > 0) {
                textureX = wallTexture.getWidth() - textureX - 1;
            }
            if (!isVerticalHit && rayY < 0) {
                textureX = wallTexture.getWidth() - textureX - 1;
            }

            textureX = Math.max(0, Math.min(textureX, wallTexture.getWidth() - 1));

            // Render the texture on the wall
            for (int drawY = drawStart; drawY < drawEnd; drawY++) {
                double sampleRatio = (drawY - drawStart) / wallHeight;
                int textureY = (int) (sampleRatio * wallTexture.getHeight());
                textureY = Math.max(0, Math.min(textureY, wallTexture.getHeight() - 1));

                try {
                    int color = wallTexture.getRGB(textureX, textureY);
                    if (x >= 0 && x < screenWidth && drawY >= 0 && drawY < screenHeight) {
                        image.setRGB(x, drawY, color); // Apply texture color to the wall
                    }
                } catch (Exception e) {
                    if (x >= 0 && x < screenWidth && drawY >= 0 && drawY < screenHeight) {
                        image.setRGB(x, drawY, new Color(255, 0, 0).getRGB()); // Fallback color
                    }
                }
            }
        }

        g.dispose();
        return image;
    }
}

