import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Raycaster {
    // Use full-resolution textures
    private static final double TEXTURE_SCALE = 1.0;

    private char[][] map;
    private int mapWidth, mapHeight;
    private double playerX, playerY, playerAngle;
    private int screenWidth, screenHeight;
    @SuppressWarnings("unused")
    private int fov, rayResolution;

    private static BufferedImage wallTexture;
    private static BufferedImage floorTexture;
    private static BufferedImage skyTexture;
    private BufferedImage image;
    private int[] pixels;

    private double[] offsetCos, offsetSin;
    private int prevFov = -1, prevRes = -1;

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

        // Load full-res textures once
        if (wallTexture == null) {
            try {
                File wallFile = new File("textures/brick3.jpg");
                File floorFile = new File("textures/floor.jpg");
                File skyFile = new File("textures/sky1.jpg");

                System.out.println("Loading wall texture from: " + wallFile.getAbsolutePath());
                System.out.println("Loading floor texture from: " + floorFile.getAbsolutePath());
                System.out.println("Loading sky texture from: " + skyFile.getAbsolutePath());

                BufferedImage rawWall  = ImageIO.read(wallFile);
                BufferedImage rawFloor = ImageIO.read(floorFile);
                wallTexture  = scaleTexture(rawWall, TEXTURE_SCALE);
                floorTexture = scaleTexture(rawFloor, TEXTURE_SCALE);
                skyTexture   = ImageIO.read(skyFile);
            } catch (IOException e) {
                System.err.println("❌ Texture load error:");
                e.printStackTrace();

                // Set textures to null explicitly to avoid NullPointerException
                wallTexture = null;
                floorTexture = null;
                skyTexture = null;
            }
        }

        image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    private BufferedImage scaleTexture(BufferedImage orig, double scale) {
        int w = (int) (orig.getWidth() * scale);
        int h = (int) (orig.getHeight() * scale);
        BufferedImage buff = new BufferedImage(w, h, orig.getType());
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(orig, buff);
    }

    public BufferedImage castRays() {
        Graphics2D g2d = image.createGraphics();

        // (1) High-quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw sky
        int skyH = screenHeight / 2;
        if (skyTexture != null) {
            int texW = skyTexture.getWidth();
            float offsetX = (float)((playerAngle / (2 * Math.PI)) * texW) % texW;
            if (offsetX < 0) offsetX += texW;
            TexturePaint skyPaint = new TexturePaint(skyTexture, new Rectangle((int)-offsetX, 0, texW, skyH));
            g2d.setPaint(skyPaint);
            g2d.fillRect(0, 0, screenWidth, skyH);
            g2d.setPaint(Color.BLACK);
        } else {
            g2d.setColor(new Color(135, 206, 235));
            g2d.fillRect(0, 0, screenWidth, skyH);
            g2d.setPaint(Color.BLACK);
        }

        // Prepare rotation factors
        double cosA = Math.cos(playerAngle);
        double sinA = Math.sin(playerAngle);

        // Rebuild offsets if needed
        updateSettings(fov, rayResolution);
        int count = offsetCos.length;

        // Raycast loop
        for (int i = 0; i < count; i++) {
            int x = i * rayResolution;
            double dx = offsetCos[i] * cosA - offsetSin[i] * sinA;
            double dy = offsetSin[i] * cosA + offsetCos[i] * sinA;

            // DDA initialization
            double deltaX = dx == 0 ? 1e30 : Math.abs(1 / dx);
            double deltaY = dy == 0 ? 1e30 : Math.abs(1 / dy);
            int mapX = (int) playerX, mapY = (int) playerY;
            int stepX = dx < 0 ? -1 : 1, stepY = dy < 0 ? -1 : 1;
            double sideX = dx < 0 ? (playerX - mapX) * deltaX : (mapX + 1 - playerX) * deltaX;
            double sideY = dy < 0 ? (playerY - mapY) * deltaY : (mapY + 1 - playerY) * deltaY;

            boolean hit = false;
            int side = 0;
            while (!hit) {
                if (sideX < sideY) {
                    sideX += deltaX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideY += deltaY;
                    mapY += stepY;
                    side = 1;
                }
                if (mapX < 0 || mapX >= mapWidth ||
                    mapY < 0 || mapY >= mapHeight ||
                    map[mapY][mapX] == '1') {
                    hit = true;
                }
            }

            // Distance and slice height
            double dist = (side == 0)
                        ? (mapX - playerX + (1 - stepX) / 2) / dx
                        : (mapY - playerY + (1 - stepY) / 2) / dy;
            dist = Math.max(dist, 1e-4);
            int lineH = (int)(screenHeight / dist);
            int yStart = Math.max(0, (screenHeight - lineH) / 2);
            int yEnd   = Math.min(screenHeight, (screenHeight + lineH) / 2);

            // Calculate texture X
            double wallX = ((side == 0) ? playerY + dist * dy : playerX + dist * dx) % 1.0;
            int texX = (int)(wallX * wallTexture.getWidth());

            // (2) Draw wall slice with bilinear filtering
            g2d.drawImage(
                wallTexture,
                /* dst */ x, yStart, x + rayResolution, yEnd,
                /* src */ texX, 0, texX + 1, wallTexture.getHeight(),
                null
            );

            // Draw floor slice (nearest)
            for (int y = yEnd; y < screenHeight; y++) {
                double floorDist = screenHeight / (2.0 * y - screenHeight);
                double fx = playerX + dx * floorDist;
                double fy = playerY + dy * floorDist;
                int cx = (int)fx, cy = (int)fy;
                Color floorColor;

                if (cx >= 0 && cx < mapWidth && cy >= 0 && cy < mapHeight) {
                    char t = map[cy][cx];
                    if (t == 'T') {
                        floorColor = new Color(0, 0, 255);
                    } else if (t == 'E') {
                        floorColor = new Color(255, 0, 0);
                    } else if (t == 'R') {
                        floorColor = new Color(255, 0, 255);
                    } else if (t == 'V') {
                        floorColor = Color.BLACK;
                    } else if (t == 'W') { // Win point
                        floorColor = new Color(255, 255, 0);
                    } else {
                        // Default textured floor
                        int tx = Math.min(floorTexture.getWidth() - 1, Math.max(0, (int)((fx - cx) * floorTexture.getWidth())));
                        int ty = Math.min(floorTexture.getHeight() - 1, Math.max(0, (int)((fy - cy) * floorTexture.getHeight())));
                        floorColor = new Color(floorTexture.getRGB(tx, ty));
                    }

                } else {
                    int shade = Math.max(0, Math.min(255,
                            (int)(1 + 205.0 * (y - skyH) / skyH)));
                    floorColor = new Color(shade, shade, shade);
                }

                int colF = floorColor.getRGB();
                for (int rx = 0; rx < rayResolution; rx++) {
                    int px = x + rx;
                    if (px < screenWidth) {
                        pixels[y * screenWidth + px] = colF;
                    }
                }
            }
        }

        g2d.dispose();
        return image;
    }

    // Update player position & angle
    public void updatePlayer(double x, double y, double angle) {
        this.playerX = x;
        this.playerY = y;
        this.playerAngle = angle;
    }

    // Recompute offset arrays when FOV or resolution change
    public void updateSettings(int fov, int rayResolution) {
        if (fov != prevFov || rayResolution != prevRes || offsetCos == null) {
            this.fov = fov;
            this.rayResolution = rayResolution;
            prevFov = fov;
            prevRes = rayResolution;
            int count = (screenWidth + rayResolution - 1) / rayResolution;
            offsetCos = new double[count];
            offsetSin = new double[count];
            double radFov = Math.toRadians(fov), half = radFov / 2;
            for (int i = 0; i < count; i++) {
                int px = i * rayResolution;
                double offset = -half + radFov * px / screenWidth;
                offsetCos[i] = Math.cos(offset);
                offsetSin[i] = Math.sin(offset);
            }
        }
    }
}
