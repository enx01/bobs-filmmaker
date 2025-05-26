package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.*;

import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenImageDialog extends JDialog {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    private boolean isSuccess = false;

    private static Color[][] gridResult;
    private static String imageLoc;

//    private static final ImageLayers layers = new ImageLayers();

    public OpenImageDialog(Frame owner) {
        super(owner, "open project", true);

        setSize(400, 150);
        setResizable(false);
        setMinimumSize(new Dimension(600, 150));
        setLocationRelativeTo(null); // Center the dialog on the screen
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel nameLabel = new JLabel("name:");
        JTextField nameField = new JTextField(30);
        JLabel locationLabel = new JLabel("location:");
        JTextField locationField = new JTextField(30); // Set width to 15 columns

        JButton browseButton = new JButton("...");

        // Browse button action
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnValue = fileChooser.showOpenDialog(OpenImageDialog.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                locationField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        JButton createButton = new JButton("open");

        // Create button action
        createButton.addActionListener(e -> {
            String imageLocation = locationField.getText().trim();
            Path paths = Paths.get(imageLocation);
            String imageName = paths.getFileName().toString();

            if (!imageName.isEmpty() && !imageLocation.isEmpty()) {
                try {
                    ImageFile image = new ImageFile(imageLocation);

                    gridResult = image.getColorMatrix();
                    imageLoc = imageLocation;
                    isSuccess = true;
                    dispose();

                } catch (IOException ex) {
                    SimpleErrorDialog.show("Error creating project : invalid project directory");
                    isSuccess = false;
                    dispose();
                }

                dispose();
            } else {
                SimpleErrorDialog.show("Please provide a valid project name and location.");
                isSuccess = false;
                dispose();
            }

        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(locationLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
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

    public Color[][] getGridResult() {
        return gridResult;
    }

    public String getImageLoc() {
        return imageLoc;
    }

    public static Pair<Color[][], String> show(Frame parent) {
        OpenImageDialog dialog = new OpenImageDialog(parent);
        dialog.setVisible(true); // Show the dialog modally
        return new Pair<>(dialog.getGridResult(), dialog.getImageLoc());
    }

}