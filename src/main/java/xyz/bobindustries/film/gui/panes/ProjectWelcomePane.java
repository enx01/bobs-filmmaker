package xyz.bobindustries.film.gui.panes;

import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.ButtonFactory;
import xyz.bobindustries.film.projects.ProjectManager;

import javax.swing.*;
import java.awt.*;

public class ProjectWelcomePane extends JPanel {

    public ProjectWelcomePane() {
        setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JButton editImagesButton = ButtonFactory.createButton("edit images", "base_image.png", 125, 125);
        JButton editScenarioButton = ButtonFactory.createButton("edit scenario", "base_image.png", 125, 125);
        JButton visualizerButton = ButtonFactory.createButton("view film", "base_image.png", 125, 125);

        editImagesButton.addActionListener(ActionListenerProvider::getShowImageEditorFrameAction);
        editScenarioButton.addActionListener(ActionListenerProvider::getShowScenarioEditorFrameAction);
        visualizerButton.addActionListener(ActionListenerProvider::getShowFilmVisualizerFrameAction);

        JLabel projectTitleLabel = new JLabel(ProjectManager.getCurrent().getProjectName());
        projectTitleLabel.setFont(new Font("Arial", Font.BOLD, 32)); // Set font style and size
        gbc.fill = GridBagConstraints.CENTER; // Center the title
        gbc.weightx = 0; // Allow title to grow horizontally
        gbc.weighty = 0; // Do not allow title to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 3; // Span across two columns
        gbc.insets = new Insets(10, 25, 10, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(projectTitleLabel, gbc);

        // Set constraints for button1
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(editImagesButton, gbc);

        // Set constraints for button2
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(editScenarioButton, gbc);
        // Set constraints for button2
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 2; // Column 2
        gbc.gridy = 1; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(visualizerButton, gbc);

        add(buttonsPanel, BorderLayout.CENTER);
    }
}
