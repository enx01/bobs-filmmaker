package xyz.bobindustries.film.gui.panes;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.ButtonFactory;

/**
 * Welcome view of the application showing two buttons :
 * - Create new project;
 * - Open existing project;
 */
public class WelcomePane extends JPanel {

    public WelcomePane() {
        setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JButton createNew = ButtonFactory.createButton("new project", "newproject.png", 250, 250);
        createNew.addActionListener(ActionListenerProvider::getNewProjectDialogAction);

        JButton openExist = ButtonFactory.createButton("open project", "openproject.png", 250, 250);
        openExist.addActionListener(ActionListenerProvider::getOpenProjectDialogAction);

        // Empty left spacer
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        buttonsPanel.add(Box.createHorizontalGlue(), gbc);

        // Title
        JLabel titleLabel = new JLabel("bob's filmmaker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 30, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        buttonsPanel.add(titleLabel, gbc);

        // Row of buttons
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 20, 10, 20);

        gbc.gridx = 1;
        buttonsPanel.add(createNew, gbc);

        gbc.gridx = 2;
        buttonsPanel.add(openExist, gbc);

        // Empty right spacer
        gbc.gridx = 4;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        buttonsPanel.add(Box.createHorizontalGlue(), gbc);

        add(buttonsPanel, BorderLayout.CENTER);
    }
}
