import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Raycaster {
    private char[][] map;
    private int mapWidth;
    private int mapHeight;
    private double playerX;
    private double playerY;
    private double playerAngle;
    private int screenWidth;
    private int screenHeight;
    private BufferedImage wallTexture;
    private BufferedImage floorTexture;
    private int fov;
    private int rayResolution;
    private BufferedImage image;

    public Raycaster(char[][] map, int mapWidth, int mapHeight, double playerX, double playerY, double playerAngle,
                     int screenWidth, int screenHeight, int fov, int rayResolution) {
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
            wallTexture = ImageIO.read(new File("textures/brick4200x.jpg"));
            floorTexture = ImageIO.read(new File("textures/floor.png"));
        } catch (IOException e) {
            System.out.println("❌ Failed to load wall texture.");
            e.printStackTrace();
        }


        image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage castRays() {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        for (int x = 0; x < screenWidth; x++) {
            double rayAngle = playerAngle - Math.toRadians(fov / 2) + Math.toRadians(fov) * x / screenWidth;
            double rayX = Math.cos(rayAngle);
            double rayY = Math.sin(rayAngle);

            double sideDistX, sideDistY;
            double deltaDistX = (rayX == 0) ? 1e30 : Math.abs(1 / rayX);
            double deltaDistY = (rayY == 0) ? 1e30 : Math.abs(1 / rayY);

            int stepX, stepY;
            int mapX = (int) playerX;
            int mapY = (int) playerY;
            int side = 0;
            boolean hit = false;

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
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }

                if (mapX < 0 || mapX >= mapWidth || mapY < 0 || mapY >= mapHeight || map[mapY][mapX] == '1') {
                    hit = true;
                }
            }

            double correctedDistance = (side == 0)
                    ? (mapX - playerX + (1 - stepX) / 2) / rayX
                    : (mapY - playerY + (1 - stepY) / 2) / rayY;

            if (correctedDistance < 0.0001) correctedDistance = 0.0001;

            double wallHeight = screenHeight / correctedDistance;
            int drawStart = (int) (screenHeight / 2 - wallHeight / 2);
            int drawEnd = (int) (screenHeight / 2 + wallHeight / 2);
            drawStart = Math.max(0, drawStart);
            drawEnd = Math.min(screenHeight, drawEnd);

            double wallX;
            if (side == 0) {
                wallX = playerY + correctedDistance * rayY;
            } else {
                wallX = playerX + correctedDistance * rayX;
            }
            wallX -= Math.floor(wallX);
            int textureX = (int) (wallX * wallTexture.getWidth());
            int floorTextureX = Math.max(0, Math.min(textureX, wallTexture.getWidth() - 1));

            for (int drawY = drawStart; drawY < drawEnd; drawY++) {
                double sampleRatio = (drawY - drawStart) / wallHeight;
                int textureY = (int) (sampleRatio * wallTexture.getHeight());
                int floorTextureY = Math.max(0, Math.min(textureY, wallTexture.getHeight() - 1));

                try {
                    int color = wallTexture.getRGB(textureX, textureY);
                    image.setRGB(x, drawY, color);
                } catch (Exception e) {
                    image.setRGB(x, drawY, new Color(255, 0, 0).getRGB());
                }
            }

            // Dynamic floor coloring
            for (int y = drawEnd; y < screenHeight; y++) {
                double floorDist = screenHeight / (2.0 * y - screenHeight);
                double floorX = playerX + rayX * floorDist;
                double floorY = playerY + rayY * floorDist;

                int cellX = (int) floorX;
                int cellY = (int) floorY;
                Color floorColor = null;
                if (cellX >= 0 && cellX < mapWidth && cellY >= 0 && cellY < mapHeight) {
                    char tile = map[cellY][cellX];
                    if (tile == 'T') {
                        floorColor = new Color(0, 0, 255); // Blue for FOV trap
                    } else if (tile == 'E') {
                        floorColor = new Color(255, 0, 0); // Red for endgame trap
                    } else {
                        // Texture for regular floor
                        double floorXOffset = floorX - cellX;
                        double floorYOffset = floorY - cellY;
                        int floorTexX = (int) (floorXOffset * wallTexture.getWidth());
                        int floorTextureY = (int) (floorYOffset * wallTexture.getHeight());
                        textureX = Math.max(0, Math.min(textureX, wallTexture.getWidth() - 1));
                        floorTextureY = Math.max(0, Math.min(floorTextureY, wallTexture.getHeight() - 1));
                        floorColor = new Color(wallTexture.getRGB(floorTexX, floorTextureY));
                    }
                }

                if (floorColor == null) {
                    int shade = (int) (1 + (205.0 * (y - screenHeight / 2) / (screenHeight / 2)));
                    shade = Math.max(0, Math.min(255, shade));
                    floorColor = new Color(shade, shade, shade);
                }

                image.setRGB(x, y, floorColor.getRGB());
            }
        }

        g.dispose();
        return image;
    }
}
