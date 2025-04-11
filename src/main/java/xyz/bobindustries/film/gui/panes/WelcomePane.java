package xyz.bobindustries.film.gui.panes;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.ButtonFactory;

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

        JButton createNew = ButtonFactory.createButton("new project", "newproject.png", 250, 250);
        createNew.addActionListener(ActionListenerProvider::getNewProjectDialogAction);

        JButton openExist = ButtonFactory.createButton("open project", "openproject.png", 250, 250);
        openExist.addActionListener(ActionListenerProvider::getOpenProjectDialogAction);

        // Create a title label
        JLabel titleLabel = new JLabel("bob's filmmaker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32)); // Set font style and size
        gbc.fill = GridBagConstraints.CENTER; // Center the title
        gbc.weightx = 0; // Allow title to grow horizontally
        gbc.weighty = 0; // Do not allow title to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 2; // Span across two columns
        gbc.insets = new Insets(10, 25, 10, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(titleLabel, gbc);

        // Set constraints for button1
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(createNew, gbc);

        // Set constraints for button2
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(openExist, gbc);

        add(buttonsPanel, BorderLayout.CENTER);
    }
}
