package xyz.bobindustries.film.gui.panes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.elements.dialogs.NewProjectDialog;
import xyz.bobindustries.film.gui.elements.utilitaries.ButtonFactory;
import xyz.bobindustries.film.gui.elements.utilitaries.LoadingWindow;
import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;

/**
 * Welcome view of the application showing two buttons :
 * - Create new project;
 * - Open existing project;
 */
public class WelcomePane extends JPanel {

    public WelcomePane() {
        setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JButton createNew = ButtonFactory.createButton("new project", "newproject.png");

        createNew.addActionListener(e -> {
            int result = NewProjectDialog.show(App.getFrame());

            if (result == NewProjectDialog.SUCCESS) {
                LoadingWindow loadingWindow = new LoadingWindow("loading project...", 200, 100);

                loadingWindow.setVisible(true);
                loadingWindow.requestFocus();

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() { // Classe anonyme d'initialisation de la frame.
                    @Override
                    protected Void doInBackground() throws Exception {
                        /* Changement du contenu de la fenetre */
                        App.getFrame().getContentPane().removeAll();
                        App.getFrame().add(new ProjectWelcomePane());

                        Thread.sleep(2000);
                        App.getFrame().revalidate();

                        return null;
                    }

                    protected void done() {
                        loadingWindow.dispose();
                    }
                };
                worker.execute();
            }
        });

        JButton openExist = ButtonFactory.createButton("open project", "openproject.png");

        // Create a title label
        JLabel titleLabel = new JLabel("bob's filmmaker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32)); // Set font style and size
        gbc.fill = GridBagConstraints.CENTER; // Center the title
        gbc.weightx = 0; // Allow title to grow horizontally
        gbc.weighty = 0; // Do not allow title to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 2; // Span across two columns
        gbc.insets = new Insets(10, 25, 10, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(titleLabel, gbc);

        // Set constraints for button1
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(createNew, gbc);

        // Set constraints for button2
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 0; // Allow button1 to grow horizontally
        gbc.weighty = 0; // Allow button1 to grow vertically
        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 0
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 25, 25, 25); // Add spacing (top, left, bottom, right)
        buttonsPanel.add(openExist, gbc);

        add(buttonsPanel, BorderLayout.CENTER);
    }
}
