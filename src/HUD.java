import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class HUD implements KeyListener, MouseListener {
    private Player player;
    private Game game;
    private boolean isMenuOpen = false;
    private boolean isOptionsMenuOpen = false;

    public HUD(Player player, Game game) {
        this.player = player;
        this.game = game;
        game.addKeyListener(this);
        game.addMouseListener(this);
    }

    public void render(Graphics g, int fps) {
        if (isMenuOpen) {
            if (isOptionsMenuOpen) renderOptionsMenu(g);
            else renderMenu(g);
        } else {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 20));

            // Player info
            g.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", 10, 20);
            g.drawString("Dots rendered [minimap Lidar]: " + game.dots, 10, 40);

            // FPS
            renderFPS(g, fps);  // at y=60

            // *** NEW: elapsed time display ***
            g.drawString(
                "Time: " + String.format("%.2f", game.getElapsedTime()) + "s",
                10, 80
            );
        }
    }

 
