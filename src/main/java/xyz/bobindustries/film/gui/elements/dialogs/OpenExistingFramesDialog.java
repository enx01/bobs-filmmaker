package xyz.bobindustries.film.gui.elements.dialogs;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class OpenExistingFramesDialog extends JDialog {
    private JList<String> liste;
    private DefaultListModel<String> modeleListe;
    private JButton boutonEnregistrer;
    private ArrayList<String> selectedFrames;

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    private boolean isSuccess = false;

    public OpenExistingFramesDialog(Frame parent, ArrayList<ImageFile> images) {
        super(parent, "Sélection des frames", true); // true = modal
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // Modèle de liste avec quelques éléments
        modeleListe = new DefaultListModel<>();

        for (ImageFile image : images) {
            modeleListe.addElement(image.getFileName());
        }

        // JList avec sélection multiple
        liste = new JList<>(modeleListe);
        liste.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(liste);

        // Bouton pour enregistrer la sélection
        boutonEnregistrer = new JButton("Enregistrer la sélection");
        boutonEnregistrer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!liste.getSelectedValuesList().isEmpty()) {
                    setSelectedFrames((ArrayList<String>) liste.getSelectedValuesList());
                    isSuccess = true;
                } else {
                   SimpleErrorDialog.show("Erreur, aucune frame sélectionnée");
                    isSuccess = false;
                }
                dispose();
            }
        });

        // Layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(boutonEnregistrer, BorderLayout.SOUTH);
    }

    public void setSelectedFrames(ArrayList<String> selectedFrames) {
        this.selectedFrames = selectedFrames;
    }

    private void enregistrerSelections(ArrayList<String> selections, ArrayList<String> selectedFrames) {
        int[] selectedIndices = liste.getSelectedIndices();
        isSuccess = true;
        dispose();
    }

    public static ArrayList<String> show(Frame parent, ArrayList<ImageFile> images) {
        OpenExistingFramesDialog dialog = new OpenExistingFramesDialog(parent, images);
        dialog.setVisible(true); // Show the dialog modally
        return dialog.selectedFrames;
    }
}
