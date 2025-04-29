import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.ArrayList;

public class Game extends Canvas implements KeyListener, MouseMotionListener {
    private Player player;
    private Map map;
    private Renderer renderer;
    public int dots = 0;
    private HUD hud;
    private boolean[] keys = new boolean[256]; // Track key states
    private int lastMouseX; // Track the last mouse X position
    private Robot robot; // Add Robot instance
    private List<Point> rayEndPoints = new ArrayList<>(); // Store ray end points
    private long lastTime = System.nanoTime(); // Track the last frame time
    private int frames = 0; // Count frames in the current second
    private int fps = 0; // Store the calculated FPS
    private boolean isMouseCentered = true; // Track mouse centering state
    private int fov = 60; // Default FOV
    private double mouseSensitivity = 0.001; // Default mouse sensitivity
    private int rayResolution = 1; // Default ray resolution
    private int targetFOV = 60; // Target FOV
    private SoundManager soundManager; // Used for adding a sound manager

    public Game() {
        try {
            TextureLoader.loadTextures();
        } catch (Exception e) {
            System.err.println("❌ Error loading textures. Using default textures.");
        }

        soundManager = new SoundManager();
        soundManager.playMusic("Rays_2d_SoftDev-main/sounds/background-music2.wav"); // Fixed indentation
        String[] options = {"Create New Level", "Load Existing Level"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose an option:",
                "Game Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            map = new Map(new char[20][20]); // Create a blank map
            MapEditor editor = new MapEditor(map); // Open the map editor
            editor.open();
            while (editor.isVisible()) {
                try {
                    Thread.sleep(100); // Wait for the editor to close
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (editor.isSpawnPointSet()) {
                player = new Player(editor.getSpawnX() + 0.5, editor.getSpawnY() + 0.5, 0); // Use the spawn point set in the editor
            } else {
                player = new Player(1.5, 1.5, 0); // Default spawn point if none is set
            }
        } else if (choice == 1) {
            if (!loadLevel()) {
                System.exit(0); // Exit if no level is loaded
            }
        } else {
            System.exit(0); // Exit if no option is selected
        }

        renderer = new Renderer(this, map, player);
        hud = new HUD(player, this);
        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private boolean loadLevel() {
        File levelsDir = new File("levels");
        if (!levelsDir.exists() || levelsDir.listFiles() == null) {
            JOptionPane.showMessageDialog(null, "No levels found. Create a new level first.");
            return false;
        }

        File[] levelFiles = levelsDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (levelFiles == null || levelFiles.length == 0) { // Added null/empty check for safety
            JOptionPane.showMessageDialog(null, "No levels found. Create a new level first.");
            return false;
        }

        String[] levelNames = new String[levelFiles.length];
        for (int i = 0; i < levelFiles.length; i++) {
            levelNames[i] = levelFiles[i].getName().replace(".txt", "");
        }

        String selectedLevel = (String) JOptionPane.showInputDialog(
                null,
                "Select a level to load:",
                "Load Level",
                JOptionPane.PLAIN_MESSAGE,
                null,
                levelNames,
                levelNames[0]
        );

        if (selectedLevel == null) {
            return false; // Return false if no level is selected
        }

        File levelFile = new File(levelsDir, selectedLevel + ".txt");
        try {
            List<String> lines = Files.readAllLines(levelFile.toPath());
            char[][] layout = new char[lines.size()][lines.get(0).length()];
            for (int y = 0; y < lines.size(); y++) {
                for (int x = 0; x < lines.get(y).length(); x++) {
                    char tile = lines.get(y).charAt(x);
                    if (tile == 'P') {
                        player = new Player(x + 0.5, y + 0.5, 0); // Set spawn point
                        layout[y][x] = '0'; // Replace spawn point with empty space
                    } else {
                        layout[y][x] = tile;
                    }
                }
            }
            map = new Map(layout);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load level: " + e.getMessage());
            System.exit(0);
        }
        return true;
    }

    public void start() {
        // Center the mouse cursor
        Toolkit.getDefaultToolkit().getBestCursorSize(1, 1);
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        lastMouseX = center.x;

        // Game loop
        while (true) {
            long now = System.nanoTime();
            frames++;
            if (now - lastTime >= 1_000_000_000) { // Update FPS every second
                fps = frames;
                frames = 0;
                lastTime = now;
            }

            processInput();
            player.update(map);

            char currentTile = map.getTile((int) player.getX(), (int) player.getY());

            if (currentTile == 'T') {
                targetFOV = 120;
            } else {
                targetFOV = 60;
            }

            if (currentTile == 'E') {
                JOptionPane.showMessageDialog(null, "You stepped on the ENDGAME trap!");
                System.exit(0);
            }

            smoothFOVTransition();
            render();
            recenterMouse(); // Recenter the mouse after each frame
        }
    }

    private void processInput() {
        player.setSpeedBoost(keys[KeyEvent.VK_SHIFT]); // Boost speed on shift
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
        hud.render(g, fps); // Pass FPS to HUD for rendering
        renderMiniMap(g); // Render the mini-map
        g.dispose();
        bs.show();
    }

    private void recenterMouse() {
        if (isMouseCentered) { // Only recenter the mouse if centering is enabled
            Point center = new Point(getWidth() / 2, getHeight() / 2);
            SwingUtilities.convertPointToScreen(center, this);
            robot.mouseMove(center.x, center.y); // Move mouse to the center of the window
            lastMouseX = center.x; // Update lastMouseX to prevent sudden jumps
        }
    }

    private void renderMiniMap(Graphics g) {
        int miniMapSize = Math.min(getWidth(), getHeight()) / 5; // Mini-map size is 1/5th of the smaller screen dimension
        int tileSize = miniMapSize / map.getWidth(); // Size of each tile on the mini-map
        int offsetX = (getWidth() - miniMapSize) / 2; // Center the mini-map horizontally
        int offsetY = getHeight() - miniMapSize - 10; // Position the mini-map 10px above the bottom edge

        // Draw the map
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                char tile = map.getTile(x, y);
                if (map.isWall(x, y)) {
                    g.setColor(Color.DARK_GRAY); // Wall color (added default color)
                } else if (tile == 'T') {
                    g.setColor(Color.BLUE);
                } else if (tile == 'E') {
                    g.setColor(Color.RED);
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

                    // Add the ray end point to the list if not already present
                    Point rayEndPoint = new Point(rayEndX, rayEndY);
                    if (!rayEndPoints.contains(rayEndPoint)) {
                        rayEndPoints.add(rayEndPoint);
                        dots++;
                    }
                    break;
                }
            }
        }

        // Render all stored ray end points
        g.setColor(Color.BLACK);
        for (Point point : rayEndPoints) {
            g.fillOval(point.x - 1, point.y - 1, 2, 2); // Draw a small black dot
        }
    }

    public void setSpawnPoint(double x, double y) {
        if (player == null) {
            player = new Player(x, y, 0); // Initialize player if not already set
        } else {
            player = new Player(x, y, player.getAngle()); // Update player position while keeping the angle
        }
    }

    public void setMouseCentered(boolean centered) {
        isMouseCentered = centered;
        if (centered) {
            recenterMouse(); // Recenter the mouse
        }
    }

    public int getFOV() {
        return fov;
    }

    public void adjustFOV(int delta) {
        fov = Math.max(30, Math.min(120, fov + delta)); // Clamp FOV between 30 and 120
        renderer.updateFOV(fov); // Ensure renderer updates with new FOV
    }

    public double getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void adjustMouseSensitivity(double delta) {
        mouseSensitivity = Math.max(0.0001, mouseSensitivity + delta); // Clamp sensitivity
    }

    public int getRayResolution() {
        return rayResolution;
    }

    public void adjustRayResolution(int delta) {
        rayResolution = Math.max(1, rayResolution + delta); // Clamp resolution
        renderer.updateRayResolution(rayResolution); // Ensure renderer updates with new resolution
    }

    public void exitToMapSelection() {
        // Logic to return to map selection menu
        JOptionPane.showMessageDialog(null, "Returning to map selection...");
        System.exit(0); // Placeholder for actual implementation
    }

    private void smoothFOVTransition() {
        if (fov < targetFOV) {
            fov++;
        } else if (fov > targetFOV) {
            fov--;
        }
        renderer.updateFOV(fov);
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
        if (isMouseCentered) {
            int mouseX = e.getXOnScreen(); // Use screen coordinates
            int deltaX = mouseX - lastMouseX;
            player.rotate(deltaX * mouseSensitivity); // Adjust rotation speed as needed
            lastMouseX = mouseX;
        }
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