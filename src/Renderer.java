import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Renderer {
    private Game game;
    private Map map;
    private Player player;

    public Renderer(Game game, Map map, Player player) {
        this.game = game;
        this.map = map;
        this.player = player;
    }

    public void render(Graphics g) {
        Raycaster raycaster = new Raycaster(
                map.getMapLayout(),
                map.getWidth(),
                map.getHeight(),
                player.getX(),
                player.getY(),
                player.getAngle(),
                game.getWidth(),
                game.getHeight(),
                game.getFOV(),
                game.getRayResolution()
        );

        BufferedImage image = raycaster.castRays();
        g.drawImage(image, 0, 0, null);
    }
}
