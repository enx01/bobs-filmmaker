package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.JDialog;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class NewProjectDialog extends JDialog {

    public NewProjectDialog() {
        setSize(400, 150);
        setResizable(false);
        setMinimumSize(new Dimension(400, 150));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // gbc.insets = new Insets(2, 5, 5, 5); // Add padding around components

        JLabel nameLabel = new JLabel("name:");
        JTextField nameField = new JTextField(30); // Set width to 15 columns
        JLabel locationLabel = new JLabel("location:");
        JTextField locationField = new JTextField(30); // Set width to 15 columns
        JButton browseButton = new JButton("...");
        JButton createButton = new JButton("create");

        // Add components to the layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(locationLabel, gbc);

        gbc.gridx = 1;
        add(locationField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(browseButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        add(createButton, gbc);

        // Browse button action
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(NewProjectDialog.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                locationField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        // Create button action
        createButton.addActionListener(e -> {
            String projectName = nameField.getText().trim();
            String projectLocation = locationField.getText().trim();

            if (!projectName.isEmpty() && !projectLocation.isEmpty()) {
                try {

                    Project newProject = ProjectManager.createProject(projectName, projectLocation);

                    ProjectManager.setCurrent(newProject);

                } catch (IOException | InvalidProjectDirectoryException ex) {
                    SimpleErrorDialog.showErrorDialog("Error creating project : invalid project directory");
                }

                dispose();
            } else {
                SimpleErrorDialog.showErrorDialog("Please provide a valid project name and location.");

            }

        });

    }
}
