import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Raycaster {
    private int[][] map;
    private int mapWidth;
    private int mapHeight;
    private double playerX;
    private double playerY;
    private double playerAngle;
    private int screenWidth;
    private int screenHeight;

    public Raycaster(int[][] map, int mapWidth, int mapHeight, double playerX, double playerY, double playerAngle, int screenWidth, int screenHeight) {
        this.map = map;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerAngle = playerAngle;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public BufferedImage castRays() {
        BufferedImage image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        for (int x = 0; x < screenWidth * 2; x++) { // Double the number of rays
            double rayAngle = playerAngle - Math.PI / 6 + (Math.PI / 3) * x / (screenWidth * 2);
            double rayX = Math.cos(rayAngle);
            double rayY = Math.sin(rayAngle);
            double distance = 0;

            while (true) {
                distance += 0.01; // Increment distance for each step
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

                    // Calculate color based on distance (closer = lighter, farther = darker)
                    int shade = (int) Math.max(0, 255 - correctedDistance * 50); // Adjust multiplier for effect
                    g.setColor(new Color(shade, shade, shade));
                    g.fillRect(x / 2, drawStart, 1, wallHeight); // Scale down to fit screen width
                    break;
                }
            }
        }

        g.dispose();
        return image;
    }
}