package xyz.bobindustries.film.gui.panes;

import xyz.bobindustries.film.gui.elements.utilitaries.ButtonFactory;
import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;

import java.awt.*;
import javax.swing.*;

public class OpenEditor extends JPanel {
    public OpenEditor(JInternalFrame frame) {
        setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JButton createImage = ButtonFactory.createButton("create image", "base_image.png", 125, 125);
        JButton openImage = ButtonFactory.createButton("open file", "base_image.png", 125, 125);
        JButton openExistingFrames = ButtonFactory.createButton("open frames", "base_image.png", 125, 125);

        createImage.addActionListener(ActionListenerProvider::createImageAction);

        openImage.addActionListener(ActionListenerProvider::openImageAction);

        openExistingFrames.addActionListener(ActionListenerProvider::openExistingFrames);

        // Set constraints for button1
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(createImage, gbc);

        // Set constraints for button2
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 1; // Column 1
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(openImage, gbc);

        // Set constraints for button3
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 2; // Column 1
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(openExistingFrames, gbc);

        add(buttonsPanel, BorderLayout.CENTER);
    }
}
