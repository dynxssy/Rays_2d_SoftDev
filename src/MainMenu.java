import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JPanel {
    private JFrame frame;

    public MainMenu(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        // Background panel
        JLabel background = new JLabel(new ImageIcon("src/textures/menu_background.jpg"));
        background.setLayout(new GridBagLayout());
        add(background, BorderLayout.CENTER);

        // Menu buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton newGameButton = createButton("Create New Level");
        newGameButton.addActionListener(e -> startNewGame());

        JButton loadGameButton = createButton("Load Existing Level");
        loadGameButton.addActionListener(e -> loadGame());

        JButton exitButton = createButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createVerticalStrut(20)); // Add spacing
        buttonPanel.add(loadGameButton);
        buttonPanel.add(Box.createVerticalStrut(20)); // Add spacing
        buttonPanel.add(exitButton);

        background.add(buttonPanel);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void startNewGame() {
        frame.getContentPane().removeAll();
        frame.add(new Game());
        frame.revalidate();
        frame.repaint();
    }

    private void loadGame() {
        frame.getContentPane().removeAll();
        frame.add(new Game(true)); // Pass a flag to load an existing level
        frame.revalidate();
        frame.repaint();
    }
}