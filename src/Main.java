import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;


public class Main {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Java Raycaster Game");
        TextureLoader.loadTextures();

        Game game = new Game();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.add(game);
        frame.setResizable(false);
        frame.setVisible(true);
        
        game.start();
    }
}