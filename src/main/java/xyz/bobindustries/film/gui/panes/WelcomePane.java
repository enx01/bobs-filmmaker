package xyz.bobindustries.film.gui.panes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;

/**
 * Welcome view of the application showing two buttons :
 * - Create new project;
 * - Open existing project;
 */
public class WelcomePane extends JPanel {

    public WelcomePane() {
        setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JButton createNew = createButton("New project", "newproject.png");
        JButton openExist = createButton("Open project", "openproject.png");

        // Set constraints for button1
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(createNew, gbc);

        // Set constraints for button2
        gbc.weightx = 0; // Allow button2 to grow horizontally
        gbc.weighty = 0; // Allow button2 to grow vertically
        gbc.gridx = 1; // Column 1
        gbc.gridy = 0; // Row 0
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(openExist, gbc);

        add(buttonsPanel);
    }

    private JButton createButton(String text, String imageName) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());

        try (InputStream is = WelcomePane.class.getResourceAsStream(imageName)) {
            if (is == null) {
                SimpleErrorDialog.showErrorDialog("InputStream returned null! :(");
            } else {
                BufferedImage img = ImageIO.read(is);
                ImageIcon icon = new ImageIcon(img);

                button.setIcon(icon);
            }
        } catch (IOException e) {
            SimpleErrorDialog.showErrorDialog("Image Not Read :(");
        }

        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setVerticalAlignment(JLabel.BOTTOM);
        label.setHorizontalAlignment(JLabel.CENTER);

        button.add(label, BorderLayout.SOUTH);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        button.setPreferredSize(new Dimension(250, 250));

        return button;
    }
}
