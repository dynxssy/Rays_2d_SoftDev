import javax.swing.*;


public class Main {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        while (true) {
            String[] options = {"Start New Game", "Settings", "Exit"};
            int choice = JOptionPane.showOptionDialog(
                null,
                "Main Menu",
                "Java Raycaster Game",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == 0) {
                // Start new game
                runGame();
            } else if (choice == 1) {
                // Settings
                showSettings();
            } else if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
                // Exit
                System.exit(0);
            }
        }
    }

    private static void runGame() {
        JFrame frame = new JFrame("Java Raycaster Game");
        TextureLoader.loadTextures();

        double player1Time = 0;
        double player2Time = 0;

        // Player 1 creates a level
        JOptionPane.showMessageDialog(null, "Player 1: Create a level.");
        Map map1 = new Map(new char[20][20]);
        MapEditor editor1 = new MapEditor(map1);
        editor1.open();
        while (editor1.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String level1Name = editor1.getLevelName();

        // Player 2 plays the level
        JOptionPane.showMessageDialog(null, "Player 1: Pass the PC to Player 2.");
        Game game1 = new Game(level1Name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.add(game1);
        frame.setResizable(false);
        frame.setVisible(true);
        player2Time = game1.start();

        // Player 2 creates a level
        JOptionPane.showMessageDialog(null, "Player 2: Create a level.");
        Map map2 = new Map(new char[20][20]);
        MapEditor editor2 = new MapEditor(map2);
        editor2.open();
        while (editor2.isVisible()) {
            try {
                Thread.sleep(100); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String level2Name = editor2.getLevelName();

        // Player 1 plays the level
        JOptionPane.showMessageDialog(null, "Player 2: Pass the PC to Player 1.");
        Game game2 = new Game(level2Name);
        frame.getContentPane().removeAll();
        frame.add(game2);
        frame.revalidate();
        frame.repaint();
        player1Time = game2.start();

        // Display the winner
        String winner = player1Time < player2Time ? "Player 1" : "Player 2";
        JOptionPane.showMessageDialog(null, "Game Over!\nPlayer 1 Time: " + player1Time + " seconds\nPlayer 2 Time: " + player2Time + " seconds\nWinner: " + winner);
        frame.dispose(); // Close the game window
    }

    private static void showSettings() {
        JOptionPane.showMessageDialog(null, "Settings Menu\n(Back to Main Menu)");
    }
}