import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Java Raycaster Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);

        MainMenu menu = new MainMenu(frame);
        frame.add(menu);

        frame.setVisible(true);
    }
}