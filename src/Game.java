import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Game extends Canvas implements KeyListener, MouseMotionListener {
    private Player player;
    private double initialSpawnX;
    private double initialSpawnY;
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
    private int targetFOV = 60;
    private SoundManager soundManager;

    // Timer fields
    private long timerStart;
    private boolean timerStarted = false;

    // Sprint / stamina (0.0 to 1.0)
    private double stamina = 1.0;

    // Screenshake fields
    private int shakeDuration = 0; // Duration of the screenshake in frames
    private int shakeIntensity = 5; // Intensity of the screenshake

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
                        player = new Player(x + 0.5, y + 0.5, 0); // Set spawn point
                        initialSpawnX = x + 0.5;
                        initialSpawnY = y + 0.5;
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

        renderer = new Renderer(this, map, player);
        hud = new HUD(player, this);
        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);

        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }
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
        String sel = (String)JOptionPane.showInputDialog(
            null, "Select a level to load:", "Load Level",
            JOptionPane.PLAIN_MESSAGE, null, levelNames, levelNames[0]
        );
        if (sel == null) return false;
        try {
            List<String> lines = Files.readAllLines(new File(levelsDir, sel + ".txt").toPath());
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

            if (currentTile == 'T') {
                targetFOV = 120;
                shakeDuration = 20; // Trigger screenshake for 20 frames
            } else {
                targetFOV = 60;
            }

            if (currentTile == 'V') {
                JOptionPane.showMessageDialog(null, "You fell into the void. Restarting level...");
                resetLevelState();
                continue; // Continue game loop from beginning
            }


// endgame  tile
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
                lastTime = now;
            }

            long frameTime = now - frameStart;
            if (frameTime < targetFrameTime) {
                try { Thread.sleep((targetFrameTime - frameTime) / 1_000_000L); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void resetLevelState() {
        player = new Player(initialSpawnX, initialSpawnY, 0); // Reset player position and angle
        hud = new HUD(player, this);                         // Re-initialize HUD
        renderer = new Renderer(this, map, player);          // Re-initialize renderer
        timerStarted = false;                                // Reset the timer
        dots = 0;
        rayEndPoints.clear();
    }


    private void processInput() {
        // Start timer on first move
        if (!timerStarted && (keys[KeyEvent.VK_W]||keys[KeyEvent.VK_S]||
                              keys[KeyEvent.VK_A]||keys[KeyEvent.VK_D])) {
            timerStart = System.nanoTime();
            timerStarted = true;
        }

        // Sprint mechanic
        if (keys[KeyEvent.VK_SHIFT] && stamina > 0.0) {
            player.setSpeedBoost(true);
            stamina = Math.max(0.0, stamina - 0.01);
        } else {
            player.setSpeedBoost(false);
            stamina = Math.min(1.0, stamina + 0.005);
        }

        if (keys[KeyEvent.VK_W]) player.moveForward(map);
        if (keys[KeyEvent.VK_S]) player.moveBackward(map);
        if (keys[KeyEvent.VK_A]) player.strafeLeft(map);
        if (keys[KeyEvent.VK_D]) player.strafeRight(map);
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) { createBufferStrategy(3); return; }
        Graphics g = bs.getDrawGraphics();

        // Apply screenshake effect
        if (shakeDuration > 0) {
            int offsetX = (int) (Math.random() * shakeIntensity * 2 - shakeIntensity);
            int offsetY = (int) (Math.random() * shakeIntensity * 2 - shakeIntensity);
            g.translate(offsetX, offsetY);
            shakeDuration--;
        }

        renderer.render(g);
        hud.render(g, fps);
        renderMiniMap(g);
        g.dispose();
        bs.show();
    }

    private void recenterMouse() {
        if (!isMouseCentered) return;
        Point center = new Point(getWidth()/2, getHeight()/2);
        SwingUtilities.convertPointToScreen(center, this);
        robot.mouseMove(center.x, center.y);
        lastMouseX = center.x;
    }

    private void renderMiniMap(Graphics g) {
        int miniMapSize = Math.min(getWidth(), getHeight())/5;
        int tileSize = miniMapSize / map.getWidth();
        int offsetX = (getWidth()-miniMapSize)/2;
        int offsetY = getHeight()-miniMapSize-10;

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                char t = map.getTile(x, y);
                if (map.isWall(x, y)) {
                    g.setColor(Color.LIGHT_GRAY); // Set wall color to match empty fields
                    g.fillRect(offsetX + x * tileSize, offsetY + y * tileSize, tileSize, tileSize);
                    continue;
                }
                if (t == 'T') g.setColor(Color.BLUE);
                else if (t == 'E') g.setColor(Color.RED);
                else g.setColor(Color.LIGHT_GRAY);
                g.fillRect(offsetX + x * tileSize, offsetY + y * tileSize, tileSize, tileSize);
            }
        }

        int pX = offsetX + (int)(player.getX()*tileSize);
        int pY = offsetY + (int)(player.getY()*tileSize);
        g.setColor(Color.RED);
        g.fillOval(pX-3, pY-3, 6, 6);

        g.setColor(Color.YELLOW);
        for (int i=-30; i<=30; i++) {
            double angle = player.getAngle()+Math.toRadians(i);
            double dx = Math.cos(angle), dy = Math.sin(angle), dist=0;
            while (true) {
                dist+=0.1;
                int tx=(int)(player.getX()+dx*dist);
                int ty=(int)(player.getY()+dy*dist);
                if (tx<0||tx>=map.getWidth()||ty<0||ty>=map.getHeight()||map.isWall(tx,ty)) {
                    int ex=offsetX+(int)((player.getX()+dx*dist)*tileSize);
                    int ey=offsetY+(int)((player.getY()+dy*dist)*tileSize);
                    g.drawLine(pX,pY,ex,ey);
                    Point ep=new Point(ex,ey);
                    if (!rayEndPoints.contains(ep)) {
                        rayEndPoints.add(ep);
                        dots++;
                    }
                    break;
                }
            }
        }

        g.setColor(Color.BLACK);
        for (Point pt: rayEndPoints) {
            g.fillOval(pt.x-1, pt.y-1, 2, 2);
        }
    }

    public void setSpawnPoint(double x, double y) {
        if (player == null) player = new Player(x,y,0);
        else player = new Player(x,y,player.getAngle());
    }

    public void setMouseCentered(boolean centered) {
        isMouseCentered = centered;
        if (centered) recenterMouse();
    }

    public int getFOV()               { return fov; }
    public void adjustFOV(int d)      { fov = Math.max(30, Math.min(120, fov + d)); }
    public double getMouseSensitivity(){ return mouseSensitivity; }
    public void adjustMouseSensitivity(double d){
        mouseSensitivity = Math.max(0.0001, mouseSensitivity + d);
    }
    public int getRayResolution()     { return rayResolution; }
    public void adjustRayResolution(int d){
        rayResolution = Math.max(1, rayResolution + d);
    }
    public void exitToMapSelection() {
        JOptionPane.showMessageDialog(null, "Returning to map selection...");
        System.exit(0);
    }
    private void smoothFOVTransition(){
        if (fov < targetFOV) fov++;
        else if (fov > targetFOV) fov--;
    }

    /** Seconds since first movement started */
    public double getElapsedTime() {
        if (!timerStarted) return 0.0;
        return (System.nanoTime() - timerStart) / 1_000_000_000.0;
    }

    /** Expose stamina to HUD */
    public double getStamina() {
        return stamina;
    }

    @Override public void keyPressed(KeyEvent e)  { keys[e.getKeyCode()] = true; }
    @Override public void keyReleased(KeyEvent e) { keys[e.getKeyCode()] = false; }
    @Override public void keyTyped(KeyEvent e)    {}
    @Override public void mouseMoved(MouseEvent e){
        if (!isMouseCentered) return;
        int mx = e.getXOnScreen(), dx = mx - lastMouseX;
        player.rotate(dx * mouseSensitivity);
        lastMouseX = mx;
    }
    @Override public void mouseDragged(MouseEvent e) {}
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
