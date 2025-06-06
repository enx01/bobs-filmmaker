package xyz.bobindustries.film.gui.elements.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import xyz.bobindustries.film.projects.ConfigProvider;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.exceptions.ImageNotFoundInDirectoryException;
import xyz.bobindustries.film.utils.ImageUtils;

public class BrokenProjectRecoveryDialog extends JDialog {

    public BrokenProjectRecoveryDialog(Exception e) {
        super();
        setModal(true);
        setSize(600, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setTitle("broken project recovery");

        System.out.println("[+] BrokenProjectRecoveryDialog");

        if (e instanceof ImageNotFoundInDirectoryException) {
            String imageName = ((ImageNotFoundInDirectoryException) e).imageName;
            setLayout(new BorderLayout());

            JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 20, 20));

            JButton createEmptyImageButton = new JButton("create an empty image \"" + imageName + "\".");
            JButton deleteImageFromScenario = new JButton("delete all occurences of \"" + imageName + "\".");
            JButton abort = new JButton("abort");

            createEmptyImageButton.addActionListener(ev -> {
                ProjectManager.getCurrent().createNewEmptyFile(imageName);

                setVisible(false);
                dispose();
            });

            // TODO : deleteImageFromScenario actionListener

            abort.addActionListener(ev -> {
                setVisible(false);
                dispose();
            });

            optionsPanel.add(createEmptyImageButton);
            optionsPanel.add(deleteImageFromScenario);
            optionsPanel.add(abort);
            add(new JLabel("image \"" + imageName + "\" not found in images directory."), BorderLayout.NORTH);
            add(optionsPanel, BorderLayout.CENTER);
        }
    }

    public static void show(Exception e) {
        BrokenProjectRecoveryDialog bprd = new BrokenProjectRecoveryDialog(e);
        bprd.setVisible(true);
    }
}
