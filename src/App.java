import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;

public class App {
    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("2D Map");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 400);
            frame.add(new MapPanel());
            frame.setVisible(true);
        });
    }
}

class MapPanel extends JPanel {
    private static int SIZE; // Size of the grid
    private static boolean DrawGrid; // Flag to determine if grid lines should be drawn
    private int[][] grid; // 2D array representing the grid

    // Static block to load configuration properties
    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
            SIZE = Integer.parseInt(properties.getProperty("grid.size"));
            DrawGrid = Boolean.parseBoolean(properties.getProperty("draw.grid"));
        } catch (IOException e) {
            e.printStackTrace();
            SIZE = 10; // default value
            DrawGrid = true; // default value
        }
    }

    // Constructor to initialize the grid and generate a random grid
    public MapPanel() {
        grid = new int[SIZE][SIZE];
        generateRandomGrid();
    }

    // Method to generate a random grid with values 0 (white) and 1 (black)
    private void generateRandomGrid() {
        Random random = new Random();
        do {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    grid[i][j] = random.nextInt(2); // Random values between 0 and 1
                }
            }
        } while (!isGridConnected()); // Ensure the grid is connected
    }

    // Method to check if the grid is connected
    private boolean isGridConnected() {
        boolean[][] visited = new boolean[SIZE][SIZE];
        Queue<int[]> queue = new LinkedList<>();
        int[] start = findStartPoint();
        if (start == null) return false;

        queue.add(start);
        visited[start[0]][start[1]] = true;
        int reachableCells = 1;

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int[] dir : directions) {
                int newRow = cell[0] + dir[0];
                int newCol = cell[1] + dir[1];
                if (newRow >= 0 && newRow < SIZE && newCol >= 0 && newCol < SIZE && !visited[newRow][newCol] && grid[newRow][newCol] == 0) {
                    queue.add(new int[]{newRow, newCol});
                    visited[newRow][newCol] = true;
                    reachableCells++;
                }
            }
        }

        int totalEmptyCells = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 0) totalEmptyCells++;
            }
        }

        return reachableCells == totalEmptyCells;
    }

    // Method to find a starting point in the grid (a cell with value 0)
    private int[] findStartPoint() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    // Override paintComponent to draw the grid
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellSize = Math.min(getWidth(), getHeight()) / SIZE;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                }
                if (DrawGrid) {
                    g.setColor(Color.GRAY);
                    g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);
                }
            }
        }
    }
}