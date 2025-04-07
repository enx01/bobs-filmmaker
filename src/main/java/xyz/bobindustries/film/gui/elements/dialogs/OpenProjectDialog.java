package xyz.bobindustries.film.gui.elements.dialogs;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class OpenProjectDialog extends JDialog {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    private boolean isSuccess = false;

    public OpenProjectDialog(Frame owner) {
        super(owner, "open project", true);

        setSize(400, 150);
        setResizable(false);
        setMinimumSize(new Dimension(600, 150));
        setLocationRelativeTo(null); // Center the dialog on the screen
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel locationLabel = new JLabel("location:");
        JTextField locationField = new JTextField(30); // Set width to 15 columns

        JButton browseButton = new JButton("...");

        // Browse button action
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(OpenProjectDialog.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                locationField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        JButton createButton = new JButton("create");

        // Create button action
        createButton.addActionListener(e -> {
            String projectLocation = locationField.getText().trim();

            if (!projectLocation.isEmpty()) {
                try {

                    Project newProject = ProjectManager.createProject(projectName, projectLocation);

                    ProjectManager.setCurrent(newProject);

                    isSuccess = true;
                    dispose();

                } catch (IOException | InvalidProjectDirectoryException ex) {
                    SimpleErrorDialog.showErrorDialog("Error creating project : invalid project directory");
                    isSuccess = false;
                    dispose();
                }

                dispose();
            } else {
                SimpleErrorDialog.showErrorDialog("Please provide a valid project name and location.");
                isSuccess = false;
                dispose();
            }

        });

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

    }
}
