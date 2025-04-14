import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MainMenu extends JPanel {
    private JFrame frame;
    private BufferedImage backgroundImage;

    public MainMenu(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        // Load the background image
        try {
            backgroundImage = ImageIO.read(new File("textures/menu_background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
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
        frame.add(new MapEditor(new Map(new char[20][20])));
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