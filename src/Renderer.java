import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Renderer {
    private Game game;
    private Map map;
    private Player player;

    private int fov = 60; // Default FOV
    private int rayResolution = 1; // Default ray resolution

    public Renderer(Game game, Map map, Player player) {
        this.game = game;
        this.map = map;
        this.player = player;
    }

    public void updateFOV(int newFOV) {
        this.fov = newFOV;
    }

    public void updateRayResolution(int newResolution) {
        this.rayResolution = newResolution;
    }

    public void render(Graphics g) {
        Raycaster raycaster = new Raycaster(
            convertMapToIntArray(map.getMapLayout()), 
            map.getWidth(), 
            map.getHeight(), 
            player.getX(), 
            player.getY(), 
            player.getAngle(), 
            game.getWidth(), 
            game.getHeight(),
            fov, // Pass updated FOV
            rayResolution // Pass updated ray resolution
        );

        BufferedImage image = raycaster.castRays();
        g.drawImage(image, 0, 0, null);
    }

    // Converts the character-based map layout into an integer array for raycasting
    private int[][] convertMapToIntArray(char[][] charMap) {
        int[][] intMap = new int[charMap.length][charMap[0].length];
        for (int y = 0; y < charMap.length; y++) {
            for (int x = 0; x < charMap[y].length; x++) {
                intMap[y][x] = (charMap[y][x] == '1') ? 1 : 0; // Convert '1' to 1 (wall), others to 0
            }
        }
        return intMap;
    }
}