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
    private String selectedBrush = "Wall"; // Default brush type

    public MapEditor(Map map, Game game) {
        this.map = map;
        this.game = game;
        setTitle("Map Editor");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
            JOptionPane.showMessageDialog(this, "Proceeding to the next step...");
            dispose(); // Close the editor
        });
        buttonPanel.add(proceedButton);

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

        public MapPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = e.getX() / tileSize;
                    int y = e.getY() / tileSize;

                    switch (selectedBrush) {
                        case "Wall":
                            paintTile(x, y, '1');
                            break;
                        case "Spawn Point":
                            if (map.getTile(x, y) != '1') {
                                if (spawnPointSet) {
                                    map.getMapLayout()[spawnY][spawnX] = '0'; // Clear previous spawn point
                                }
                                spawnX = x;
                                spawnY = y;
                                spawnPointSet = true;
                                map.getMapLayout()[y][x] = 'S'; // Mark spawn point
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
            });
        }

        private void paintTile(int x, int y, char tileType) {
            if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) {
                if (map.getTile(x, y) != tileType) {
                    map.getMapLayout()[y][x] = tileType;
                    repaint();
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    char tile = map.getTile(x, y);
                    if (tile == '1') {
                        g.setColor(Color.DARK_GRAY);
                    } else if (tile == 'S') {
                        g.setColor(Color.GREEN);
                    } else if (tile == 'T') {
                        g.setColor(Color.BLUE);
                    } else if (tile == 'E') {
                        g.setColor(Color.RED);
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);

                    g.setColor(Color.GRAY);
                    g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }
    }
}