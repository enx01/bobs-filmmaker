package xyz.bobindustries.film.gui.elements.dialogs;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.RecentProjectsProvider;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class OpenProjectDialog extends JDialog {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    HashMap<String, String> pathsShortened;

    private boolean browseProjectsSelected = true;

    private boolean isSuccess = false;

    public OpenProjectDialog(Frame owner) {
        super(owner, "Open Project", true);

        pathsShortened = new HashMap<>();

        setSize(400, 200);
        setResizable(false);
        setMinimumSize(new Dimension(600, 200));
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel locationLabel = new JLabel("Location:");
        JTextField locationField = new JTextField(30);
        JButton browseButton = new JButton("...");

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(OpenProjectDialog.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                locationField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        JLabel recentProjectsLabel = new JLabel("Recent Projects:");
        JComboBox<String> recentProjectsComboBox = getRecentsProjectsJComboBox(pathsShortened);

        JButton toggleButton = new JButton("Choose a recent project");

        locationLabel.setVisible(true);
        locationField.setVisible(true);
        browseButton.setVisible(true);
        recentProjectsLabel.setVisible(false);
        recentProjectsComboBox.setVisible(false);

        toggleButton.addActionListener(e -> {
            if (!browseProjectsSelected) {
                locationLabel.setVisible(true);
                locationField.setVisible(true);
                browseButton.setVisible(true);
                recentProjectsLabel.setVisible(false);
                recentProjectsComboBox.setVisible(false);
                toggleButton.setText("Choose a recent project");
                browseProjectsSelected=true;
            } else {
                locationLabel.setVisible(false);
                locationField.setVisible(false);
                browseButton.setVisible(false);
                recentProjectsLabel.setVisible(true);
                recentProjectsComboBox.setVisible(true);
                toggleButton.setText("Choose a project location");
                browseProjectsSelected=false;
            }
        });

        JButton openButton = new JButton("Open");

        openButton.addActionListener(e -> {
            if (!browseProjectsSelected) {
                if (recentProjectsComboBox.getSelectedItem() != null) {
                    String selectedProject = (String) recentProjectsComboBox.getSelectedItem();
                    try {
                        Project newProject = ProjectManager.openProject(pathsShortened.get(selectedProject));
                        newProject.loadProperties();
                        ProjectManager.setCurrent(newProject);
                        RecentProjectsProvider.writeConfigFile(newProject.getProjectDir().toString());
                        isSuccess = true;
                        dispose();
                    } catch (IOException | InvalidProjectDirectoryException ex) {
                        SimpleErrorDialog.show("Error opening project: invalid project directory");
                        isSuccess = false;
                        dispose();
                    }
                } else {
                    SimpleErrorDialog.show("Please select a recent project.");
                    isSuccess = false;
                    dispose();
                }
            } else {
                String projectLocation = locationField.getText().trim();
                if (!projectLocation.isEmpty()) {
                    try {
                        Project newProject = ProjectManager.openProject(projectLocation);
                        newProject.loadProperties();
                        ProjectManager.setCurrent(newProject);

                        RecentProjectsProvider.writeConfigFile(newProject.getProjectDir().toString());

                        isSuccess = true;
                        dispose();
                    } catch (IOException | InvalidProjectDirectoryException ex) {
                        SimpleErrorDialog.show("Error opening project: invalid project directory");
                        isSuccess = false;
                        dispose();
                    }
                } else {
                    SimpleErrorDialog.show("Please provide a valid location.");
                    isSuccess = false;
                    dispose();
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(locationLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(locationField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(browseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(toggleButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(recentProjectsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(recentProjectsComboBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(openButton, gbc);
    }

    private static JComboBox<String> getRecentsProjectsJComboBox(HashMap<String, String> pathsShortened) {
        JComboBox<String> recentProjectsComboBox = new JComboBox<>();

        ArrayList<String> recentProjects = RecentProjectsProvider.getDirectoriesFromConfig();
        for (String project : recentProjects) {
            String displayName = shortenPath(project, 50);
            pathsShortened.put(displayName, project);
            recentProjectsComboBox.addItem(displayName);
        }
        return recentProjectsComboBox;
    }

    private static String shortenPath(String path, int maxLength) {
        if (path.length() <= maxLength) {
            return path;
        } else {
            return "..." + path.substring(path.length() - maxLength + 3);
        }
    }

    public static int show(Frame parent) {
        OpenProjectDialog dialog = new OpenProjectDialog(parent);
        dialog.setVisible(true); // Show the dialog modally
        return dialog.isSuccess ? SUCCESS : FAILURE; // Return the result
    }
}
