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
import java.util.Properties;

public class NewProjectDialog extends JDialog {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    private boolean isSuccess = false;

    public NewProjectDialog(Frame parent) {
        super(parent, "new project", true);
        setSize(600, 300);
        setResizable(true);
        setMinimumSize(new Dimension(600, 300));
        setLocationRelativeTo(null); // Center the dialog on the screen
        setLayout(new GridLayout(4, 0));

        JLabel nameLabel = new JLabel("name:");
        JTextField nameField = new JTextField(30); // Set width to 15 columns
        nameField.setBorder(BorderFactory.createEtchedBorder());
        JLabel locationLabel = new JLabel("location:");
        JTextField locationField = new JTextField(30); // Set width to 15 columns
        locationField.setBorder(BorderFactory.createEtchedBorder());

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

        JPanel locationPanel = new JPanel(new BorderLayout());

        locationPanel.add(locationLabel, BorderLayout.WEST);
        locationPanel.add(locationField, BorderLayout.CENTER);
        locationPanel.add(browseButton, BorderLayout.EAST);

        locationPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel namePanel = new JPanel(new BorderLayout());

        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);

        namePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel resolutionLabel = new JLabel("resolution (WxH):");
        JTextField widthField = new JTextField("800", 6);
        widthField.setBorder(BorderFactory.createEtchedBorder());
        JTextField heightField = new JTextField("600", 6);
        heightField.setBorder(BorderFactory.createEtchedBorder());
        JLabel xLabel = new JLabel("x");

        JPanel resolutionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        resolutionPanel.add(widthField);
        resolutionPanel.add(xLabel);
        resolutionPanel.add(heightField);
        resolutionPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel resLabelAndInput = new JPanel(new BorderLayout());

        resLabelAndInput.add(resolutionLabel, BorderLayout.WEST);
        resLabelAndInput.add(resolutionPanel, BorderLayout.CENTER);

        add(namePanel);
        add(locationPanel);
        add(resLabelAndInput);
        add(createButton);

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
                    if (width <= 0 || height <= 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    SimpleErrorDialog.show("Please provide valid positive integers for resolution.");
                    isSuccess = false;
                    return;
                }
                try {
                    Project newProject = ProjectManager.createProject(projectName, projectLocation);
                    Properties props = newProject.loadProperties();
                    ProjectManager.setCurrent(newProject);
                    // Sauvegarde la résolution dans la config du projet
                    // Properties props = ConfigProvider.loadProperties(newProject);
                    ConfigProvider.setResolution(props, width, height);
                    ConfigProvider.saveProperties(newProject, props);
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
