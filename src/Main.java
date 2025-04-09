import javax.swing.*;

public class Main {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Java Raycaster Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setResizable(false);

        MainMenu menu = new MainMenu(frame);
        frame.add(menu);

        frame.setVisible(true);
    }
}