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
        JLabel background = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("textures/menu_background.jpg");
                Image scaledImage = icon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                g.drawImage(scaledImage, 0, 0, null);
            }
        };
        background.setLayout(new GridBagLayout());
        add(background, BorderLayout.CENTER);

        // Menu buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0); // Add spacing between buttons
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        JButton newGameButton = createButton("Create New Level");
        JButton loadGameButton = createButton("Load Existing Level");
        JButton exitButton = createButton("Exit");

        newGameButton.addActionListener(e -> startNewGame());
        loadGameButton.addActionListener(e -> loadGame());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(newGameButton, gbc);
        buttonPanel.add(loadGameButton, gbc);
        buttonPanel.add(exitButton, gbc);

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