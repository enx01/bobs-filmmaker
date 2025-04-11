package xyz.bobindustries.film.gui.panes;

import javax.swing.*;

import xyz.bobindustries.film.gui.elements.utilitaries.Bob;

import java.awt.*;

public class AboutPane extends JPanel {

    public AboutPane() {
        setLayout(new BorderLayout());

        // Create the left panel with Bob and label
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0; // Move to the next row for the label
        JLabel bobLabel = new JLabel("bob");
        gbc.insets = new Insets(0, 0, -80, 0); // Add some padding
        bobLabel.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(bobLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER; // Center Bob
        gbc.insets = new Insets(10, 10, -10, 10); // Add some padding

        Bob bob = new Bob();
        bob.setPreferredSize(new Dimension(200, 200)); // Set preferred size for Bob's panel

        leftPanel.add(bob, gbc);

        // Create the right panel with labels
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.anchor = GridBagConstraints.CENTER; // Center the labels
        gbcRight.insets = new Insets(10, 10, 30, 10); // Add some padding

        JLabel aboutLabel = new JLabel("About");
        aboutLabel.setFont(new Font("Arial", Font.BOLD, 26));
        rightPanel.add(aboutLabel, gbcRight);

        gbcRight.insets = new Insets(10, 10, 10, 10); // Add some padding
        gbcRight.gridy = 1; // Move to the next row for name1
        JLabel name1Label = new JLabel("Tiago Cardoso");
        rightPanel.add(name1Label, gbcRight);

        gbcRight.gridy = 2; // Move to the next row for name2
        JLabel name2Label = new JLabel("Corentin Tiberghien");
        rightPanel.add(name2Label, gbcRight);

        // Add left and right panels to the main AboutPane
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

}
