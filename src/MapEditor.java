import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MapEditor extends JFrame {
    private Map map;
    private Game game;
    private boolean spawnPointSet = false;
    private int spawnX = -1, spawnY = -1;
    private String selectedBrush = "Wall"; // Default brush type
    private String levelName;

    public MapEditor(Map map, Game game) {
        this.map = map;
        this.game = game;
        setTitle("Map Editor");
        setSize(600, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        MapPanel mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        // Brush chooser dropdown
        JComboBox<String> brushChooser = new JComboBox<>(new String[]{"Wall", "Spawn Point", "FOV Trap", "Endgame Trap", "Erase"});
        brushChooser.addActionListener(e -> selectedBrush = (String) brushChooser.getSelectedItem());
        buttonPanel.add(new JLabel("Brush:"));
        buttonPanel.add(brushChooser);

        // Save button
        JButton saveButton = new JButton("Save Level");
        saveButton.addActionListener(e -> saveLevel());
        buttonPanel.add(saveButton);

        // Proceed button
        JButton proceedButton = new JButton("Proceed");
        proceedButton.addActionListener(e -> {
            // You can add additional logic here if needed before closing
            dispose();
        });
        buttonPanel.add(proceedButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public MapEditor(Map map) {
        this(map, null);
    }

    public String getLevelName() {
        return levelName;
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

    public void open() {
        setVisible(true);
    }

    private void saveLevel() {
        if (!spawnPointSet) {
            JOptionPane.showMessageDialog(this, "Set a spawn point before saving.");
            return;
        }

        levelName = JOptionPane.showInputDialog(this, "Enter level name:");
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
                        writer.write('P');
                    } else {
                        writer.write(layout[y][x]);
                    }
                }
                writer.write("\n");
            }
            // Reset spawn marker in layout
            map.getMapLayout()[spawnY][spawnX] = '0';
            JOptionPane.showMessageDialog(this, "Level saved successfully!");
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save level: " + ex.getMessage());
        }
    }

    private class MapPanel extends JPanel {
        private final int tileSize = 30;
        private boolean isDragging = false;
        private boolean isErasing = false;

        public MapPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = e.getX() / tileSize;
                    int y = e.getY() / tileSize;
                    handleBrush(x, y);
                    // Prepare for drag painting on wall/erase
                    if ("Wall".equals(selectedBrush)) {
                        isDragging = true;
                        isErasing = false;
                    } else if ("Erase".equals(selectedBrush)) {
                        isDragging = true;
                        isErasing = true;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isDragging = false;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!isDragging) return;
                    int x = e.getX() / tileSize;
                    int y = e.getY() / tileSize;
                    if (isErasing) {
                        paintTile(x, y, '0');
                    } else {
                        paintTile(x, y, '1');
                    }
                }
            });
        }

        private void handleBrush(int x, int y) {
            if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight()) return;
            switch (selectedBrush) {
                case "Wall":
                    paintTile(x, y, '1');
                    break;
                case "Spawn Point":
                    if (map.getTile(x, y) != '1') {
                        if (spawnPointSet) {
                            map.getMapLayout()[spawnY][spawnX] = '0';
                        }
                        spawnX = x;
                        spawnY = y;
                        spawnPointSet = true;
                        map.getMapLayout()[y][x] = 'S';
                        if (game != null) {
                            game.setSpawnPoint(x + 0.5, y + 0.5);
                        }
                        repaint();
                    }
                    break;
                case "FOV Trap":
                    paintTile(x, y, 'T');
                    break;
                case "Endgame Trap":
                    paintTile(x, y, 'E');
                    break;
                case "Erase":
                    paintTile(x, y, '0');
                    break;
            }
        }

        private void paintTile(int x, int y, char tileType) {
            if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight() && map.getTile(x, y) != tileType) {
                map.getMapLayout()[y][x] = tileType;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    char tile = map.getTile(x, y);
                    switch (tile) {
                        case '1': g.setColor(Color.DARK_GRAY); break;
                        case 'S': g.setColor(Color.GREEN); break;
                        case 'T': g.setColor(Color.BLUE); break;
                        case 'E': g.setColor(Color.RED); break;
                        default:  g.setColor(Color.LIGHT_GRAY); break;
                    }
                    g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    g.setColor(Color.GRAY);
                    g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }
    }
}
