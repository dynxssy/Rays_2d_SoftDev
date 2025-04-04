import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class HUD {
    private Player player;

    public HUD(Player player) {
        this.player = player;
    }

    public void render(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));

        // Display player position
        g.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", 10, 20);
        // Display player direction
        g.drawString("Direction: " + player.getAngle(), 10, 40);
    }
}