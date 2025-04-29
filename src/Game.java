// Game.java
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
    private boolean[] keys = new boolean[256];
    private int lastMouseX;
    private Robot robot;
    private List<Point> rayEndPoints = new ArrayList<>();
    private long lastTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;
    private boolean isMouseCentered = true;
    private int fov = 60;
    private double mouseSensitivity = 0.001;
    private int rayResolution = 2;
    private int targetFOV = 60;
    private SoundManager soundManager;

    // Timer fields
    private long timerStart;
    private boolean timerStarted = false;

    public Game() {
        soundManager = new SoundManager();
        soundManager.playMusic("sounds/background-music2.wav");
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
            map = new Map(new char[20][20]);
            MapEditor editor = new MapEditor(map);
            editor.open();
            while (editor.isVisible()) {
                try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
            }
            if (editor.isSpawnPointSet()) {
                player = new Player(editor.getSpawnX() + 0.5, editor.getSpawnY() + 0.5, 0);
            } else {
                player = new Player(1.5, 1.5, 0);
            }
        } else if (choice == 1) {
            if (!loadLevel()) System.exit(0);
        } else {
            System.exit(0);
        }

        renderer = new Renderer(this, map, player);
        hud = new HUD(player, this);
        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);

        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }
    }

    public Game(String levelName) {
        soundManager = new SoundManager();
        soundManager.playMusic("sounds/background-music2.wav");

        File levelFile = new File("levels", levelName + ".txt");
        try {
            List<String> lines = Files.readAllLines(levelFile.toPath());
            char[][] layout = new char[lines.size()][lines.get(0).length()];
            for (int y = 0; y < lines.size(); y++) {
                for (int x = 0; x < lines.get(y).length(); x++) {
                    char tile = lines.get(y).charAt(x);
                    if (tile == 'P') {
                        player = new Player(x + 0.5, y + 0.5, 0);
                        layout[y][x] = '0';
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

        keys = new boolean[256];
        renderer = new Renderer(this, map, player);
        hud = new HUD(player, this);
        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);

        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }

        System.out.println("Initializing game with level: " + levelName);
        System.out.println("Player spawn point: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Map dimensions: " + map.getWidth() + "x" + map.getHeight());
    }

    private boolean loadLevel() {
        File levelsDir = new File("levels");
        if (!levelsDir.exists() || levelsDir.listFiles() == null) {
            JOptionPane.showMessageDialog(null, "No levels found. Create a new level first.");
            return false;
        }

        File[] levelFiles = levelsDir.listFiles((dir, name) -> name.endsWith(".txt"));
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

        if (selectedLevel == null) return false;

        File levelFile = new File(levelsDir, selectedLevel + ".txt");
        try {
            List<String> lines = Files.readAllLines(levelFile.toPath());
            char[][] layout = new char[lines.size()][lines.get(0).length()];
            for (int y = 0; y < lines.size(); y++) {
                for (int x = 0; x < lines.get(y).length(); x++) {
                    char tile = lines.get(y).charAt(x);
                    if (tile == 'P') {
                        player = new Player(x + 0.5, y + 0.5, 0);
                        layout[y][x] = '0';
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

    public double start() {
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        lastMouseX = center.x;
        final long targetFrameTime = 1_000_000_000L / 60;
        lastTime = System.nanoTime();
        frames = 0;

        while (true) {
            long frameStart = System.nanoTime();
            processInput();
            player.update(map);

            char currentTile = map.getTile((int) player.getX(), (int) player.getY());
            targetFOV = (currentTile == 'T') ? 120 : 60;

            if (currentTile == 'E') {
                long endTime = System.nanoTime();
                double elapsedSec = (endTime - timerStart) / 1_000_000_000.0;
                String timeStr = String.format("%.2f", elapsedSec);
                JOptionPane.showMessageDialog(null, "Level completed in " + timeStr + " seconds!");
                return elapsedSec;
            }

            smoothFOVTransition();
            render();
            recenterMouse();

            long now = System.nanoTime();
            frames++;
            if (now - lastTime >= 1_000_000_000L) {
                fps = frames;
                frames = 0;
                int targetFpsLow = 50, targetFpsHigh = 58;
                if (fps < targetFpsLow) adjustRayResolution(1);
                else if (fps > targetFpsHigh) adjustRayResolution(-1);
                lastTime = now;
            }

            long frameTime = now - frameStart;
            if (frameTime < targetFrameTime) {
                try { Thread.sleep((targetFrameTime - frameTime) / 1_000_000L); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void processInput() {
        // Start timer on first movement
        if (!timerStarted && (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_S] ||
                              keys[KeyEvent.VK_A] || keys[KeyEvent.VK_D])) {
            timerStart = System.nanoTime();
            timerStarted = true;
            System.out.println("Timer started.");
        }

        player.setSpeedBoost(keys[KeyEvent.VK_SHIFT]);
        if (keys[KeyEvent.VK_W]) player.moveForward(map);
        if (keys[KeyEvent.VK_S]) player.moveBackward(map);
        if (keys[KeyEvent.VK_A]) player.strafeLeft(map);
        if (keys[KeyEvent.VK_D]) player.strafeRight(map);
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) { createBufferStrategy(3); return; }
        Graphics g = bs.getDrawGraphics();
        renderer.render(g);
        hud.render(g, fps);
        renderMiniMap(g);
        g.dispose();
        bs.show();
    }

    private void recenterMouse() {
        if (isMouseCentered) {
            Point center = new Point(getWidth() / 2, getHeight() / 2);
            SwingUtilities.convertPointToScreen(center, this);
            robot.mouseMove(center.x, center.y);
            lastMouseX = center.x;
        }
    }

    private void renderMiniMap(Graphics g) {
        int miniMapSize = Math.min(getWidth(), getHeight()) / 5;
        int tileSize = miniMapSize / map.getWidth();
        int offsetX = (getWidth() - miniMapSize) / 2;
        int offsetY = getHeight() - miniMapSize - 10;

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                char tile = map.getTile(x, y);
                if (map.isWall(x, y)) {
                    // skip
                } else if (tile == 'T') g.setColor(Color.BLUE);
                else if (tile == 'E') g.setColor(Color.RED);
                else g.setColor(Color.LIGHT_GRAY);
                g.fillRect(offsetX + x * tileSize, offsetY + y * tileSize, tileSize, tileSize);
            }
        }

        int pX = offsetX + (int)(player.getX() * tileSize);
        int pY = offsetY + (int)(player.getY() * tileSize);
        g.setColor(Color.RED);
        g.fillOval(pX - 3, pY - 3, 6, 6);

        g.setColor(Color.YELLOW);
        for (int i = -30; i <= 30; i++) {
            double rayAngle = player.getAngle() + Math.toRadians(i);
            double rx = Math.cos(rayAngle), ry = Math.sin(rayAngle);
            double dist = 0;
            while (true) {
                dist += 0.1;
                int tx = (int)(player.getX() + rx * dist);
                int ty = (int)(player.getY() + ry * dist);
                if (tx < 0 || tx >= map.getWidth() || ty < 0 || ty >= map.getHeight() || map.isWall(tx, ty)) {
                    int endX = (int)(offsetX + (player.getX() + rx * dist) * tileSize);
                    int endY = (int)(offsetY + (player.getY() + ry * dist) * tileSize);
                    g.drawLine(pX, pY, endX, endY);
                    Point ep = new Point(endX, endY);
                    if (!rayEndPoints.contains(ep)) {
                        rayEndPoints.add(ep);
                        dots++;
                    }
                    break;
                }
            }
        }

        g.setColor(Color.BLACK);
        for (Point pt : rayEndPoints) {
            g.fillOval(pt.x - 1, pt.y - 1, 2, 2);
        }
    }

    public void setSpawnPoint(double x, double y) {
        if (player == null) player = new Player(x, y, 0);
        else player = new Player(x, y, player.getAngle());
    }

    public void setMouseCentered(boolean centered) {
        isMouseCentered = centered;
        if (centered) recenterMouse();
    }

    public int getFOV() { return fov; }
    public void adjustFOV(int delta) { fov = Math.max(30, Math.min(120, fov + delta)); }
    public double getMouseSensitivity() { return mouseSensitivity; }
    public void adjustMouseSensitivity(double delta) {
        mouseSensitivity = Math.max(0.0001, mouseSensitivity + delta);
    }
    public int getRayResolution() { return rayResolution; }
    public void adjustRayResolution(int delta) {
        rayResolution = Math.max(1, rayResolution + delta);
    }
    public void exitToMapSelection() {
        JOptionPane.showMessageDialog(null, "Returning to map selection...");
        System.exit(0);
    }
    private void smoothFOVTransition() {
        if (fov < targetFOV) fov++;
        else if (fov > targetFOV) fov--;
    }

    /** NEW: elapsed seconds since first move, 0 if not started */
    public double getElapsedTime() {
        if (!timerStarted) return 0.0;
        long now = System.nanoTime();
        return (now - timerStart) / 1_000_000_000.0;
    }

    @Override
    public void keyPressed(KeyEvent e) { keys[e.getKeyCode()] = true; }
    @Override
    public void keyReleased(KeyEvent e) { keys[e.getKeyCode()] = false; }
    @Override public void keyTyped(KeyEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {
        if (isMouseCentered) {
            int mouseX = e.getXOnScreen();
            int deltaX = mouseX - lastMouseX;
            player.rotate(deltaX * mouseSensitivity);
            lastMouseX = mouseX;
        }
    }
    @Override public void mouseDragged(MouseEvent e) {}
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}