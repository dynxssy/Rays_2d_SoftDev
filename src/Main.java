import javax.swing.*;

public class Main {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Java Raycaster Game");

        // Load textures and start background music
        TextureLoader.loadTextures();
        SoundManager.playBackgroundMusic("sounds/background-music.mp3");  // Add your music file here
        
        Game game = new Game();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.add(game);
        frame.setResizable(false);
        frame.setVisible(true);
        
        game.start();
    }
}
