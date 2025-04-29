import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Renderer {
    private Game game;
    private Map map;
    private Player player;
    private Raycaster raycaster;
    private int lastWidth = 0, lastHeight = 0;

    public Renderer(Game game, Map map, Player player) {
        this.game = game;
        this.map = map;
        this.player = player;
    }

    public void render(Graphics g) {
        // Ensure raycaster is created after canvas is laid out and size is non-zero
        int w = game.getWidth();
        int h = game.getHeight();
        if (raycaster == null || w != lastWidth || h != lastHeight) {
            raycaster = new Raycaster(
                    map.getMapLayout(), map.getWidth(), map.getHeight(),
                    player.getX(), player.getY(), player.getAngle(),
                    w, h, game.getFOV(), game.getRayResolution()
            );
            lastWidth = w;
            lastHeight = h;
        }
        // Update raycaster with latest player and settings
        raycaster.updatePlayer(player.getX(), player.getY(), player.getAngle());
        raycaster.updateSettings(game.getFOV(), game.getRayResolution());
        BufferedImage image = raycaster.castRays();
        g.drawImage(image, 0, 0, null);
    }
}
