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
                map.getMapLayout(),     //  pass char[][] directly
                map.getWidth(),
                map.getHeight(),
                player.getX(),
                player.getY(),
                player.getAngle(),
                game.getWidth(),
                game.getHeight(),
                fov,
                rayResolution
        );

        BufferedImage image = raycaster.castRays();
        g.drawImage(image, 0, 0, null);
    }
}
