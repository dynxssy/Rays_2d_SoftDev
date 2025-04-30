// HUD.java
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.*;

public class HUD implements KeyListener, MouseListener {
    private final Player player;
    private final Game game;
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
            if (isOptionsMenuOpen) {
                renderOptionsMenu(g);
            } else {
                renderMenu(g);
            }
        } else {
            // ● Tiny stamina bar
            int barW = 80, barH = 6, bx = 10, by = 10;
            double ratio = player.getStaminaRatio();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(bx, by, barW, barH);
            g.setColor(Color.GREEN);
            g.fillRect(bx, by, (int)(barW * ratio), barH);
            g.setColor(Color.BLACK);
            g.drawRect(bx, by, barW, barH);

            // ● Position & dots
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", 10, 30);
            g.drawString("Dots [minimap Lidar]: " + game.dots, 10, 50);

            // ● FPS
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("FPS: " + fps, 10, 70);

            // ● Elapsed time
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Time: " + String.format("%.2f", game.getElapsedTime()) + "s", 10, 90);

            // ● Crosshair
            int cx = game.getWidth()/2, cy = game.getHeight()/2, len = 8;
            g.setColor(Color.RED);
            g.drawLine(cx - len, cy, cx + len, cy);
            g.drawLine(cx, cy - len, cx, cy + len);
        }
    }

    private void renderMenu(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(100, 100, 300, 250);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Resume",               150, 150);
        g.drawString("Options",              150, 200);
        g.drawString("Exit to Map Selection",150, 250);
    }

    private void renderOptionsMenu(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(100, 100, 300, 320);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Options Menu",                  140, 130);
        g.drawString("FOV: " + game.getFOV(),         140, 180);
        g.drawString("Mouse Sensitivity: " + game.getMouseSensitivity(), 140, 220);
        g.drawString("Wall Resolution: " + game.getRayResolution(),      140, 260);
        g.drawString("Back",                          140, 300);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                isMenuOpen = !isMenuOpen;
                game.setMouseCentered(!isMenuOpen);
                break;
            case KeyEvent.VK_SHIFT:
                player.setSpeedBoost(true);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            player.setSpeedBoost(false);
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!isMenuOpen) return;
        int x = e.getX(), y = e.getY();

        if (!isOptionsMenuOpen) {
            // Main menu regions
            if (x >= 100 && x <= 400) {
                if (y >= 130 && y <= 170) {
                    // Resume
                    isMenuOpen = false;
                    game.setMouseCentered(true);
                } else if (y >= 180 && y <= 220) {
                    // Open Options
                    isOptionsMenuOpen = true;
                } else if (y >= 230 && y <= 270) {
                    // Exit
                    game.exitToMapSelection();
                }
            }
        } else {
            // Options submenu regions
            if (x >= 100 && x <= 400) {
                if (y >= 160 && y <= 200) {
                    // Adjust FOV
                    game.adjustFOV(5);
                } else if (y >= 200 && y <= 240) {
                    // Adjust Mouse Sensitivity
                    game.adjustMouseSensitivity(0.001);
                } else if (y >= 240 && y <= 280) {
                    // Adjust Wall Resolution
                    game.adjustRayResolution(10);
                } else if (y >= 280 && y <= 320) {
                    // Back to main menu
                    isOptionsMenuOpen = false;
                }
            }
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
