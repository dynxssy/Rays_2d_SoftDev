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

    private void renderMenu(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(100, 100, 300, 300);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Resume", 150, 150);
        g.drawString("Options", 150, 200);
        g.drawString("Exit to Map Selection", 150, 250);
    }

    private void renderOptionsMenu(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(100, 100, 400, 400);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Options Menu", 150, 130);
        g.drawString("FOV: " + game.getFOV(), 150, 180);
        g.drawString("Walk Speed: " + player.getMoveSpeed(), 150, 220);
        g.drawString("Mouse Sensitivity: " + game.getMouseSensitivity(), 150, 260);
        g.drawString("Wall Resolution: " + game.getRayResolution(), 150, 300);
        g.drawString("Back", 150, 340);
    }

    private void renderFPS(Graphics g, int fps) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("FPS: " + fps, 10, 60);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            isMenuOpen = !isMenuOpen;
            game.setMouseCentered(!isMenuOpen);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!isMenuOpen) return;
        int mx = e.getX(), my = e.getY();
        if (!isOptionsMenuOpen) {
            if (mx >= 150 && mx <= 300) {
                if (my >= 130 && my <= 170) {
                    isMenuOpen = false;
                    game.setMouseCentered(true);
                } else if (my >= 180 && my <= 220) {
                    isOptionsMenuOpen = true;
                } else if (my >= 230 && my <= 270) {
                    game.exitToMapSelection();
                }
            }
        } else {
            if (mx >= 150 && mx <= 350) {
                if (my >= 160 && my <= 200) game.adjustFOV(5);
                else if (my >= 200 && my <= 240) player.adjustMoveSpeed(0.01);
                else if (my >= 240 && my <= 280) game.adjustMouseSensitivity(0.001);
                else if (my >= 280 && my <= 320) game.adjustRayResolution(10);
                else if (my >= 320 && my <= 360) isOptionsMenuOpen = false;
            }
        }
    }

    // Unused listener methods
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
