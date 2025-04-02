// This file defines the tile-based map structure. It includes methods to load the map layout, check for wall collisions, and provide information about the map tiles.

import java.util.ArrayList;
import java.util.List;

public class Map {
    private final char[][] mapLayout;
    private final int width;
    private final int height;

    public Map(char[][] layout) {
        this.mapLayout = layout;
        this.width = layout[0].length;
        this.height = layout.length;
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true; // Treat out-of-bounds as walls
        }
        return mapLayout[y][x] == '1'; // Assuming '1' represents a wall
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<int[]> getExitPoints() {
        List<int[]> exits = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (mapLayout[y][x] == 'E') { // Assuming 'E' represents an exit
                    exits.add(new int[]{x, y});
                }
            }
        }
        return exits;
    }

    public char[][] getMapLayout() {
        return mapLayout;
    }

    public char getTile(int x, int y) {
        return mapLayout[y][x];
    }
}