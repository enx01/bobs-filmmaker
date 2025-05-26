package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.JDialog;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.RecentProjectsProvider;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;
import xyz.bobindustries.film.projects.ConfigProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class NewProjectDialog extends JDialog {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    private boolean isSuccess = false;

    public NewProjectDialog(Frame parent) {
        super(parent, "new project", true);
        setSize(600, 300);
        setResizable(true);
        setMinimumSize(new Dimension(600, 150));
        setLocationRelativeTo(null); // Center the dialog on the screen
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // gbc.insets = new Insets(2, 5, 5, 5); // Add padding around components

        JLabel nameLabel = new JLabel("name:");
        JTextField nameField = new JTextField(30); // Set width to 15 columns
        JLabel locationLabel = new JLabel("location:");
        JTextField locationField = new JTextField(30); // Set width to 15 columns

        JButton browseButton = new JButton("...");

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

        JLabel resolutionLabel = new JLabel("resolution (WxH):");
        JTextField widthField = new JTextField("1920", 6);
        JTextField heightField = new JTextField("1080", 6);
        JLabel xLabel = new JLabel("x");

        // Add components to the layout
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(resolutionLabel, gbc);

        gbc.gridx = 1;
        JPanel resolutionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resolutionPanel.add(widthField);
        resolutionPanel.add(xLabel);
        resolutionPanel.add(heightField);
        add(resolutionPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        add(createButton, gbc);

        // Create button action
        createButton.addActionListener(e -> {
            String projectName = nameField.getText().trim();
            String projectLocation = locationField.getText().trim();
            String widthText = widthField.getText().trim();
            String heightText = heightField.getText().trim();

            if (!projectName.isEmpty() && !projectLocation.isEmpty() && !widthText.isEmpty() && !heightText.isEmpty()) {
                int width, height;
                try {
                    width = Integer.parseInt(widthText);
                    height = Integer.parseInt(heightText);
                    if (width <= 0 || height <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    SimpleErrorDialog.show("Please provide valid positive integers for resolution.");
                    isSuccess = false;
                    return;
                }
                try {
                    Project newProject = ProjectManager.createProject(projectName, projectLocation);
                    newProject.loadProperties();
                    ProjectManager.setCurrent(newProject);
                    // Sauvegarde la résolution dans la config du projet
                    java.util.Properties props = ConfigProvider.loadProperties(newProject);
                    ConfigProvider.setResolution(props, width, height);
                    ConfigProvider.saveProperties(newProject, props);
                    System.out.println(newProject.getProjectDir().toString());
                    RecentProjectsProvider.writeConfigFile(newProject.getProjectDir().toString());
                    isSuccess = true;
                    dispose();
                } catch (IOException | InvalidProjectDirectoryException ex) {
                    SimpleErrorDialog.show("Error creating project : invalid project directory");
                    isSuccess = false;
                    dispose();
                }
            } else {
                SimpleErrorDialog.show("Please provide a valid project name, location, and resolution.");
                isSuccess = false;
                dispose();
            }
        });

    }

    public static int show(Frame parent) {
        NewProjectDialog dialog = new NewProjectDialog(parent);
        dialog.setVisible(true); // Show the dialog modally
        return dialog.isSuccess ? SUCCESS : FAILURE; // Return the result
    }
}
