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
    private int selectedOption = 0; // Track selected option in the options menu
    private boolean isOptionsMenuOpen = false; // Track if options menu is open
    private int targetX, targetY; // Target coordinates

    public HUD(Player player, Game game, int targetX, int targetY) {
        this.player = player;
        this.game = game;
        this.targetX = targetX;
        this.targetY = targetY;
        game.addKeyListener(this); // Add key listener for ESC key
        game.addMouseListener(this); // Add mouse listener for menu buttons
    }

    public void render(Graphics g, int fps) {
        if (isMenuOpen) {
            if (isOptionsMenuOpen) {
                renderOptionsMenu(g); // Render the options menu
            } else {
                renderMenu(g); // Render the escape menu
            }
        } else {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 20));

            // Display player position
            g.drawString("Position: (" + player.getX() + ", " + player.getY() + ")", 10, 20);
            // Display player direction
            g.drawString("Dots rendered [minimap Lidar]: " + game.dots, 10, 40);
            // Display distance to target
            double distance = player.calculateDistance(targetX, targetY);
            g.drawString("Distance to Target: " + String.format("%.2f", distance), 10, 60);

            renderFPS(g, fps); // Render FPS
        }
    }

    private void renderMenu(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(100, 100, 300, 300); // Draw menu background

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Resume", 150, 150); // Adjusted button positions
        g.drawString("Options", 150, 200);
        g.drawString("Exit to Map Selection", 150, 250);
    }

    private void renderOptionsMenu(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(100, 100, 400, 400); // Draw options menu background

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
        g.drawString("FPS: " + fps, 10, 80); // Draw FPS at the top-left corner
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            isMenuOpen = !isMenuOpen; // Toggle menu state
            game.setMouseCentered(!isMenuOpen); // Center mouse if menu is closed
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isMenuOpen) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            if (!isOptionsMenuOpen) {
                if (mouseX >= 150 && mouseX <= 300) {
                    if (mouseY >= 130 && mouseY <= 170) {
                        isMenuOpen = false;
                        game.setMouseCentered(true);
                    } else if (mouseY >= 180 && mouseY <= 220) {
                        isOptionsMenuOpen = true;
                    } else if (mouseY >= 230 && mouseY <= 270) {
                        game.exitToMapSelection();
                    }
                }
            } else {
                if (mouseX >= 150 && mouseX <= 350) {
                    if (mouseY >= 160 && mouseY <= 200) {
                        game.adjustFOV(5);
                    } else if (mouseY >= 200 && mouseY <= 240) {
                        player.adjustMoveSpeed(0.01);
                    } else if (mouseY >= 240 && mouseY <= 280) {
                        game.adjustMouseSensitivity(0.001);
                    } else if (mouseY >= 280 && mouseY <= 320) {
                        game.adjustRayResolution(10);
                    } else if (mouseY >= 320 && mouseY <= 360) {
                        isOptionsMenuOpen = false;
                    }
                }
            }
        }
    }

    // Unused KeyListener and MouseListener methods
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}