package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.*;
import java.awt.*;

/*
    Simple Yes / No user prompting dialog.
 */
public class YesNoDialog extends JDialog {
    public static final int YES = 1;
    public static final int NO = 0;

    private boolean choice = false;

    public YesNoDialog(Frame owner, String query) {
        super(owner, true);

        setSize(400, 150);
        setResizable(false);
        setMinimumSize(new Dimension(600, 150));
        setLocationRelativeTo(null); // Center the dialog on the screen
        setLayout(new BorderLayout());

        // Add label to display the query
        JLabel queryLabel = new JLabel(query, JLabel.CENTER);
        add(queryLabel, BorderLayout.CENTER);

        // Create panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Yes button
        JButton yesButton = new JButton("Yes");
        yesButton.addActionListener(e -> {
            choice = true;
            setVisible(false);
        });

        // No button
        JButton noButton = new JButton("No");
        noButton.addActionListener(e -> {
            choice = false;
            setVisible(false);
        });

        // Add buttons to panel
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    public static int show(Frame parent, String query) {
        YesNoDialog dialog = new YesNoDialog(parent, query);
        dialog.setVisible(true);
        return dialog.choice ? YES : NO; // Return the result
    }
}
