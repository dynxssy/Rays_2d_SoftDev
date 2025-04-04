import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import javax.swing.SwingUtilities;

public class Game extends Canvas implements KeyListener, MouseMotionListener {
    private Player player;
    private Map map;
    private Renderer renderer;
    private HUD hud;
    private boolean[] keys = new boolean[256]; // Track key states
    private int lastMouseX; // Track the last mouse X position
    private Robot robot; // Add Robot instance

    public Game() {
        player = new Player(1.5, 1.5, 0); // Example initial position and angle
        map = new Map(new char[][]{
            {'1', '1', '1', '1', '1'},
            {'1', '0', '0', '0', '1'},
            {'1', '0', '1', '0', '1'},
            {'1', '0', '0', '0', '1'},
            {'1', '1', '1', '1', '1'}
        });
        renderer = new Renderer(this, map, player); // Pass map and player to Renderer
        hud = new HUD(player);

        addKeyListener(this); // Add KeyListener to the game
        addMouseMotionListener(this); // Add MouseMotionListener to the game
        setFocusable(true);

        try {
            robot = new Robot(); // Initialize Robot
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // Center the mouse cursor
        Toolkit.getDefaultToolkit().getBestCursorSize(1, 1);
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        lastMouseX = center.x;

        // Game loop
        while (true) {
            processInput();
            player.update(map);
            render();
            recenterMouse(); // Recenter the mouse after each frame
        }
    }

    private void processInput() {
        if (keys[KeyEvent.VK_W]) player.moveForward(map);
        if (keys[KeyEvent.VK_S]) player.moveBackward(map);
        if (keys[KeyEvent.VK_A]) player.strafeLeft(map); // Strafe left
        if (keys[KeyEvent.VK_D]) player.strafeRight(map); // Strafe right
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        renderer.render(g);
        hud.render(g);
        renderMiniMap(g); // Render the mini-map
        g.dispose();
        bs.show();
    }

    private void recenterMouse() {
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        SwingUtilities.convertPointToScreen(center, this);
        robot.mouseMove(center.x, center.y); // Move mouse to the center of the window
        lastMouseX = center.x; // Update lastMouseX to prevent sudden jumps
    }

    private void renderMiniMap(Graphics g) {
        int miniMapSize = 150; // Size of the mini-map
        int tileSize = miniMapSize / map.getWidth(); // Size of each tile on the mini-map
        int offsetX = 10; // X offset for the mini-map
        int offsetY = 10; // Y offset for the mini-map

        // Draw the map
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.isWall(x, y)) {
                    g.setColor(Color.DARK_GRAY); // Wall color
                } else {
                    g.setColor(Color.LIGHT_GRAY); // Floor color
                }
                g.fillRect(offsetX + x * tileSize, offsetY + y * tileSize, tileSize, tileSize);
            }
        }

        // Draw the player
        int playerX = (int) (offsetX + player.getX() * tileSize);
        int playerY = (int) (offsetY + player.getY() * tileSize);
        g.setColor(Color.RED);
        g.fillOval(playerX - 3, playerY - 3, 6, 6); // Player marker

        // Draw the field of view (FOV) rays
        g.setColor(Color.YELLOW);
        for (int i = -30; i <= 30; i++) { // Cast rays within FOV (-30 to +30 degrees)
            double rayAngle = player.getAngle() + Math.toRadians(i);
            double rayX = Math.cos(rayAngle);
            double rayY = Math.sin(rayAngle);
            double distance = 0;

            while (true) {
                distance += 0.1; // Increment distance for each step
                int testX = (int) (player.getX() + rayX * distance);
                int testY = (int) (player.getY() + rayY * distance);

                if (testX < 0 || testX >= map.getWidth() || testY < 0 || testY >= map.getHeight() || map.isWall(testX, testY)) {
                    // Stop the ray at the wall or boundary
                    int rayEndX = (int) (offsetX + (player.getX() + rayX * distance) * tileSize);
                    int rayEndY = (int) (offsetY + (player.getY() + rayY * distance) * tileSize);
                    g.drawLine(playerX, playerY, rayEndX, rayEndY);
                    break;
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getXOnScreen(); // Use screen coordinates
        int deltaX = mouseX - lastMouseX;
        player.rotate(deltaX * 0.001); // Adjust rotation speed as needed
        lastMouseX = mouseX;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Not used
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}