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
    private int rayResolution = 1;
    private int targetFOV = 60; // For trap-based FOV changes

    public Game() {
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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (editor.isSpawnPointSet()) {
                player = new Player(editor.getSpawnX() + 0.5, editor.getSpawnY() + 0.5, 0);
            } else {
                player = new Player(1.5, 1.5, 0);
            }
        } else if (choice == 1) {
            if (!loadLevel()) {
                System.exit(0);
            }
        } else {
            System.exit(0);
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
            return false;
        }

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

    public void start() {
        Toolkit.getDefaultToolkit().getBestCursorSize(1, 1);
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        lastMouseX = center.x;

        while (true) {
            long now = System.nanoTime();
            frames++;
            if (now - lastTime >= 1_000_000_000) {
                fps = frames;
                frames = 0;
                lastTime = now;
            }

            processInput();
            player.update(map);

            // Trap detection logic
            char tile = map.getTile((int) player.getX(), (int) player.getY());
            if (tile == 'T') {
                targetFOV = 120;
            } else if (tile == 'E') {
                JOptionPane.showMessageDialog(null, "You've reached the exit!");
                System.exit(0);
            } else {
                targetFOV = 60;
            }

            render();
            recenterMouse();
        }
    }

    private void processInput() {
        player.setSpeedBoost(keys[KeyEvent.VK_SHIFT]);
        if (keys[KeyEvent.VK_W]) player.moveForward(map);
        if (keys[KeyEvent.VK_S]) player.moveBackward(map);
        if (keys[KeyEvent.VK_A]) player.strafeLeft(map);
        if (keys[KeyEvent.VK_D]) player.strafeRight(map);
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
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
                if (tile == '1') continue; // skip drawing walls
                else if (tile == 'T') g.setColor(Color.BLUE);
                else if (tile == 'E') g.setColor(Color.RED);
                else g.setColor(Color.LIGHT_GRAY);

                g.fillRect(offsetX + x * tileSize, offsetY + y * tileSize, tileSize, tileSize);
            }
        }

        int playerX = (int) (offsetX + player.getX() * tileSize);
        int playerY = (int) (offsetY + player.getY() * tileSize);
        g.setColor(Color.RED);
        g.fillOval(playerX - 3, playerY - 3, 6, 6);
    }

    public void setSpawnPoint(double x, double y) {
        if (player == null) {
            player = new Player(x, y, 0);
        } else {
            player = new Player(x, y, player.getAngle());
        }
    }

    public void setMouseCentered(boolean centered) {
        isMouseCentered = centered;
        if (centered) {
            recenterMouse();
        }
    }

    public int getFOV() {
        return fov;
    }

    public void adjustFOV(int delta) {
        fov = Math.max(30, Math.min(120, fov + delta));
        renderer.updateFOV(fov);
    }

    public double getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void adjustMouseSensitivity(double delta) {
        mouseSensitivity = Math.max(0.0001, mouseSensitivity + delta);
    }

    public int getRayResolution() {
        return rayResolution;
    }

    public void adjustRayResolution(int delta) {
        rayResolution = Math.max(1, rayResolution + delta);
        renderer.updateRayResolution(rayResolution);
    }

    public void exitToMapSelection() {
        JOptionPane.showMessageDialog(null, "Returning to map selection...");
        System.exit(0);
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
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isMouseCentered) {
            int mouseX = e.getXOnScreen();
            int deltaX = mouseX - lastMouseX;
            player.rotate(deltaX * mouseSensitivity);
            lastMouseX = mouseX;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
