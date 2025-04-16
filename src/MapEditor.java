import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MapEditor extends JFrame {
    private Map map;
    private Game game;
    private boolean spawnPointSet = false;
    private int spawnX = -1, spawnY = -1;
    private boolean spawnPointBrushActive = false; // Track if spawn point brush is active
    private boolean trapBrushActive = false; // Track if trap brush is active

    public MapEditor(Map map, Game game) {
        this.map = map;
        this.game = game; // Allow null for standalone editing
        setTitle("Map Editor");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        MapPanel mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save Level");
        saveButton.addActionListener(e -> saveLevel());
        buttonPanel.add(saveButton);

        JButton spawnPointButton = new JButton("Spawn Point Brush");
        spawnPointButton.addActionListener(e -> {
            spawnPointBrushActive = !spawnPointBrushActive; // Toggle the brush mode
            spawnPointButton.setText(spawnPointBrushActive ? "Brush: ON" : "Brush: OFF");
        });
        buttonPanel.add(spawnPointButton);

        JButton trapButton = new JButton("Trap Brush");
        trapButton.addActionListener(e -> {
            trapBrushActive = !trapBrushActive;
            trapButton.setText(trapBrushActive ? "Trap Brush: ON" : "Trap Brush: OFF");
        });
        buttonPanel.add(trapButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> dispose());
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public MapEditor(Map map) {
        this(map, null); // Overloaded constructor for standalone editing
    }

    private void saveLevel() {
        if (!spawnPointSet) {
            JOptionPane.showMessageDialog(this, "Set a spawn point before saving.");
            return;
        }

        String levelName = JOptionPane.showInputDialog(this, "Enter level name:");
        if (levelName == null || levelName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Level name cannot be empty.");
            return;
        }

        File levelsDir = new File("levels");
        if (!levelsDir.exists()) levelsDir.mkdir();

        File levelFile = new File(levelsDir, levelName + ".txt");
        try (FileWriter writer = new FileWriter(levelFile)) {
            char[][] layout = map.getMapLayout();
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    if (x == spawnX && y == spawnY) {
                        writer.write('P'); // Save spawn point as 'P'
                    } else {
                        writer.write(layout[y][x]);
                    }
                }
                writer.write("\n");
            }
            map.getMapLayout()[spawnY][spawnX] = '0'; // Reset spawn point tile to '0' after saving
            JOptionPane.showMessageDialog(this, "Level saved successfully!");
            dispose(); // Close the editor after saving
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save level: " + ex.getMessage());
        }
    }

    public void open() {
        setVisible(true);
    }

    public boolean isSpawnPointSet() {
        return spawnPointSet;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    private class MapPanel extends JPanel {
        private final int tileSize = 30;
        private boolean isDragging = false; // Track if the mouse is being dragged
        private boolean isErasing = false; // Track if the right mouse button is used
        private Image wallTexture = new ImageIcon("/Users/dynxsy/Rays_2d_SoftDev-1/textures/wall_stone.jpg").getImage();

        public MapPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = e.getX() / tileSize;
                    int y = e.getY() / tileSize;

                    if (spawnPointBrushActive) {
                        paintTile(x, y, 'S'); // Set spawn point
                    } else if (trapBrushActive) {
                        paintTile(x, y, 'T'); // Paint trap
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        isDragging = true;
                        isErasing = false;
                        paintTile(x, y, '1'); // Paint wall
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        isDragging = true;
                        isErasing = true;
                        paintTile(x, y, '0'); // Erase wall
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isDragging = false; // Stop dragging when the mouse is released
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging) {
                        int x = e.getX() / tileSize;
                        int y = e.getY() / tileSize;

                        if (isErasing) {
                            paintTile(x, y, '0'); // Erase wall
                        } else {
                            paintTile(x, y, '1'); // Paint wall
                        }
                    }
                }
            });
        }

        // Paints a tile on the map based on the selected brush type
        private void paintTile(int x, int y, char tileType) {
            if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) {
                if (tileType == 'S' && map.getTile(x, y) != '1') { // Set spawn point
                    if (spawnPointSet) {
                        map.getMapLayout()[spawnY][spawnX] = '0'; // Clear previous spawn point
                    }
                    spawnX = x;
                    spawnY = y;
                    spawnPointSet = true;
                } else if (tileType == 'T' && map.getTile(x, y) != '1') { // Set trap
                    map.getMapLayout()[y][x] = 'T';
                } else if (tileType == '1') { // Paint wall
                    map.getMapLayout()[y][x] = '1';
                } else if (tileType == '0') { // Erase tile
                    map.getMapLayout()[y][x] = '0';
                }
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    if (map.getTile(x, y) == '1') {
                        g.drawImage(wallTexture, x * tileSize, y * tileSize, tileSize, tileSize, this);
                    } else if (map.getTile(x, y) == 'S') {
                        g.setColor(Color.GREEN); // Spawn point tile
                        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    } else if (map.getTile(x, y) == 'T') {
                        g.setColor(Color.RED); // Trap tile
                        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    } else {
                        g.setColor(Color.LIGHT_GRAY); // Empty space
                        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    }

                    if (spawnPointSet && x == spawnX && y == spawnY) {
                        g.setColor(Color.GREEN); // Spawn point
                        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                        g.setColor(Color.BLACK);
                        g.drawString("S", x * tileSize + tileSize / 2 - 5, y * tileSize + tileSize / 2 + 5); // Draw 'S' for spawn point
                    } else {
                        g.setColor(Color.GRAY); // Grid lines
                        g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    }
                }
            }
        }
    }
}
