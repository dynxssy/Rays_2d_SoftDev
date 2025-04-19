import java.awt.*;
import java.awt.image.*;
import java.awt.TexturePaint;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;


public class Raycaster {
    private static final double TEXTURE_SCALE = 0.2;

    private char[][] map;
    private int mapWidth, mapHeight;
    private double playerX, playerY, playerAngle;
    private int screenWidth, screenHeight;
    private int fov, rayResolution;

    private BufferedImage wallTexture;
    private BufferedImage floorTexture;
    private BufferedImage skyTexture;
    private BufferedImage image;

    public Raycaster(char[][] map, int mapWidth, int mapHeight,
                     double playerX, double playerY, double playerAngle,
                     int screenWidth, int screenHeight,
                     int fov, int rayResolution) {
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
            BufferedImage rawWall  = ImageIO.read(new File("Rays_2d_SoftDev-main/textures/brick4200x.jpg"));
            BufferedImage rawFloor = ImageIO.read(new File("Rays_2d_SoftDev-main/textures/floor.jpg"));
            wallTexture  = scaleTexture(rawWall, TEXTURE_SCALE);
            floorTexture = scaleTexture(rawFloor, TEXTURE_SCALE);
            skyTexture   = ImageIO.read(new File("Rays_2d_SoftDev-main/textures/sky1.jpg"));
        } catch (IOException e) {
            System.err.println("❌ Texture load error:");
            e.printStackTrace();
        }

        image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
    }

    private BufferedImage scaleTexture(BufferedImage orig, double scale) {
        int w = (int)(orig.getWidth() * scale);
        int h = (int)(orig.getHeight() * scale);
        BufferedImage buff = new BufferedImage(w, h, orig.getType());
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(orig, buff);
    }

    public BufferedImage castRays() {
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draw sky using TexturePaint for easy horizontal scrolling and tiling
        int skyH = screenHeight / 2;
        if (skyTexture != null) {
            int texW = skyTexture.getWidth();
            // compute scroll offset based on player angle
            float offsetX = (float)((playerAngle / (2 * Math.PI)) * texW) % texW;
            if (offsetX < 0) offsetX += texW;
            Rectangle anchor = new Rectangle((int)-offsetX, 0, texW, skyH);
            TexturePaint skyPaint = new TexturePaint(skyTexture, anchor);
            g2d.setPaint(skyPaint);
            g2d.fillRect(0, 0, screenWidth, skyH);
            // reset paint to default
            g2d.setPaint(Color.BLACK);
        } else {
            g2d.setPaint(new Color(135, 206, 235));
            g2d.fillRect(0, 0, screenWidth, skyH);
            g2d.setPaint(Color.BLACK);
        }

        // Cache floor texture dims
        int floorTexW = (floorTexture != null ? floorTexture.getWidth() : 0);
        int floorTexH = (floorTexture != null ? floorTexture.getHeight() : 0);

        // Ray casting for walls and floors
        for (int x = 0; x < screenWidth; x += rayResolution) {
            double rayAngle = playerAngle - Math.toRadians(fov/2)
                    + Math.toRadians(fov) * x / screenWidth;
            double dx = Math.cos(rayAngle);
            double dy = Math.sin(rayAngle);

            // DDA initialization
            double deltaX = dx == 0 ? 1e30 : Math.abs(1 / dx);
            double deltaY = dy == 0 ? 1e30 : Math.abs(1 / dy);
            int mapX = (int)playerX;
            int mapY = (int)playerY;
            int stepX = dx < 0 ? -1 : 1;
            int stepY = dy < 0 ? -1 : 1;
            double sideX = dx < 0 ? (playerX - mapX) * deltaX : (mapX + 1 - playerX) * deltaX;
            double sideY = dy < 0 ? (playerY - mapY) * deltaY : (mapY + 1 - playerY) * deltaY;

            boolean hit = false;
            int side = 0;
            while (!hit) {
                if (sideX < sideY) { sideX += deltaX; mapX += stepX; side = 0; }
                else               { sideY += deltaY; mapY += stepY; side = 1; }
                if (mapX < 0 || mapX >= mapWidth
                        || mapY < 0 || mapY >= mapHeight
                        || map[mapY][mapX] == '1') hit = true;
            }

            double dist = (side == 0)
                    ? (mapX - playerX + (1 - stepX) / 2) / dx
                    : (mapY - playerY + (1 - stepY) / 2) / dy;
            dist = Math.max(dist, 1e-4);

            // Draw wall slice
            int lineH = (int)(screenHeight / dist);
            int yStart = Math.max(0, (screenHeight - lineH) / 2);
            int yEnd   = Math.min(screenHeight, (screenHeight + lineH) / 2);
            double wallX = ((side == 0) ? playerY + dist * dy : playerX + dist * dx) % 1.0;
            int texX = (int)(wallX * wallTexture.getWidth());

            for (int y = yStart; y < yEnd; y++) {
                double sample = (double)(y - yStart) / lineH;
                int texY = (int)(sample * wallTexture.getHeight());
                image.setRGB(x, y, wallTexture.getRGB(texX, texY));
            }

            // Draw floor slice
            for (int y = yEnd; y < screenHeight; y++) {
                double floorDist = screenHeight / (2.0 * y - screenHeight);
                double fx = playerX + dx * floorDist;
                double fy = playerY + dy * floorDist;
                int cx = (int)fx, cy = (int)fy;
                Color floorColor;

                if (cx >= 0 && cx < mapWidth && cy >= 0 && cy < mapHeight) {
                    char t = map[cy][cx];
                    if (t == 'T') floorColor = new Color(0, 0, 255);
                    else if (t == 'E') floorColor = new Color(255, 0, 0);
                    else if (t == 'R') floorColor = new Color(255, 0, 255);
                    else if (floorTexture != null) {
                        int tx = Math.min(floorTexW - 1, Math.max(0, (int)((fx - cx) * floorTexW)));
                        int ty = Math.min(floorTexH - 1, Math.max(0, (int)((fy - cy) * floorTexH)));
                        floorColor = new Color(floorTexture.getRGB(tx, ty));
                    } else {
                        floorColor = Color.GRAY;
                    }
                } else {
                    int shade = Math.max(0, Math.min(255,
                            (int)(1 + 205.0 * (y - screenHeight / 2) / (screenHeight / 2))));
                    floorColor = new Color(shade, shade, shade);
                }
                image.setRGB(x, y, floorColor.getRGB());
            }
        }

        g2d.dispose();
        return image;
    }
}
