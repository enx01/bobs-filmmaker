package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.*;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidFileFormatException;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class OpenImageDialog extends JDialog {
    private static boolean isSuccess = false;

    private static Color[][] gridResult;
    private static String imageLoc;

    // private static final ImageLayers layers = new ImageLayers();

    public OpenImageDialog(Frame owner) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(OpenImageDialog.this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            String imageLocation = selectedDirectory.getAbsolutePath().trim();

            if (!imageLocation.isEmpty()) {
                try {
                    ImageFile image = new ImageFile(imageLocation);

                    try {
                        gridResult = image.getColorMatrix();
                    } catch (InvalidFileFormatException iffe) {
                        SimpleErrorDialog.show(iffe.getMessage());
                        isSuccess = false;
                        dispose();
                        return;
                    }

                    imageLoc = imageLocation;
                    isSuccess = true;
                    dispose();
                    return;

                } catch (IOException ex) {
                    SimpleErrorDialog.show("Error opening file : Invalid file");
                    isSuccess = false;
                    dispose();
                    return;
                }

            } else {
                SimpleErrorDialog.show("Please provide a valid project name and location.");
                isSuccess = false;
                dispose();
                return;
            }
        } else if (returnValue == JFileChooser.ERROR_OPTION || returnValue == JFileChooser.CANCEL_OPTION) {
            isSuccess = false;
            dispose();
            return;
        }
    }

    public static Pair<Color[][], String> show(Frame parent) {
        OpenImageDialog dialog = new OpenImageDialog(parent);
        // dialog.setVisible(true); // Show the dialog modally

        if (!isSuccess)
            return null;
        else
            return new Pair<>(gridResult, imageLoc);
    }

}
